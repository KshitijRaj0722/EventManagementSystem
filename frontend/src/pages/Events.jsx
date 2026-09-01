import { useEffect, useMemo, useState } from 'react';
import client from '../api/client.js';
import EventCard from '../components/EventCard.jsx';
import { apiError } from '../utils/format.js';

export default function Events() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [search, setSearch] = useState('');
  const [location, setLocation] = useState('');
  const [category, setCategory] = useState('');
  const [fromDate, setFromDate] = useState('');

  function load(params = {}) {
    setLoading(true);
    setError('');
    client
      .get('/api/events', { params })
      .then((res) => setEvents(res.data))
      .catch((err) => setError(apiError(err, 'Could not load events')))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
  }, []);

  // Distinct locations & categories from the loaded set, for filter dropdowns.
  const locations = useMemo(
    () => [...new Set(events.map((e) => e.location))].sort(),
    [events],
  );
  const categories = useMemo(
    () => [...new Set(events.map((e) => e.category))].sort(),
    [events],
  );

  function applyFilters(e) {
    e.preventDefault();
    const params = {};
    if (search.trim()) params.search = search.trim();
    if (location) params.location = location;
    if (category) params.category = category;
    if (fromDate) params.fromDate = fromDate;
    load(params);
  }

  function clearFilters() {
    setSearch('');
    setLocation('');
    setCategory('');
    setFromDate('');
    load();
  }

  return (
    <div>
      <div className="hero">
        <h1>Discover upcoming events</h1>
        <p>Browse, search and register for events — talks, workshops and meetups.</p>
      </div>

      <form className="toolbar" onSubmit={applyFilters}>
        <div className="field" style={{ flex: 2 }}>
          <label htmlFor="search">Search</label>
          <input
            id="search"
            placeholder="Title or keyword…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="field">
          <label htmlFor="location">Location</label>
          <select id="location" value={location} onChange={(e) => setLocation(e.target.value)}>
            <option value="">All</option>
            {locations.map((l) => (
              <option key={l} value={l}>
                {l}
              </option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="category">Category</label>
          <select id="category" value={category} onChange={(e) => setCategory(e.target.value)}>
            <option value="">All</option>
            {categories.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="fromDate">From date</label>
          <input
            id="fromDate"
            type="date"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
          />
        </div>
        <button className="btn" type="submit">
          Search
        </button>
        <button className="btn btn-ghost" type="button" onClick={clearFilters}>
          Clear
        </button>
      </form>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="spinner">Loading events…</div>
      ) : events.length === 0 ? (
        <div className="empty">No events match your filters.</div>
      ) : (
        <div className="grid">
          {events.map((event) => (
            <EventCard key={event.id} event={event} />
          ))}
        </div>
      )}
    </div>
  );
}
