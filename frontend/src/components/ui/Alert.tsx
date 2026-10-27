import { ReactNode } from 'react'
import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-react'

type AlertType = 'success' | 'error' | 'warning' | 'info'

interface AlertProps {
  type?: AlertType
  title?: string
  children: ReactNode
  onClose?: () => void
}

const config: Record<AlertType, { icon: React.ElementType; classes: string }> = {
  success: { icon: CheckCircle, classes: 'bg-green-50 border-green-200 text-green-800' },
  error:   { icon: XCircle,     classes: 'bg-red-50 border-red-200 text-red-800' },
  warning: { icon: AlertTriangle, classes: 'bg-yellow-50 border-yellow-200 text-yellow-800' },
  info:    { icon: Info,         classes: 'bg-blue-50 border-blue-200 text-blue-800' },
}

export default function Alert({ type = 'info', title, children, onClose }: AlertProps) {
  const { icon: Icon, classes } = config[type]
  return (
    <div className={`flex items-start gap-3 rounded-lg border px-4 py-3 ${classes}`}>
      <Icon className="h-5 w-5 mt-0.5 flex-shrink-0" />
      <div className="flex-1 text-sm">
        {title && <p className="font-semibold mb-0.5">{title}</p>}
        <div>{children}</div>
      </div>
      {onClose && (
        <button onClick={onClose} className="opacity-60 hover:opacity-100 transition-opacity">
          <X className="h-4 w-4" />
        </button>
      )}
    </div>
  )
}
