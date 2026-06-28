export function formatCurrency(amount) {
  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(num ?? 0)
}

export function formatDate(isoString) {
  if (!isoString) return '—'
  const date = new Date(isoString)
  return date.toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function maskAccountNumber(accountNumber) {
  if (!accountNumber || accountNumber.length < 4) return accountNumber
  return `•••• ${accountNumber.slice(-4)}`
}

export function txnTypeLabel(type) {
  const map = {
    DEPOSIT: 'Deposit',
    WITHDRAWAL: 'Withdrawal',
    TRANSFER_IN: 'Transfer received',
    TRANSFER_OUT: 'Transfer sent',
  }
  return map[type] || type
}

export function isCredit(type) {
  return type === 'DEPOSIT' || type === 'TRANSFER_IN'
}
