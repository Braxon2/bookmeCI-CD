import "./styles/Navbar.css";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
const Navbar = () => {
  const { isAuthenticated, isOwner, isAdmin, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav>
      <div className="logo">
        <h1>BookMe</h1>
      </div>
      <ul className="navLinks">
        <Link to="/search">
          <li>Search</li>
        </Link>
        {isAuthenticated && !isAdmin && (
          <Link to="/profile">
            <li>Profile</li>
          </Link>
        )}

        {isOwner && (
          <Link to="/list-properties">
            <li>List your properties</li>
          </Link>
        )}
        {isOwner && (
          <Link to="/add-property">
            <li>Add Property</li>
          </Link>
        )}
        {isAuthenticated && (
          <Link to="/" onClick={handleLogout}>
            <li>Log out</li>
          </Link>
        )}
        {isAdmin && (
          <Link to="/fascilities">
            <li>Fascilities</li>
          </Link>
        )}

        {isAdmin && (
          <Link to="/unit-fascilities">
            <li>Unit Facilities</li>
          </Link>
        )}

        {isAdmin && (
          <Link to="/addons">
            <li>Addons</li>
          </Link>
        )}

        {!isAuthenticated && (
          <Link to="/login">
            <li>Login</li>
          </Link>
        )}

        {!isAuthenticated && (
          <Link to="/register">
            <li>Register</li>
          </Link>
        )}

        <div className="hamburger-menu">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </ul>
    </nav>
  );
};

export default Navbar;
