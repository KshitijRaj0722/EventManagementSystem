import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Navbar() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="brand">
          🎟️ EventHub
        </Link>
        <div className="nav-links">
          <Link to="/">Events</Link>
          {isAuthenticated && <Link to="/dashboard">My Registrations</Link>}
          {isAdmin && <Link to="/admin/events">Admin</Link>}
          {isAuthenticated ? (
            <>
              <span className="nav-user">Hi, {user.name}</span>
              <button className="btn btn-ghost btn-sm" onClick={handleLogout}>
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login">Login</Link>
              <Link to="/register" className="btn btn-sm">
                Sign up
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
