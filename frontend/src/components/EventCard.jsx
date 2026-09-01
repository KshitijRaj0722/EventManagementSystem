import { Link } from 'react-router-dom';
import { formatDateTime } from '../utils/format.js';

export default function EventCard({ event }) {
  const full = event.capacity != null && event.registeredCount >= event.capacity;
  return (
    <div className="card event-card">
      <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8 }}>
        <span className="badge">{event.category}</span>
        {full && <span className="badge badge-muted">Full</span>}
      </div>
      <h3>
        <Link to={`/events/${event.id}`}>{event.title}</Link>
      </h3>
      <div className="event-meta">
        <span>📅 {formatDateTime(event.dateTime)}</span>
        <span>📍 {event.venue}, {event.location}</span>
        {event.speakers?.length > 0 && (
          <span>🎤 {event.speakers.map((s) => s.name).join(', ')}</span>
        )}
      </div>
      <p className="desc">
        {event.description?.length > 110
          ? `${event.description.slice(0, 110)}…`
          : event.description}
      </p>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span className="muted" style={{ fontSize: '0.82rem' }}>
          {event.registeredCount} registered
          {event.capacity != null ? ` / ${event.capacity}` : ''}
        </span>
        <Link to={`/events/${event.id}`} className="btn btn-sm btn-outline">
          View details
        </Link>
      </div>
    </div>
  );
}
