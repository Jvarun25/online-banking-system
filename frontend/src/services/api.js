import axios from 'axios'

// In dev, Vite proxies /api/* to the Spring Boot backend (see vite.config.js).
// In production, set VITE_API_BASE_URL to the deployed backend's URL.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
})

// Attach the JWT to every outgoing request, if we have one stored.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('nb_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// If the backend ever responds 401 (expired/invalid token), clear the
// session and bounce to login rather than leaving the user stuck.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('nb_token')
      localStorage.removeItem('nb_user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
