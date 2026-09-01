import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { apiError } from '../utils/format.js';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/';

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await login(email, password);
      navigate(from, { replace: true });
    } catch (err) {
      setError(apiError(err, 'Login failed'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-wrap card">
      <h1>Welcome back</h1>
      <p className="muted">Log in to register for events.</p>
      {error && <div className="alert alert-error">{error}</div>}
      <form className="form" onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            autoComplete="email"
          />
        </div>
        <div className="field">
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            autoComplete="current-password"
          />
        </div>
        <button className="btn" type="submit" disabled={submitting}>
          {submitting ? 'Logging in…' : 'Log in'}
        </button>
      </form>
      <p className="muted" style={{ marginTop: 16 }}>
        No account? <Link to="/register">Sign up</Link>
      </p>
      <p className="muted" style={{ fontSize: '0.82rem' }}>
        Demo: admin@eventhub.local / admin123 · user@eventhub.local / user123
      </p>
    </div>
  );
}
