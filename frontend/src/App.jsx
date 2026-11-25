import { Navigate, Route, Routes } from 'react-router-dom'
import { getToken } from './api'
import Login from './pages/Login'
import Home from './pages/Home'

function Protected({ children }) {
  return getToken() ? children : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<Protected><Home /></Protected>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}