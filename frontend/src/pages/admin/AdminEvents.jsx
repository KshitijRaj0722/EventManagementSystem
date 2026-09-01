import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import client from '../../api/client.js';
import { formatDateTime, apiError } from '../../utils/format.js';

export default function AdminEvents() {
  const navigate = useNavigate();
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  function load() {
    setLoading(true);
    client
      .get('/api/events')
      .then((res) => setEvents(res.data))
      .catch((err) => setError(apiError(err, 'Could not load events')))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
  }, []);

  async function remove(id, title) {
    if (!window.confirm(`Delete "${title}"? This removes its registrations too.`)) return;
    setError('');
    try {
      await client.delete(`/api/admin/events/${id}`);
      load();
    } catch (err) {
      setError(apiError(err, 'Could not delete event'));
    }
  }

  return (
    <div>
      <div className="page-head">
        <h1>Manage Events</h1>
        <div style={{ display: 'flex', gap: 10 }}>
          <Link to="/admin/speakers" className="btn btn-outline">
            Manage Speakers
          </Link>
          <button className="btn" onClick={() => navigate('/admin/events/new')}>
            + New Event
          </button>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="spinner">Loading…</div>
      ) : events.length === 0 ? (
        <div className="empty">No events yet. Create your first one.</div>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Title</th>
              <th>When</th>
              <th>Location</th>
              <th>Category</th>
              <th>Registered</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {events.map((e) => (
              <tr key={e.id}>
                <td>
                  <Link to={`/events/${e.id}`}>{e.title}</Link>
                </td>
                <td>{formatDateTime(e.dateTime)}</td>
                <td>{e.location}</td>
                <td>{e.category}</td>
                <td>
                  {e.registeredCount}
                  {e.capacity != null ? ` / ${e.capacity}` : ''}
                </td>
                <td style={{ display: 'flex', gap: 6, whiteSpace: 'nowrap' }}>
                  <Link className="btn btn-sm btn-ghost" to={`/admin/events/${e.id}/attendance`}>
                    Attendance
                  </Link>
                  <Link className="btn btn-sm btn-outline" to={`/admin/events/${e.id}/edit`}>
                    Edit
                  </Link>
                  <button className="btn btn-sm btn-danger" onClick={() => remove(e.id, e.title)}>
                    Delete
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
