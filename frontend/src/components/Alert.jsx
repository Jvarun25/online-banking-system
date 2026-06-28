export default function Alert({ type = 'error', children }) {
  if (!children) return null

  const styles = {
    error: 'bg-red-50 text-red-700 border-red-200',
    success: 'bg-mint-50 text-mint-700 border-mint-200',
    info: 'bg-navy-50 text-navy-700 border-navy-200',
  }

  return (
    <div className={`rounded-xl border px-4 py-3 text-sm font-medium ${styles[type]}`}>
      {children}
    </div>
  )
}
