import { useEffect, useState } from "react";
import "./styles/Login.css";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

const Login = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const { user, login, isLoading, error } = useAuth();

  const navigate = useNavigate();
  const handleSubmit = async (e) => {
    e.preventDefault();
    await login(email, password);
  };

  useEffect(() => {
    if (user) {
      if (user.role === "OWNER") navigate("/list-properties", { replace: true });
      else if (user.role === "ADMIN") navigate("/fascilities", { replace: true });
      else navigate("/search", { replace: true });
    }
  }, [user, navigate]);

  return (
    <div className="login-container">
      <form className="login-form" onSubmit={handleSubmit}>
        <h2>Log in</h2>

        <label>Email</label>
        <input
          type="email"
          placeholder="ex. email@gmail.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <label>Password</label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <button type="submit" disabled={isLoading}>
          {isLoading ? "Logging in..." : "Log in"}
        </button>

        {error && <div className="error">{error}</div>}
      </form>
    </div>
  );
};

export default Login;
