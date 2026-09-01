import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import client from '../api/client.js';
import { useAuth } from '../context/AuthContext.jsx';
import { formatDateTime, apiError } from '../utils/format.js';

export default function EventDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [registered, setRegistered] = useState(false);
  const [working, setWorking] = useState(false);

  function loadEvent() {
    client
      .get(`/api/events/${id}`)
      .then((res) => setEvent(res.data))
      .catch((err) => setError(apiError(err, 'Event not found')))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadEvent();
    // If logged in, check whether the user is already registered.
    if (isAuthenticated) {
      client
        .get('/api/registrations/me')
        .then((res) => setRegistered(res.data.some((r) => String(r.eventId) === String(id))))
        .catch(() => {});
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, isAuthenticated]);

  async function handleRegister() {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: { pathname: `/events/${id}` } } });
      return;
    }
    setWorking(true);
    setError('');
    setMessage('');
    try {
      await client.post(`/api/events/${id}/register`);
      setRegistered(true);
      setMessage('You are registered! A confirmation email has been sent.');
      loadEvent();
    } catch (err) {
      setError(apiError(err, 'Could not register'));
    } finally {
      setWorking(false);
    }
  }

  async function handleCancel() {
    setWorking(true);
    setError('');
    setMessage('');
    try {
      await client.delete(`/api/events/${id}/register`);
      setRegistered(false);
      setMessage('Your registration has been cancelled.');
      loadEvent();
    } catch (err) {
      setError(apiError(err, 'Could not cancel'));
    } finally {
      setWorking(false);
    }
  }

  if (loading) return <div className="spinner">Loading…</div>;
  if (!event) return <div className="alert alert-error">{error || 'Event not found'}</div>;

  const full = event.capacity != null && event.registeredCount >= event.capacity;

  return (
    <div>
      <Link to="/" className="muted">
        ← Back to events
      </Link>
      <div className="card" style={{ marginTop: 14 }}>
        <span className="badge">{event.category}</span>
        <h1 style={{ marginTop: 12 }}>{event.title}</h1>
        <div className="event-meta" style={{ fontSize: '0.95rem', marginBottom: 14 }}>
          <span>📅 {formatDateTime(event.dateTime)}</span>
          <span>📍 {event.venue}, {event.location}</span>
          <span>
            👥 {event.registeredCount} registered
            {event.capacity != null ? ` / ${event.capacity} capacity` : ''}
          </span>
        </div>

        <p>{event.description}</p>

        {event.speakers?.length > 0 && (
          <>
            <h3>Speakers</h3>
            <div className="checkbox-row">
              {event.speakers.map((s) => (
                <div key={s.id} className="speaker-chip">
                  🎤 <strong>{s.name}</strong>
                  {s.expertise ? <span className="muted">· {s.expertise}</span> : null}
                </div>
              ))}
            </div>
          </>
        )}

        {message && <div className="alert alert-success" style={{ marginTop: 16 }}>{message}</div>}
        {error && <div className="alert alert-error" style={{ marginTop: 16 }}>{error}</div>}

        <div style={{ marginTop: 20 }}>
          {registered ? (
            <button className="btn btn-danger" onClick={handleCancel} disabled={working}>
              {working ? 'Working…' : 'Cancel registration'}
            </button>
          ) : (
            <button className="btn" onClick={handleRegister} disabled={working || full}>
              {full ? 'Event full' : working ? 'Registering…' : 'Register for this event'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
