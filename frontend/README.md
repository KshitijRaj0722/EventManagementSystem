# EventHub — Frontend (React + Vite)

Single-page React app for the EventHub event management platform. Talks to the Spring Boot REST API and stores the JWT in `localStorage`.

## Tech stack
- React 18 + Vite 5
- React Router 6 (client-side routing)
- Axios (with a JWT request interceptor)
- Context API for auth state

## Features
- Browse, **search & filter** events (text, location, category, date)
- Event detail with speakers, register / cancel registration
- Sign up & log in (JWT)
- "My Registrations" dashboard with attendance status
- Admin area: create/edit/delete events, manage speakers, view & mark attendance
- Role-aware navigation and protected routes

## Configure
Create a `.env` (copy from `.env.example`) and point it at your backend:

```
VITE_API_URL=http://localhost:8081
```

## Run locally
```bash
npm install
npm run dev          # http://localhost:5173
```

Make sure the backend is running and `VITE_API_URL` matches its URL.
Demo logins (seeded by the backend):
- Admin — `admin@eventhub.local` / `admin123`
- User — `user@eventhub.local` / `user123`

## Build
```bash
npm run build        # outputs static site to dist/
npm run preview      # preview the production build
```

## Deployment (Vercel / Netlify)
- **Vercel** — import the repo, framework preset **Vite**, set env var `VITE_API_URL` to your deployed backend. `vercel.json` provides the SPA fallback.
- **Netlify** — build command `npm run build`, publish directory `dist`, set `VITE_API_URL`. `netlify.toml` provides the SPA redirect.

> Remember to add your deployed frontend URL to the backend's `CORS_ORIGINS`.
