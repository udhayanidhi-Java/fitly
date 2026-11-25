import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, saveSession } from '../api'

export default function Login() {
  const [mode, setMode] = useState('login')
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  async function submit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const body = mode === 'login' ? { email, password } : { name, email, password }
      const data = await api(`/api/auth/${mode}`, { method: 'POST', body })
      saveSession(data)
      navigate('/')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-brand">
        <h1>Fitly</h1>
        <p>Know how well your resume fits a job before you apply.</p>
        <ul>
          <li>Upload your resume as a PDF</li>
          <li>Paste any job description</li>
          <li>See your match score and missing skills</li>
        </ul>
      </div>

      <div className="auth-panel">
        <div className="auth-card">
          <div className="auth-tabs">
            <button type="button" className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>
              Log in
            </button>
            <button type="button" className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>
              Sign up
            </button>
          </div>

          <h2>{mode === 'login' ? 'Welcome back' : 'Create your account'}</h2>
          <p className="muted" style={{ marginBottom: 20 }}>
            {mode === 'login' ? 'Log in to see your matches.' : 'It takes less than a minute.'}
          </p>

          {error && <div className="alert-error">{error}</div>}

          <form onSubmit={submit}>
            {mode === 'register' && (
              <div className="field">
                <label>Name</label>
                <input type="text" value={name} onChange={(e) => setName(e.target.value)} required />
              </div>
            )}
            <div className="field">
              <label>Email</label>
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </div>
            <div className="field">
              <label>Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                minLength={mode === 'register' ? 8 : undefined}
                required
              />
              {mode === 'register' && <span className="muted">At least 8 characters</span>}
            </div>
            <button className="btn btn-primary" disabled={loading}>
              {loading ? 'Please wait...' : mode === 'login' ? 'Log in' : 'Sign up'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}