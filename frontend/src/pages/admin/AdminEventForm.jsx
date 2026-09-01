import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import client from '../../api/client.js';
import { toLocalDateInput, apiError } from '../../utils/format.js';

const EMPTY = {
  title: '',
  description: '',
  dateTime: '',
  venue: '',
  location: '',
  category: '',
  capacity: '',
  speakerIds: [],
};

export default function AdminEventForm() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [form, setForm] = useState(EMPTY);
  const [speakers, setSpeakers] = useState([]);
  const [loading, setLoading] = useState(isEdit);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    client.get('/api/speakers').then((res) => setSpeakers(res.data)).catch(() => {});
    if (isEdit) {
      client
        .get(`/api/events/${id}`)
        .then((res) => {
          const e = res.data;
          setForm({
            title: e.title,
            description: e.description || '',
            dateTime: toLocalDateInput(e.dateTime),
            venue: e.venue,
            location: e.location,
            category: e.category,
            capacity: e.capacity ?? '',
            speakerIds: e.speakers.map((s) => s.id),
          });
        })
        .catch((err) => setError(apiError(err, 'Could not load event')))
        .finally(() => setLoading(false));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value });
  }

  function toggleSpeaker(speakerId) {
    setForm((prev) => ({
      ...prev,
      speakerIds: prev.speakerIds.includes(speakerId)
        ? prev.speakerIds.filter((x) => x !== speakerId)
        : [...prev.speakerIds, speakerId],
    }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    const payload = {
      ...form,
      capacity: form.capacity === '' ? null : Number(form.capacity),
      dateTime: form.dateTime, // datetime-local string is ISO-compatible (yyyy-MM-ddThh:mm)
    };
    try {
      if (isEdit) {
        await client.put(`/api/admin/events/${id}`, payload);
      } else {
        await client.post('/api/admin/events', payload);
      }
      navigate('/admin/events');
    } catch (err) {
      setError(apiError(err, 'Could not save event'));
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <div className="spinner">Loading…</div>;

  return (
    <div>
      <div className="page-head">
        <h1>{isEdit ? 'Edit Event' : 'New Event'}</h1>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <form className="form card" onSubmit={handleSubmit}>
        <div className="field">
          <label>Title</label>
          <input value={form.title} onChange={update('title')} required />
        </div>
        <div className="field">
          <label>Description</label>
          <textarea rows={4} value={form.description} onChange={update('description')} />
        </div>
        <div className="row">
          <div className="field">
            <label>Date &amp; time</label>
            <input
              type="datetime-local"
              value={form.dateTime}
              onChange={update('dateTime')}
              required
            />
          </div>
          <div className="field">
            <label>Capacity (optional)</label>
            <input type="number" min="1" value={form.capacity} onChange={update('capacity')} />
          </div>
        </div>
        <div className="row">
          <div className="field">
            <label>Venue</label>
            <input value={form.venue} onChange={update('venue')} required />
          </div>
          <div className="field">
            <label>Location (city)</label>
            <input value={form.location} onChange={update('location')} required />
          </div>
          <div className="field">
            <label>Category</label>
            <input value={form.category} onChange={update('category')} required />
          </div>
        </div>

        <div className="field">
          <label>Speakers</label>
          {speakers.length === 0 ? (
            <span className="muted">No speakers yet — add some under Manage Speakers.</span>
          ) : (
            <div className="checkbox-row">
              {speakers.map((s) => (
                <label key={s.id}>
                  <input
                    type="checkbox"
                    checked={form.speakerIds.includes(s.id)}
                    onChange={() => toggleSpeaker(s.id)}
                  />
                  {s.name}
                </label>
              ))}
            </div>
          )}
        </div>

        <div style={{ display: 'flex', gap: 10 }}>
          <button className="btn" type="submit" disabled={submitting}>
            {submitting ? 'Saving…' : isEdit ? 'Update event' : 'Create event'}
          </button>
          <button
            className="btn btn-ghost"
            type="button"
            onClick={() => navigate('/admin/events')}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
