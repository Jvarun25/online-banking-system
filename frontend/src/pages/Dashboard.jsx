import { useEffect, useState, useCallback } from 'react'
import Navbar from '../components/Navbar'
import Card from '../components/Card'
import Button from '../components/Button'
import Input from '../components/Input'
import Alert from '../components/Alert'
import AccountCard from '../components/AccountCard'
import TransactionRow from '../components/TransactionRow'
import { accountService, transactionService } from '../services'
import { formatCurrency } from '../utils/format'

export default function Dashboard() {
  const [accounts, setAccounts] = useState([])
  const [selectedAccount, setSelectedAccount] = useState(null)
  const [transactions, setTransactions] = useState([])
  const [loadingAccounts, setLoadingAccounts] = useState(true)
  const [loadingTxns, setLoadingTxns] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // New account form
  const [showNewAccount, setShowNewAccount] = useState(false)
  const [newAccountType, setNewAccountType] = useState('SAVINGS')
  const [newAccountDeposit, setNewAccountDeposit] = useState('')
  const [creating, setCreating] = useState(false)

  // Deposit / withdraw form
  const [activeAction, setActiveAction] = useState(null) // 'deposit' | 'withdraw' | null
  const [amount, setAmount] = useState('')
  const [description, setDescription] = useState('')
  const [submittingAction, setSubmittingAction] = useState(false)

  const loadAccounts = useCallback(async () => {
    setLoadingAccounts(true)
    setError('')
    try {
      const { data } = await accountService.list()
      setAccounts(data)
      if (data.length > 0) {
        setSelectedAccount((prev) => data.find((a) => a.accountNumber === prev?.accountNumber) || data[0])
      } else {
        setSelectedAccount(null)
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load accounts')
    } finally {
      setLoadingAccounts(false)
    }
  }, [])

  const loadTransactions = useCallback(async (accountNumber) => {
    if (!accountNumber) return
    setLoadingTxns(true)
    try {
      const { data } = await transactionService.history(accountNumber, 0, 15)
      setTransactions(data.content || [])
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load transaction history')
    } finally {
      setLoadingTxns(false)
    }
  }, [])

  useEffect(() => {
    loadAccounts()
  }, [loadAccounts])

  useEffect(() => {
    if (selectedAccount) loadTransactions(selectedAccount.accountNumber)
  }, [selectedAccount, loadTransactions])

  const handleCreateAccount = async (e) => {
    e.preventDefault()
    setCreating(true)
    setError('')
    setSuccess('')
    try {
      await accountService.create({
        accountType: newAccountType,
        initialDeposit: newAccountDeposit ? parseFloat(newAccountDeposit) : 0,
      })
      setSuccess('Account created successfully')
      setShowNewAccount(false)
      setNewAccountDeposit('')
      await loadAccounts()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create account')
    } finally {
      setCreating(false)
    }
  }

  const handleAction = async (e) => {
    e.preventDefault()
    if (!selectedAccount) return
    setSubmittingAction(true)
    setError('')
    setSuccess('')
    try {
      const payload = {
        accountNumber: selectedAccount.accountNumber,
        amount: parseFloat(amount),
        description: description || undefined,
      }
      if (activeAction === 'deposit') {
        await accountService.deposit(payload)
        setSuccess(`Deposited ${formatCurrency(amount)} successfully`)
      } else {
        await accountService.withdraw(payload)
        setSuccess(`Withdrew ${formatCurrency(amount)} successfully`)
      }
      setAmount('')
      setDescription('')
      setActiveAction(null)
      await loadAccounts()
      await loadTransactions(selectedAccount.accountNumber)
    } catch (err) {
      setError(err.response?.data?.message || 'Transaction failed')
    } finally {
      setSubmittingAction(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#f6f8fb]">
      <Navbar />

      <main className="max-w-6xl mx-auto px-6 py-8">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-navy-900 tracking-tight">Your accounts</h1>
            <p className="text-navy-400 text-sm mt-0.5">Manage balances, deposits, and withdrawals</p>
          </div>
          <Button onClick={() => setShowNewAccount((s) => !s)} variant={showNewAccount ? 'outline' : 'primary'}>
            {showNewAccount ? 'Cancel' : '+ New account'}
          </Button>
        </div>

        {error && <div className="mb-4"><Alert type="error">{error}</Alert></div>}
        {success && <div className="mb-4"><Alert type="success">{success}</Alert></div>}

        {showNewAccount && (
          <Card className="mb-6">
            <form onSubmit={handleCreateAccount} className="flex flex-wrap items-end gap-4">
              <label className="block">
                <span className="mb-1.5 block text-sm font-medium text-navy-700">Account type</span>
                <select
                  value={newAccountType}
                  onChange={(e) => setNewAccountType(e.target.value)}
                  className="rounded-xl border border-navy-200 bg-white px-4 py-2.5 text-navy-900 focus:outline-none focus:ring-2 focus:ring-mint-500/40"
                >
                  <option value="SAVINGS">Savings</option>
                  <option value="CURRENT">Current</option>
                </select>
              </label>
              <Input
                label="Initial deposit (optional)"
                type="number"
                min="0"
                step="0.01"
                placeholder="0.00"
                value={newAccountDeposit}
                onChange={(e) => setNewAccountDeposit(e.target.value)}
                className="w-48"
              />
              <Button type="submit" loading={creating}>Create</Button>
            </form>
          </Card>
        )}

        {loadingAccounts ? (
          <p className="text-navy-400">Loading accounts…</p>
        ) : accounts.length === 0 ? (
          <Card className="text-center py-12">
            <p className="text-navy-500">You don't have any accounts yet.</p>
            <Button className="mt-4" onClick={() => setShowNewAccount(true)}>Open your first account</Button>
          </Card>
        ) : (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
              {accounts.map((acc) => (
                <AccountCard
                  key={acc.accountNumber}
                  account={acc}
                  selected={selectedAccount?.accountNumber === acc.accountNumber}
                  onClick={() => setSelectedAccount(acc)}
                />
              ))}
            </div>

            {selectedAccount && (
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <Card className="lg:col-span-1 h-fit">
                  <h2 className="font-bold text-navy-900 mb-4">Quick actions</h2>
                  <div className="flex gap-3 mb-4">
                    <Button
                      variant={activeAction === 'deposit' ? 'primary' : 'outline'}
                      className="flex-1"
                      onClick={() => { setActiveAction('deposit'); setError(''); setSuccess('') }}
                      disabled={selectedAccount.status !== 'ACTIVE'}
                    >
                      Deposit
                    </Button>
                    <Button
                      variant={activeAction === 'withdraw' ? 'primary' : 'outline'}
                      className="flex-1"
                      onClick={() => { setActiveAction('withdraw'); setError(''); setSuccess('') }}
                      disabled={selectedAccount.status !== 'ACTIVE'}
                    >
                      Withdraw
                    </Button>
                  </div>

                  {selectedAccount.status !== 'ACTIVE' && (
                    <Alert type="info">This account is {selectedAccount.status.toLowerCase()} and can't be used for transactions.</Alert>
                  )}

                  {activeAction && (
                    <form onSubmit={handleAction} className="space-y-3 mt-2">
                      <Input
                        label="Amount"
                        type="number"
                        min="0.01"
                        step="0.01"
                        required
                        autoFocus
                        value={amount}
                        onChange={(e) => setAmount(e.target.value)}
                        placeholder="0.00"
                      />
                      <Input
                        label="Description (optional)"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        placeholder={activeAction === 'deposit' ? 'e.g. Salary' : 'e.g. ATM withdrawal'}
                      />
                      <Button type="submit" className="w-full" loading={submittingAction}>
                        Confirm {activeAction}
                      </Button>
                    </form>
                  )}
                </Card>

                <Card className="lg:col-span-2">
                  <h2 className="font-bold text-navy-900 mb-2">Recent activity</h2>
                  <p className="text-xs text-navy-400 mb-2 font-mono">{selectedAccount.accountNumber}</p>
                  {loadingTxns ? (
                    <p className="text-navy-400 text-sm py-4">Loading transactions…</p>
                  ) : transactions.length === 0 ? (
                    <p className="text-navy-400 text-sm py-4">No transactions yet.</p>
                  ) : (
                    <div>
                      {transactions.map((txn) => (
                        <TransactionRow key={txn.referenceId + txn.type} txn={txn} />
                      ))}
                    </div>
                  )}
                </Card>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  )
}
