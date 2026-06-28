import { formatCurrency, formatDate, txnTypeLabel, isCredit } from '../utils/format'

export default function TransactionRow({ txn }) {
  const credit = isCredit(txn.type)

  return (
    <div className="flex items-center justify-between py-3.5 border-b border-navy-100 last:border-0">
      <div className="flex items-center gap-3">
        <div
          className={`w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold ${
            credit ? 'bg-mint-500/15 text-mint-600' : 'bg-navy-100 text-navy-500'
          }`}
        >
          {credit ? '↓' : '↑'}
        </div>
        <div>
          <p className="text-sm font-semibold text-navy-900">{txnTypeLabel(txn.type)}</p>
          <p className="text-xs text-navy-400">
            {txn.description || (txn.relatedAccountNumber ? `Ref: ${txn.relatedAccountNumber.slice(-4)}` : '—')}
          </p>
        </div>
      </div>
      <div className="text-right">
        <p className={`text-sm font-bold ${credit ? 'text-mint-600' : 'text-navy-900'}`}>
          {credit ? '+' : '−'} {formatCurrency(txn.amount)}
        </p>
        <p className="text-xs text-navy-400">{formatDate(txn.timestamp)}</p>
      </div>
    </div>
  )
}
