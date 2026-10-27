type BadgeColor = 'green' | 'red' | 'yellow' | 'blue' | 'gray'

interface BadgeProps {
  children: React.ReactNode
  color?: BadgeColor
}

const colorMap: Record<BadgeColor, string> = {
  green: 'bg-green-100 text-green-700',
  red: 'bg-red-100 text-red-700',
  yellow: 'bg-yellow-100 text-yellow-700',
  blue: 'bg-blue-100 text-blue-700',
  gray: 'bg-gray-100 text-gray-600',
}

export default function Badge({ children, color = 'blue' }: BadgeProps) {
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${colorMap[color]}`}>
      {children}
    </span>
  )
}

export function statusBadge(status: string): BadgeColor {
  const s = status?.toLowerCase()
  if (['activo', 'aprobado', 'pagado', 'aplicado', 'activa'].includes(s)) return 'green'
  if (['inactivo', 'anulado', 'rechazado', 'cancelado'].includes(s)) return 'red'
  if (['pendiente', 'borrador', 'registrado'].includes(s)) return 'yellow'
  return 'blue'
}
