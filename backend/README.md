# EventHub — Backend (Spring Boot REST API)

REST API for the EventHub event management platform. Built with Spring Boot 3.2, Spring Security (JWT), Spring Data JPA and MySQL. Sends email confirmations/reminders via JavaMailSender.

## Tech stack
- Java 17, Spring Boot 3.2.5
- Spring Web · Spring Data JPA · Spring Security + **JWT** (jjwt 0.12)
- MySQL 8 (runtime) · H2 (tests)
- JavaMailSender (email notifications)
- JUnit 5 + Mockito (72 tests across controller, service & repository layers)

## Features
- User registration & login (JWT, BCrypt, roles `USER` / `ADMIN`)
- Browse / search / filter events (by text, location, category, date)
- Register & cancel registration for events (email confirmation)
- Admin: CRUD events, manage speakers, view attendees, mark attendance
- Hourly reminder scheduler for events within the next 24 hours

## Run locally
Requires Java 17 and a running MySQL (a database `eventhubdb` is auto-created).

```bash
# from backend/
./mvnw spring-boot:run
# or
./mvnw clean package -DskipTests && java -jar target/event-management-system-1.0.0.jar
```

App starts on `http://localhost:8081` (override with `PORT`). On first run it seeds:
- `admin@eventhub.local` / `admin123` (ADMIN)
- `user@eventhub.local` / `user123` (USER)
- 3 sample speakers and 3 sample events

## Run tests
```bash
./mvnw test     # uses in-memory H2, no MySQL needed
```

72 tests covering all three layers:
- **Controller** — `@WebMvcTest` + Mockito: request mapping, param binding, validation, error translation and the role rules from `SecurityConfig` (401 anonymous / 403 wrong role).
- **Service** — `@ExtendWith(MockitoExtension.class)`: business rules (duplicate email, duplicate registration, capacity limits, attendance, confirmation email).
- **Repository** — `@DataJpaTest` on H2: the search/filter query, the reminder window lookup and the DB constraints (unique email, one registration per user+event).

## Configuration (environment variables)
| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `8081` | HTTP port |
| `DB_URL` | local MySQL `eventhubdb` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / *(local)* | DB credentials |
| `JWT_SECRET` | dev placeholder | HMAC signing secret (set a long random value in prod) |
| `JWT_EXPIRATION_MS` | `86400000` | Token lifetime (24h) |
| `CORS_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Allowed frontend origins (comma-separated) |
| `MAIL_ENABLED` | `false` | When true, actually sends SMTP email |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | — | SMTP credentials |

## API overview
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | public | Sign up, returns JWT |
| POST | `/api/auth/login` | public | Log in, returns JWT |
| GET | `/api/auth/me` | user | Current user |
| GET | `/api/events` | public | List/search events (`?search=&location=&category=&fromDate=`) |
| GET | `/api/events/{id}` | public | Event detail |
| GET | `/api/speakers` | public | List speakers |
| POST | `/api/events/{id}/register` | user | Register for event (+email) |
| DELETE | `/api/events/{id}/register` | user | Cancel registration |
| GET | `/api/registrations/me` | user | My registrations |
| POST/PUT/DELETE | `/api/admin/events/**` | admin | Manage events |
| POST/PUT/DELETE | `/api/admin/speakers/**` | admin | Manage speakers |
| GET | `/api/admin/events/{id}/registrations` | admin | Event attendees |
| PUT | `/api/admin/registrations/{id}/attendance?attended=` | admin | Mark attendance |

## Deployment (Render)
This folder holds the `Dockerfile`; the Render Blueprint lives at the **repository root**
(`../render.yaml`) because Render only looks for `render.yaml` there.

Easiest path — Render dashboard → **New → Blueprint** → pick this repo. That provisions the
PostgreSQL database and the web service together and injects `DB_HOST` / `DB_PORT` / `DB_NAME` /
`DB_USERNAME` / `DB_PASSWORD` plus a generated `JWT_SECRET`. The only value you set by hand is
`CORS_ORIGINS` (your deployed frontend URL).

To wire it up manually instead, create a **Web Service** with runtime *Docker*, Dockerfile path
`./backend/Dockerfile`, Docker context `./backend`, health check `/actuator/health`, and set
`DB_URL` (or `DB_HOST`/`DB_PORT`/`DB_NAME`), `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` and
`CORS_ORIGINS`.
