import { useEffect, useState } from 'react'
import Navbar from '../components/Navbar'
import Card from '../components/Card'
import Button from '../components/Button'
import Alert from '../components/Alert'
import { adminService } from '../services'
import { formatCurrency, formatDate, maskAccountNumber } from '../utils/format'

const statusStyles = {
  ACTIVE: 'bg-mint-500/15 text-mint-600',
  FROZEN: 'bg-amber-500/15 text-amber-600',
  CLOSED: 'bg-red-500/15 text-red-600',
}

export default function Admin() {
  const [accounts, setAccounts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actingOn, setActingOn] = useState(null)

  const load = async () => {
    setLoading(true)
    setError('')
    try {
      const { data } = await adminService.allAccounts()
      setAccounts(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load accounts')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const toggleFreeze = async (account) => {
    setActingOn(account.accountNumber)
    setError('')
    try {
      if (account.status === 'ACTIVE') {
        await adminService.freeze(account.accountNumber)
      } else if (account.status === 'FROZEN') {
        await adminService.unfreeze(account.accountNumber)
      }
      await load()
    } catch (err) {
      setError(err.response?.data?.message || 'Action failed')
    } finally {
      setActingOn(null)
    }
  }

  const totalBalance = accounts.reduce((sum, a) => sum + parseFloat(a.balance), 0)

  return (
    <div className="min-h-screen bg-[#f6f8fb]">
      <Navbar />
      <main className="max-w-6xl mx-auto px-6 py-8">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-navy-900 tracking-tight">Admin · All accounts</h1>
          <p className="text-navy-400 text-sm mt-0.5">
            Bank-wide view, restricted to ROLE_ADMIN — {accounts.length} accounts, {formatCurrency(totalBalance)} total
          </p>
        </div>

        {error && <div className="mb-4"><Alert type="error">{error}</Alert></div>}

        <Card className="p-0 overflow-hidden">
          {loading ? (
            <p className="text-navy-400 p-6">Loading…</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-navy-50 text-navy-500 text-xs uppercase tracking-wide">
                  <th className="text-left px-6 py-3 font-semibold">Account</th>
                  <th className="text-left px-6 py-3 font-semibold">Owner</th>
                  <th className="text-left px-6 py-3 font-semibold">Type</th>
                  <th className="text-right px-6 py-3 font-semibold">Balance</th>
                  <th className="text-left px-6 py-3 font-semibold">Opened</th>
                  <th className="text-left px-6 py-3 font-semibold">Status</th>
                  <th className="text-right px-6 py-3 font-semibold">Action</th>
                </tr>
              </thead>
              <tbody>
                {accounts.map((acc) => (
                  <tr key={acc.accountNumber} className="border-t border-navy-100 hover:bg-navy-50/50">
                    <td className="px-6 py-3 font-mono text-navy-700">{maskAccountNumber(acc.accountNumber)}</td>
                    <td className="px-6 py-3 text-navy-700">@{acc.ownerUsername}</td>
                    <td className="px-6 py-3 text-navy-600">{acc.accountType}</td>
                    <td className="px-6 py-3 text-right font-semibold text-navy-900">{formatCurrency(acc.balance)}</td>
                    <td className="px-6 py-3 text-navy-500">{formatDate(acc.createdAt)}</td>
                    <td className="px-6 py-3">
                      <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${statusStyles[acc.status]}`}>
                        {acc.status}
                      </span>
                    </td>
                    <td className="px-6 py-3 text-right">
                      {acc.status !== 'CLOSED' && (
                        <Button
                          size="sm"
                          variant={acc.status === 'ACTIVE' ? 'outline' : 'secondary'}
                          loading={actingOn === acc.accountNumber}
                          onClick={() => toggleFreeze(acc)}
                        >
                          {acc.status === 'ACTIVE' ? 'Freeze' : 'Unfreeze'}
                        </Button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Card>
      </main>
    </div>
  )
}
