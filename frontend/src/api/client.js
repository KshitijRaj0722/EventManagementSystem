import axios from 'axios';

const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8081';

const client = axios.create({ baseURL });

// Attach JWT from localStorage to every request.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// On 401 (missing/expired token) clear the stale session and bounce to login —
// otherwise the cached user stays in React state and every action just errors out.
// 403 is left alone: that's a logged-in user hitting an admin-only endpoint.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      const hadSession = !!localStorage.getItem('token');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (hadSession && window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
    }
    return Promise.reject(error);
  },
);

export default client;
