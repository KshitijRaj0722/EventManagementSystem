# EventHub — Event Management System

A full-stack event management platform. Admins create and manage events and speakers;
users browse, search and register for events and receive email confirmations and reminders.

```
.
├── backend/    Spring Boot 3.2 REST API (Java 17, JPA, Spring Security + JWT, MySQL)
└── frontend/   React 18 + Vite SPA (React Router, Axios, Context API)
```

Each folder has its own README with setup, configuration and deployment details.

## Features
- **Authentication** — sign up / log in with JWT, BCrypt-hashed passwords, `USER` / `ADMIN` roles
- **Event browsing** — upcoming events with date, venue, description and speakers
- **Search & filters** — by text, location, category and date
- **Registration** — register / cancel, capacity limits, email confirmation
- **Admin** — CRUD events, manage speakers, view attendees, mark attendance
- **Notifications** — confirmation emails plus an hourly scheduler that reminds
  attendees of events happening in the next 24 hours

## Tech stack
| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2.5, Spring Web, Spring Data JPA, Spring Security, JavaMailSender |
| Auth | JWT (jjwt 0.12) — stateless, role-based |
| Database | MySQL 8 (local) · PostgreSQL (production) · H2 (tests) |
| Frontend | React 18, Vite 5, React Router 6, Axios |
| Tests | JUnit 5 + Mockito — 72 tests across controller, service and repository layers |
| Deployment | Backend: Docker → Render · Frontend: Vercel / Netlify |

## Quick start

Requires **Java 17**, **Node 18+** and a running **MySQL** (the `eventhubdb` database is created automatically).

```bash
# 1. Backend — http://localhost:8081
cd backend
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
# edit that file with your MySQL username/password
./mvnw spring-boot:run
```

```bash
# 2. Frontend — http://localhost:5173
cd frontend
cp .env.example .env       # VITE_API_URL=http://localhost:8081
npm install
npm run dev
```

On first run the backend seeds demo data:

| Role | Email | Password |
|---|---|---|
| Admin | `admin@eventhub.local` | `admin123` |
| User | `user@eventhub.local` | `user123` |

…plus 3 sample speakers and 3 sample events.

## Tests

```bash
cd backend
./mvnw test        # 72 tests, in-memory H2 — no MySQL needed
```

- **Controller** — `@WebMvcTest` + Mockito: routing, param binding, validation, error
  translation and role rules (401 anonymous / 403 wrong role)
- **Service** — Mockito unit tests: duplicate email, duplicate registration, capacity
  limits, attendance, confirmation email
- **Repository** — `@DataJpaTest`: search/filter query, reminder window, DB constraints

## Deployment
- **Backend** → Render. The `render.yaml` Blueprint at the repo root provisions a
  PostgreSQL database and the Dockerized API (`backend/Dockerfile`) and wires the
  `DB_*` vars automatically. After the frontend is live, set `CORS_ORIGINS` to its
  URL. Health check: `/actuator/health`.
- **Frontend** → Vercel or Netlify. **Set the project's root directory to `frontend`**
  (this is a monorepo), and set `VITE_API_URL` to the deployed backend URL.
  SPA fallbacks come from `frontend/vercel.json` / `frontend/netlify.toml`.

The backend must be deployed first, so you have its URL for `VITE_API_URL`; then set
`CORS_ORIGINS` on the backend to the frontend's URL.
