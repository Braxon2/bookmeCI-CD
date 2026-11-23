#!/usr/bin/env bash
set -Eeuo pipefail

# Exercise orchestration only: no Docker daemon, SSH connection or real secrets.
script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
test_dir=$(mktemp -d "${TMPDIR:-/tmp}/bookme-deploy-test.XXXXXX")
cleanup() {
  case "$test_dir" in */bookme-deploy-test.*) rm -rf -- "$test_dir" ;; esac
}
trap cleanup EXIT
mkdir -p "$test_dir/bin"
touch "$test_dir/.env"

cat > "$test_dir/bin/sudo" <<'MOCK'
#!/usr/bin/env bash
exec "$@"
MOCK
cat > "$test_dir/bin/docker" <<'MOCK'
#!/usr/bin/env bash
set -eu
printf '%s %s\n' "${APP_VERSION:-}" "$*" >> "$TRACE_FILE"
case "$1" in
  inspect)
    [[ "${FIRST_DEPLOY:-0}" != 1 ]] || exit 1
    echo "sha256:previous-${@: -1}"
    ;;
  image) exit 0 ;;
  compose)
    case " $* " in
      *" config --quiet "*) exit 0 ;;
      *" config --format json "*)
        printf '{"services":{"backend":{"environment":{"JWT_SECRET_KEY":"%s","AI_CHAT_PROVIDER":"%s","GEMINI_API_KEY":""}}}}\n' \
          "${TEST_KEY:-MDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDA=}" "${TEST_PROVIDER:-none}"
        ;;
      *" pull "*) [[ "${FAIL_PULL:-0}" != 1 ]] ;;
      *" up "*)
        if [[ "$APP_VERSION" == rollback-* ]]; then
          [[ "${FAIL_ROLLBACK:-0}" != 1 ]]
        else
          [[ "${FAIL_UP:-0}" != 1 ]]
        fi
        ;;
      *) exit 2 ;;
    esac
    ;;
  *) exit 2 ;;
esac
MOCK
cat > "$test_dir/bin/curl" <<'MOCK'
#!/usr/bin/env bash
[[ "${FAIL_PROXY:-0}" != 1 ]]
MOCK
chmod +x "$test_dir/bin/"*
export PATH="$test_dir/bin:$PATH"
export CURRENT_SHA=0123456789abcdef0123456789abcdef01234567
export DOCKERHUB_USERNAME=example
export TRACE_FILE="$test_dir/trace"
cd "$test_dir"

run_case() {
  local name=$1 expected=$2 actual=0
  shift 2
  : > "$TRACE_FILE"
  env "$@" bash "$script_dir/deploy.sh" > "$test_dir/output" 2>&1 || actual=$?
  if [[ "$actual" != "$expected" ]]; then
    cat "$test_dir/output" >&2
    echo "FAIL: $name (expected $expected, got $actual)" >&2
    exit 1
  fi
  echo "PASS: $name"
}
assert_rollback() { grep -q "rollback-$CURRENT_SHA .* up .*--pull never" "$TRACE_FILE"; }
assert_no_up() { if grep -q ' up ' "$TRACE_FILE"; then exit 1; fi; }

run_case success 0
grep -q 'image tag sha256:previous-bookme-backend example/bookme-backend:rollback-' "$TRACE_FILE"
run_case failed-pull-leaves-running-release 1 FAIL_PULL=1
assert_no_up
run_case failed-start-rolls-back 1 FAIL_UP=1
assert_rollback
run_case failed-proxy-rolls-back 1 FAIL_PROXY=1
assert_rollback
run_case failed-rollback-remains-failed 1 FAIL_UP=1 FAIL_ROLLBACK=1
assert_rollback
run_case first-deploy-failure 1 FIRST_DEPLOY=1 FAIL_UP=1
if grep -q -- '--pull never' "$TRACE_FILE"; then exit 1; fi
run_case missing-gemini-key-stops-before-pull 1 TEST_PROVIDER=google-genai
assert_no_up
if grep -q ' pull' "$TRACE_FILE"; then exit 1; fi
run_case invalid-signing-key-stops-before-pull 1 TEST_KEY=invalid
assert_no_up
run_case invalid-sha-stops-before-docker 1 CURRENT_SHA=bad
[[ ! -s "$TRACE_FILE" ]]
echo "All deployment checks passed."
