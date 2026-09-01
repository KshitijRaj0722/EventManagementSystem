import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import client from '../../api/client.js';
import { apiError } from '../../utils/format.js';

const EMPTY = { name: '', expertise: '', bio: '' };

export default function AdminSpeakers() {
  const [speakers, setSpeakers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [form, setForm] = useState(EMPTY);
  const [editingId, setEditingId] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  function load() {
    setLoading(true);
    client
      .get('/api/speakers')
      .then((res) => setSpeakers(res.data))
      .catch((err) => setError(apiError(err, 'Could not load speakers')))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
  }, []);

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value });
  }

  function startEdit(s) {
    setEditingId(s.id);
    setForm({ name: s.name, expertise: s.expertise || '', bio: s.bio || '' });
  }

  function resetForm() {
    setEditingId(null);
    setForm(EMPTY);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      if (editingId) {
        await client.put(`/api/admin/speakers/${editingId}`, form);
      } else {
        await client.post('/api/admin/speakers', form);
      }
      resetForm();
      load();
    } catch (err) {
      setError(apiError(err, 'Could not save speaker'));
    } finally {
      setSubmitting(false);
    }
  }

  async function remove(id, name) {
    if (!window.confirm(`Delete speaker "${name}"?`)) return;
    setError('');
    try {
      await client.delete(`/api/admin/speakers/${id}`);
      load();
    } catch (err) {
      setError(apiError(err, 'Could not delete speaker'));
    }
  }

  return (
    <div>
      <div className="page-head">
        <h1>Manage Speakers</h1>
        <Link to="/admin/events" className="btn btn-outline">
          ← Back to Events
        </Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) 320px', gap: 20 }}>
        <div>
          {loading ? (
            <div className="spinner">Loading…</div>
          ) : speakers.length === 0 ? (
            <div className="empty">No speakers yet.</div>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Expertise</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {speakers.map((s) => (
                  <tr key={s.id}>
                    <td>
                      <strong>{s.name}</strong>
                      {s.bio ? <div className="muted" style={{ fontSize: '0.82rem' }}>{s.bio}</div> : null}
                    </td>
                    <td>{s.expertise}</td>
                    <td style={{ display: 'flex', gap: 6 }}>
                      <button className="btn btn-sm btn-outline" onClick={() => startEdit(s)}>
                        Edit
                      </button>
                      <button className="btn btn-sm btn-danger" onClick={() => remove(s.id, s.name)}>
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <form className="form card" onSubmit={handleSubmit} style={{ alignSelf: 'start' }}>
          <h3 style={{ margin: 0 }}>{editingId ? 'Edit speaker' : 'Add speaker'}</h3>
          <div className="field">
            <label>Name</label>
            <input value={form.name} onChange={update('name')} required />
          </div>
          <div className="field">
            <label>Expertise</label>
            <input value={form.expertise} onChange={update('expertise')} />
          </div>
          <div className="field">
            <label>Bio</label>
            <textarea rows={3} value={form.bio} onChange={update('bio')} />
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn" type="submit" disabled={submitting}>
              {submitting ? 'Saving…' : editingId ? 'Update' : 'Add'}
            </button>
            {editingId && (
              <button className="btn btn-ghost" type="button" onClick={resetForm}>
                Cancel
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
}
