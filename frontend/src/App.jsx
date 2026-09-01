import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import Events from './pages/Events.jsx';
import EventDetail from './pages/EventDetail.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Dashboard from './pages/Dashboard.jsx';
import AdminEvents from './pages/admin/AdminEvents.jsx';
import AdminEventForm from './pages/admin/AdminEventForm.jsx';
import AdminSpeakers from './pages/admin/AdminSpeakers.jsx';
import AdminAttendance from './pages/admin/AdminAttendance.jsx';

export default function App() {
  return (
    <>
      <Navbar />
      <div className="container">
        <Routes>
          <Route path="/" element={<Events />} />
          <Route path="/events/:id" element={<EventDetail />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />

          <Route
            path="/admin/events"
            element={
              <ProtectedRoute adminOnly>
                <AdminEvents />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/events/new"
            element={
              <ProtectedRoute adminOnly>
                <AdminEventForm />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/events/:id/edit"
            element={
              <ProtectedRoute adminOnly>
                <AdminEventForm />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/events/:id/attendance"
            element={
              <ProtectedRoute adminOnly>
                <AdminAttendance />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/speakers"
            element={
              <ProtectedRoute adminOnly>
                <AdminSpeakers />
              </ProtectedRoute>
            }
          />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </>
  );
}
