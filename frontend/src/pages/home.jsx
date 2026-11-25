import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, clearSession, getName } from '../api'

function Tags({ csv, kind }) {
  const items = csv ? csv.split(',').map((s) => s.trim()).filter(Boolean) : []
  if (items.length === 0) return <p className="muted">None</p>
  return (
    <div className="tags">
      {items.map((s) => (
        <span key={s} className={`tag ${kind}`}>{s}</span>
      ))}
    </div>
  )
}

export default function Home() {
  const navigate = useNavigate()
  const [file, setFile] = useState(null)
  const [title, setTitle] = useState('')
  const [company, setCompany] = useState('')
  const [description, setDescription] = useState('')
  const [result, setResult] = useState(null)
  const [history, setHistory] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function logout() {
    clearSession()
    navigate('/login')
  }

  async function handleMatch(e) {
    e.preventDefault()
    setError('')
    setResult(null)
    if (!file) return setError('Please choose a PDF resume.')
    setLoading(true)
    try {
      const form = new FormData()
      form.append('file', file)
      const resume = await api('/api/resumes', { method: 'POST', body: form })
      const job = await api('/api/jobs', { method: 'POST', body: { title, company, description } })
      const match = await api('/api/matches', { method: 'POST', body: { resumeId: resume.id, jobId: job.id } })
      setResult(match)
      setHistory(await api(`/api/matches/resume/${resume.id}`))
    } catch (err) {
      if (err.status === 401) navigate('/login')
      else setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <nav className="navbar">
        <div className="nav-inner">
          <span className="brand">Fitly</span>
          <div className="nav-right">
            <span>Hi, {getName()}</span>
            <button className="btn btn-ghost" onClick={logout}>Log out</button>
          </div>
        </div>
      </nav>

      <div className="container">
        <h2>Check your resume match</h2>
        <p className="muted" style={{ marginBottom: 20 }}>
          Upload your resume and paste a job description to see how well they fit.
        </p>

        <div className="grid-2">
          <form className="card" onSubmit={handleMatch}>
            <div className="field">
              <label>Your resume (PDF, max 5 MB)</label>
              <div className="file-box">
                <input type="file" accept="application/pdf" onChange={(e) => setFile(e.target.files[0])} />
              </div>
            </div>
            <div className="field">
              <label>Job title</label>
              <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} maxLength={200} required />
            </div>
            <div className="field">
              <label>Company</label>
              <input type="text" value={company} onChange={(e) => setCompany(e.target.value)} maxLength={200} />
            </div>
            <div className="field">
              <label>Job description</label>
              <textarea value={description} onChange={(e) => setDescription(e.target.value)} maxLength={20000} required />
            </div>
            {error && <div className="alert-error">{error}</div>}
            <button className="btn btn-primary" disabled={loading}>
              {loading ? 'Matching...' : 'Match my resume'}
            </button>
          </form>

          <div>
            <div className="card result">
              {!result ? (
                <p className="muted">Your score will show up here.</p>
              ) : (
                <>
                  <h3>{result.jobTitle}{result.company ? ` at ${result.company}` : ''}</h3>
                  <div className="score-ring" style={{ '--score': result.score }}>
                    <span>{result.score}%</span>
                  </div>
                  <strong>Matched skills</strong>
                  <Tags csv={result.matchedSkills} kind="tag-ok" />
                  <strong>Missing skills</strong>
                  <Tags csv={result.missingSkills} kind="tag-miss" />
                </>
              )}
            </div>

            {history.length > 0 && (
              <div className="card">
                <h3>Matches for this resume</h3>
                {history.map((h) => (
                  <div className="history-row" key={h.id}>
                    <span>{h.jobTitle}{h.company ? ` - ${h.company}` : ''}</span>
                    <strong>{h.score}%</strong>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  )
}