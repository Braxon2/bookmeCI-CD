#!/usr/bin/env bash
set -Eeuo pipefail

# Run in /home/ubuntu/bookme. Runtime secrets stay in that directory's .env.
: "${CURRENT_SHA:?CURRENT_SHA must be the commit SHA to deploy}"
: "${DOCKERHUB_USERNAME:?DOCKERHUB_USERNAME is required}"
[[ "$CURRENT_SHA" =~ ^[a-f0-9]{40}$ ]] || { echo "Invalid commit SHA" >&2; exit 1; }
[[ "$DOCKERHUB_USERNAME" =~ ^[a-z0-9][a-z0-9_-]*$ ]] || { echo "Invalid Docker Hub namespace" >&2; exit 1; }
[[ -f .env ]] || { echo "Create /home/ubuntu/bookme/.env before deployment (see DEPLOYMENT.md)." >&2; exit 1; }

compose() {
  sudo env "APP_VERSION=$1" "DOCKERHUB_USERNAME=$DOCKERHUB_USERNAME" \
    docker compose --env-file .env -f docker-compose.prod.yml "${@:2}"
}

# Validate without printing the resolved environment, which contains secrets.
compose "$CURRENT_SHA" config --quiet
compose "$CURRENT_SHA" config --format json | python3 -c '
import base64, json, sys
env = json.load(sys.stdin)["services"]["backend"]["environment"]
provider = env.get("AI_CHAT_PROVIDER", "none")
if provider not in ("none", "google-genai"):
    sys.exit("AI_CHAT_PROVIDER must be none or google-genai for this deployment.")
if provider == "google-genai" and not env.get("GEMINI_API_KEY", "").strip():
    sys.exit("Set GEMINI_API_KEY in the EC2 .env when enabling google-genai.")
try:
    key = base64.b64decode(env.get("JWT_SECRET_KEY", ""), validate=True)
except (ValueError, TypeError):
    sys.exit("JWT_SECRET_KEY must be valid Base64.")
if len(key) < 32:
    sys.exit("JWT_SECRET_KEY must encode at least 32 bytes.")
'

# Snapshot the actual previous images, even if the previous Git commit was never deployed.
rollback_tag="rollback-$CURRENT_SHA"
can_rollback=false
backend_image=$(sudo docker inspect --format '{{.Image}}' bookme-backend 2>/dev/null || true)
frontend_image=$(sudo docker inspect --format '{{.Image}}' bookme-frontend 2>/dev/null || true)
if [[ -n "$backend_image" && -n "$frontend_image" ]]; then
  sudo docker image tag "$backend_image" "$DOCKERHUB_USERNAME/bookme-backend:$rollback_tag"
  sudo docker image tag "$frontend_image" "$DOCKERHUB_USERNAME/bookme-frontend:$rollback_tag"
  can_rollback=true
fi

echo "Pulling images for $CURRENT_SHA"
# A failed pull leaves the currently running containers alone.
compose "$CURRENT_SHA" pull

if compose "$CURRENT_SHA" up -d --wait --wait-timeout 180 --remove-orphans; then
  # Also verify that Nginx forwards API traffic to Spring Boot.
  if curl --fail --silent --show-error --max-time 10 http://127.0.0.1/api/health > /dev/null; then
    echo "Deployment healthy: $CURRENT_SHA"
    exit 0
  fi
fi

echo "Deployment health check failed." >&2
if [[ "$can_rollback" == true ]]; then
  echo "Restoring the previously running backend and frontend images." >&2
  # Use the saved local tags, never assume github.event.before has published images.
  if compose "$rollback_tag" up -d --pull never --wait --wait-timeout 180 --remove-orphans; then
    echo "Previous application images restored. Deployment remains failed." >&2
  else
    echo "Rollback failed; inspect container health on EC2." >&2
  fi
else
  echo "No previous application images exist; first deployment cannot roll back." >&2
fi
# Never delete volumes. Image rollback does not undo Flyway database migrations.
exit 1
