import { formatCurrency } from '../utils/format'

const statusStyles = {
  ACTIVE: 'bg-mint-500/15 text-mint-600',
  FROZEN: 'bg-amber-500/15 text-amber-600',
  CLOSED: 'bg-red-500/15 text-red-600',
}

export default function AccountCard({ account, selected, onClick }) {
  return (
    <button
      onClick={onClick}
      className={`w-full text-left rounded-2xl p-5 border transition-all ${
        selected
          ? 'border-mint-500 bg-navy-950 text-white shadow-lg scale-[1.01]'
          : 'border-navy-100 bg-white hover:border-navy-300'
      }`}
    >
      <div className="flex items-start justify-between mb-3">
        <div>
          <p className={`text-xs font-medium ${selected ? 'text-navy-300' : 'text-navy-400'}`}>
            {account.accountType === 'SAVINGS' ? 'Savings account' : 'Current account'}
          </p>
          <p className={`text-sm font-mono mt-0.5 ${selected ? 'text-navy-200' : 'text-navy-500'}`}>
            {account.accountNumber}
          </p>
        </div>
        <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${statusStyles[account.status]}`}>
          {account.status}
        </span>
      </div>
      <p className={`text-2xl font-bold tracking-tight ${selected ? 'text-white' : 'text-navy-900'}`}>
        {formatCurrency(account.balance)}
      </p>
    </button>
  )
}
