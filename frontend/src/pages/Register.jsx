import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Button from '../components/Button'
import Input from '../components/Input'
import Alert from '../components/Alert'

export default function Register() {
  const { register, loading } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    fullName: '',
    phoneNumber: '',
  })
  const [error, setError] = useState('')

  const handleChange = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await register(form)
      navigate('/dashboard')
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="min-h-screen bg-navy-950 flex items-center justify-center px-6 py-12">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-2xl font-bold text-white tracking-tight">Open your account</h1>
          <p className="text-navy-300 text-sm mt-1">Takes less than a minute</p>
        </div>

        <div className="bg-white rounded-2xl p-8 shadow-xl">
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && <Alert type="error">{error}</Alert>}

            <Input
              label="Full name"
              required
              value={form.fullName}
              onChange={handleChange('fullName')}
              placeholder="Varun Jayaram"
            />
            <Input
              label="Username"
              required
              minLength={4}
              value={form.username}
              onChange={handleChange('username')}
              placeholder="varunj"
            />
            <Input
              label="Email"
              type="email"
              required
              value={form.email}
              onChange={handleChange('email')}
              placeholder="you@example.com"
            />
            <Input
              label="Phone number"
              value={form.phoneNumber}
              onChange={handleChange('phoneNumber')}
              placeholder="9876543210"
            />
            <Input
              label="Password"
              type="password"
              required
              minLength={8}
              value={form.password}
              onChange={handleChange('password')}
              placeholder="At least 8 characters, with a number"
              autoComplete="new-password"
            />

            <Button type="submit" className="w-full" loading={loading} size="lg">
              Create account
            </Button>
          </form>

          <p className="text-center text-sm text-navy-500 mt-6">
            Already have an account?{' '}
            <Link to="/login" className="font-semibold text-mint-600 hover:text-mint-700">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
