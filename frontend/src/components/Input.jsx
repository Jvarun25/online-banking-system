export default function Input({ label, error, className = '', ...props }) {
  return (
    <label className="block">
      {label && (
        <span className="mb-1.5 block text-sm font-medium text-navy-700">{label}</span>
      )}
      <input
        className={`w-full rounded-xl border bg-white px-4 py-2.5 text-navy-900 placeholder:text-navy-300
          focus:outline-none focus:ring-2 focus:ring-mint-500/40 focus:border-mint-500
          ${error ? 'border-red-400' : 'border-navy-200'} ${className}`}
        {...props}
      />
      {error && <span className="mt-1 block text-xs text-red-500">{error}</span>}
    </label>
  )
}
