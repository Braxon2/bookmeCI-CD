import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import "./styles/Register.css";
import { useNavigate } from "react-router-dom";

const Register = () => {
  const [firstname, setFirstname] = useState("");
  const [lastname, setLastname] = useState("");
  const [phonenumber, setPhonenumber] = useState("");
  const [userType, setUserType] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [data, setData] = useState(null);

  const navigate = useNavigate();

  useEffect(() => {
    if (data) {
      navigate("/login");
    }
  }, [data, navigate]);
  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      const res = await fetch("http://localhost:8080/auth/register", {
        method: "POST",
        headers: { "Content-type": "application/json" },
        body: JSON.stringify({
          firstName: firstname,
          lastName: lastname,
          phoneNumber: phonenumber,
          userType,
          email,
          password,
        }),
      });

      if (!res.ok) {
        throw new Error(json.error || "Reigteration failed");
      }

      const json = await res.json();

      if (json.ok) {
        setError(null);
        setData(json);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };
  return (
    <div className="register-container">
      <form className="register-form" onSubmit={handleSubmit}>
        <h2>REGISTER</h2>

        <p>First Name:</p>

        <input
          type="text"
          name="firstname"
          id="firstname"
          value={firstname}
          onChange={(e) => setFirstname(e.target.value)}
        />

        <p>Last Name:</p>

        <input
          type="text"
          name="lastname"
          id="lastname"
          value={lastname}
          onChange={(e) => setLastname(e.target.value)}
        />

        <p>Phone Number:</p>

        <input
          type="tel"
          name="phonenumber"
          id="phonenumber"
          value={phonenumber}
          onChange={(e) => setPhonenumber(e.target.value)}
        />

        <p>Register as:</p>

        <select
          name="type"
          id="type"
          value={userType}
          onChange={(e) => setUserType(e.target.value)}
        >
          <option value="USER">User</option>
          <option value="OWNER">Owner</option>
        </select>

        <p>Email:</p>

        <input
          type="email"
          name="email"
          id="email"
          placeholder="ex. email@gmail.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <p>Password</p>

        <input
          type="password"
          name="password"
          id="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        {error && <div className="error">{error}</div>}
        <button type="submit">Register</button>
      </form>
    </div>
  );
};

export default Register;
