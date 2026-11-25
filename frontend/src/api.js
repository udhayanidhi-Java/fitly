const TOKEN_KEY = 'fitly_token'
const NAME_KEY = 'fitly_name'

export const getToken = () => localStorage.getItem(TOKEN_KEY)
export const getName = () => localStorage.getItem(NAME_KEY)

export function saveSession(data) {
  localStorage.setItem(TOKEN_KEY, data.token)
  localStorage.setItem(NAME_KEY, data.name)
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(NAME_KEY)
}

export async function api(path, { method = 'GET', body } = {}) {
  const headers = {}
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`

  const isForm = body instanceof FormData
  if (body && !isForm) headers['Content-Type'] = 'application/json'

  const res = await fetch(path, {
    method,
    headers,
    body: body ? (isForm ? body : JSON.stringify(body)) : undefined,
  })
  const data = await res.json().catch(() => ({}))

  if (res.status === 401) {
    clearSession()
    const err = new Error('Session expired. Please log in again.')
    err.status = 401
    throw err
  }
  if (!res.ok) {
    throw new Error(data.error || Object.values(data).join(', ') || 'Something went wrong')
  }
  return data
}