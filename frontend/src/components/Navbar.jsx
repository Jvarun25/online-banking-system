import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const linkClass = (path) =>
    `px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
      location.pathname === path
        ? 'bg-mint-500/15 text-mint-600'
        : 'text-navy-200 hover:text-white hover:bg-white/5'
    }`

  return (
    <nav className="bg-navy-950 border-b border-navy-800/60">
      <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
        <Link to="/dashboard" className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-mint-500 flex items-center justify-center">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
              <path d="M3 10L12 4L21 10" stroke="#0a1420" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M5 10V19H19V10" stroke="#0a1420" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              <line x1="9" y1="13" x2="9" y2="19" stroke="#0a1420" strokeWidth="2" strokeLinecap="round"/>
              <line x1="15" y1="13" x2="15" y2="19" stroke="#0a1420" strokeWidth="2" strokeLinecap="round"/>
            </svg>
          </div>
          <span className="font-bold text-lg text-white tracking-tight">NovaBank</span>
        </Link>

        {user && (
          <div className="flex items-center gap-1">
            <Link to="/dashboard" className={linkClass('/dashboard')}>Accounts</Link>
            <Link to="/transfer" className={linkClass('/transfer')}>Transfer</Link>
            {isAdmin && <Link to="/admin" className={linkClass('/admin')}>Admin</Link>}
          </div>
        )}

        {user && (
          <div className="flex items-center gap-3">
            <div className="text-right hidden sm:block">
              <p className="text-sm font-medium text-white leading-tight">{user.fullName}</p>
              <p className="text-xs text-navy-300 leading-tight">@{user.username}</p>
            </div>
            <button
              onClick={handleLogout}
              className="px-3 py-2 rounded-lg text-sm font-medium text-navy-200 hover:text-white hover:bg-white/5 transition-colors"
            >
              Sign out
            </button>
          </div>
        )}
      </div>
    </nav>
  )
}
