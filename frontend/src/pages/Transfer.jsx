import { useEffect, useState } from 'react'
import Navbar from '../components/Navbar'
import Card from '../components/Card'
import Button from '../components/Button'
import Input from '../components/Input'
import Alert from '../components/Alert'
import { accountService, transferService } from '../services'
import { formatCurrency } from '../utils/format'

export default function Transfer() {
  const [accounts, setAccounts] = useState([])
  const [fromAccount, setFromAccount] = useState('')
  const [toAccount, setToAccount] = useState('')
  const [amount, setAmount] = useState('')
  const [description, setDescription] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(null)

  useEffect(() => {
    accountService.list().then(({ data }) => {
      setAccounts(data)
      const active = data.find((a) => a.status === 'ACTIVE')
      if (active) setFromAccount(active.accountNumber)
    }).catch(() => setError('Failed to load your accounts'))
  }, [])

  const selectedFrom = accounts.find((a) => a.accountNumber === fromAccount)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess(null)
    setSubmitting(true)
    try {
      const { data } = await transferService.transfer({
        fromAccountNumber: fromAccount,
        toAccountNumber: toAccount,
        amount: parseFloat(amount),
        description: description || undefined,
      })
      setSuccess(data)
      setAmount('')
      setToAccount('')
      setDescription('')
      // refresh balances
      const { data: updated } = await accountService.list()
      setAccounts(updated)
    } catch (err) {
      setError(err.response?.data?.message || 'Transfer failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#f6f8fb]">
      <Navbar />
      <main className="max-w-2xl mx-auto px-6 py-8">
        <h1 className="text-2xl font-bold text-navy-900 tracking-tight mb-1">Transfer funds</h1>
        <p className="text-navy-400 text-sm mb-6">Move money between accounts, instantly and securely</p>

        <Card>
          {error && <div className="mb-4"><Alert type="error">{error}</Alert></div>}
          {success && (
            <div className="mb-4">
              <Alert type="success">
                Transfer complete. Reference: <span className="font-mono">{success.referenceId.slice(0, 8)}</span> ·
                New balance: {formatCurrency(success.balanceAfter)}
              </Alert>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <label className="block">
              <span className="mb-1.5 block text-sm font-medium text-navy-700">From account</span>
              <select
                required
                value={fromAccount}
                onChange={(e) => setFromAccount(e.target.value)}
                className="w-full rounded-xl border border-navy-200 bg-white px-4 py-2.5 text-navy-900 focus:outline-none focus:ring-2 focus:ring-mint-500/40"
              >
                <option value="" disabled>Select an account</option>
                {accounts.map((a) => (
                  <option key={a.accountNumber} value={a.accountNumber} disabled={a.status !== 'ACTIVE'}>
                    {a.accountType} · {a.accountNumber} · {formatCurrency(a.balance)} {a.status !== 'ACTIVE' ? `(${a.status})` : ''}
                  </option>
                ))}
              </select>
            </label>

            <Input
              label="To account number"
              required
              value={toAccount}
              onChange={(e) => setToAccount(e.target.value)}
              placeholder="12-digit account number"
            />

            <Input
              label="Amount"
              type="number"
              min="0.01"
              step="0.01"
              required
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="0.00"
            />
            {selectedFrom && (
              <p className="text-xs text-navy-400 -mt-2">Available balance: {formatCurrency(selectedFrom.balance)}</p>
            )}

            <Input
              label="Note (optional)"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="What's this for?"
            />

            <Button type="submit" className="w-full" size="lg" loading={submitting}>
              Send transfer
            </Button>
          </form>
        </Card>
      </main>
    </div>
  )
}
