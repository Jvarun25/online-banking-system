export default function Card({ children, className = '', ...props }) {
  return (
    <div
      className={`rounded-2xl border border-navy-100 bg-white p-6 shadow-sm ${className}`}
      {...props}
    >
      {children}
    </div>
  )
}
