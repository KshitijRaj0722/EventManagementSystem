import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import client from '../api/client.js';
import { useAuth } from '../context/AuthContext.jsx';
import { formatDateTime, apiError } from '../utils/format.js';

export default function Dashboard() {
  const { user } = useAuth();
  const [registrations, setRegistrations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  function load() {
    setLoading(true);
    client
      .get('/api/registrations/me')
      .then((res) => setRegistrations(res.data))
      .catch((err) => setError(apiError(err, 'Could not load your registrations')))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
  }, []);

  async function cancel(eventId) {
    setError('');
    try {
      await client.delete(`/api/events/${eventId}/register`);
      load();
    } catch (err) {
      setError(apiError(err, 'Could not cancel'));
    }
  }

  return (
    <div>
      <div className="page-head">
        <h1>My Registrations</h1>
        <span className="muted">{user.name}</span>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="spinner">Loading…</div>
      ) : registrations.length === 0 ? (
        <div className="empty">
          You haven't registered for any events yet. <Link to="/">Browse events →</Link>
        </div>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Event</th>
              <th>When</th>
              <th>Venue</th>
              <th>Attendance</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {registrations.map((r) => (
              <tr key={r.id}>
                <td>
                  <Link to={`/events/${r.eventId}`}>{r.eventTitle}</Link>
                </td>
                <td>{formatDateTime(r.eventDateTime)}</td>
                <td>{r.venue}</td>
                <td>
                  {r.attended ? (
                    <span className="badge badge-success">Attended</span>
                  ) : (
                    <span className="badge badge-muted">Registered</span>
                  )}
                </td>
                <td>
                  <button className="btn btn-sm btn-danger" onClick={() => cancel(r.eventId)}>
                    Cancel
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
