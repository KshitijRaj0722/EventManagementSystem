import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import client from '../../api/client.js';
import { formatDateTime, apiError } from '../../utils/format.js';

export default function AdminAttendance() {
  const { id } = useParams();
  const [event, setEvent] = useState(null);
  const [registrations, setRegistrations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  function load() {
    setLoading(true);
    Promise.all([
      client.get(`/api/events/${id}`),
      client.get(`/api/admin/events/${id}/registrations`),
    ])
      .then(([ev, regs]) => {
        setEvent(ev.data);
        setRegistrations(regs.data);
      })
      .catch((err) => setError(apiError(err, 'Could not load attendance')))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  async function toggle(reg) {
    setError('');
    try {
      const res = await client.put(
        `/api/admin/registrations/${reg.id}/attendance`,
        null,
        { params: { attended: !reg.attended } },
      );
      setRegistrations((prev) => prev.map((r) => (r.id === reg.id ? res.data : r)));
    } catch (err) {
      setError(apiError(err, 'Could not update attendance'));
    }
  }

  if (loading) return <div className="spinner">Loading…</div>;

  const attendedCount = registrations.filter((r) => r.attended).length;

  return (
    <div>
      <div className="page-head">
        <div>
          <Link to="/admin/events" className="muted">
            ← Back to events
          </Link>
          <h1 style={{ marginBottom: 0 }}>Attendance</h1>
          {event && (
            <span className="muted">
              {event.title} · {formatDateTime(event.dateTime)}
            </span>
          )}
        </div>
        <span className="badge">
          {attendedCount} / {registrations.length} attended
        </span>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {registrations.length === 0 ? (
        <div className="empty">No one has registered for this event yet.</div>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Attendee</th>
              <th>Email</th>
              <th>Registered</th>
              <th>Attended</th>
            </tr>
          </thead>
          <tbody>
            {registrations.map((r) => (
              <tr key={r.id}>
                <td>{r.userName}</td>
                <td>{r.userEmail}</td>
                <td>{formatDateTime(r.registeredAt)}</td>
                <td>
                  <label style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <input
                      type="checkbox"
                      checked={r.attended}
                      onChange={() => toggle(r)}
                    />
                    {r.attended ? (
                      <span className="badge badge-success">Present</span>
                    ) : (
                      <span className="badge badge-muted">Absent</span>
                    )}
                  </label>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
