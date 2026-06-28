import { createContext, useContext, useState, useCallback } from 'react'
import { authService } from '../services'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('nb_user')
    return stored ? JSON.parse(stored) : null
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const persistSession = (authResponse) => {
    localStorage.setItem('nb_token', authResponse.token)
    localStorage.setItem('nb_user', JSON.stringify(authResponse))
    setUser(authResponse)
  }

  const login = useCallback(async (username, password) => {
    setLoading(true)
    setError(null)
    try {
      const { data } = await authService.login({ username, password })
      persistSession(data)
      return data
    } catch (err) {
      const message = err.response?.data?.message || 'Login failed. Please check your credentials.'
      setError(message)
      throw new Error(message)
    } finally {
      setLoading(false)
    }
  }, [])

  const register = useCallback(async (payload) => {
    setLoading(true)
    setError(null)
    try {
      const { data } = await authService.register(payload)
      persistSession(data)
      return data
    } catch (err) {
      const message = err.response?.data?.message || 'Registration failed. Please try again.'
      setError(message)
      throw new Error(message)
    } finally {
      setLoading(false)
    }
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('nb_token')
    localStorage.removeItem('nb_user')
    setUser(null)
  }, [])

  const isAdmin = user?.roles?.includes('ROLE_ADMIN')

  return (
    <AuthContext.Provider value={{ user, loading, error, login, register, logout, isAdmin }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
