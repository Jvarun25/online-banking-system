import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Button from '../components/Button'
import Input from '../components/Input'
import Alert from '../components/Alert'

export default function Login() {
  const { login, loading } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await login(form.username, form.password)
      navigate('/dashboard')
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="min-h-screen bg-navy-950 flex items-center justify-center px-6">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-12 h-12 rounded-xl bg-mint-500 flex items-center justify-center mx-auto mb-4">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M3 10L12 4L21 10" stroke="#0a1420" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M5 10V19H19V10" stroke="#0a1420" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              <line x1="9" y1="13" x2="9" y2="19" stroke="#0a1420" strokeWidth="2" strokeLinecap="round"/>
              <line x1="15" y1="13" x2="15" y2="19" stroke="#0a1420" strokeWidth="2" strokeLinecap="round"/>
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Welcome back</h1>
          <p className="text-navy-300 text-sm mt-1">Sign in to manage your accounts</p>
        </div>

        <div className="bg-white rounded-2xl p-8 shadow-xl">
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && <Alert type="error">{error}</Alert>}

            <Input
              label="Username"
              type="text"
              required
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              placeholder="e.g. admin"
              autoComplete="username"
            />
            <Input
              label="Password"
              type="password"
              required
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              placeholder="••••••••"
              autoComplete="current-password"
            />

            <Button type="submit" className="w-full" loading={loading} size="lg">
              Sign in
            </Button>
          </form>

          <p className="text-center text-sm text-navy-500 mt-6">
            New to NovaBank?{' '}
            <Link to="/register" className="font-semibold text-mint-600 hover:text-mint-700">
              Create an account
            </Link>
          </p>
        </div>

        <p className="text-center text-xs text-navy-500 mt-6">
          Demo admin login — username: <code className="text-navy-300">admin</code> · password:{' '}
          <code className="text-navy-300">Admin@123</code>
        </p>
      </div>
    </div>
  )
}
