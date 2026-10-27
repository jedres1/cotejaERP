import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, Play } from 'lucide-react'
import { getPayrollPeriods, createPayrollPeriod, applyPayrollPeriod, PayrollPeriod } from '../../api/payroll'
import { Table, Thead, Tbody, Tr, Th, Td } from '../../components/ui/Table'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Modal from '../../components/ui/Modal'
import ConfirmDialog from '../../components/ui/ConfirmDialog'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'
import Alert from '../../components/ui/Alert'
import Badge, { statusBadge } from '../../components/ui/Badge'

const schema = z.object({
  name: z.string().min(1, 'Nombre requerido'),
  periodStart: z.string().min(1, 'Fecha inicio requerida'),
  periodEnd: z.string().min(1, 'Fecha fin requerida'),
  notes: z.string().optional(),
})
type FormData = z.infer<typeof schema>

const fmtCurrency = (v: number) => `$${v.toFixed(2)}`
const fmtDate = (d: string) => d ? new Date(d).toLocaleDateString('es-SV') : '—'

export default function PayrollPeriodsPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [applying, setApplying] = useState<PayrollPeriod | null>(null)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['payroll-periods', page], queryFn: () => getPayrollPeriods(page) })

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const createMutation = useMutation({
    mutationFn: createPayrollPeriod,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['payroll-periods'] }); setModalOpen(false) },
    onError: () => setError('Error al crear período'),
  })

  const applyMutation = useMutation({
    mutationFn: (id: number) => applyPayrollPeriod(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['payroll-periods'] }); setApplying(null) },
    onError: () => setError('Error al aplicar período'),
  })

  const onSubmit = (data: FormData) => createMutation.mutate(data)

  const isBorrador = (status: string) => status?.toLowerCase() === 'borrador'

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Períodos de Nómina</h1>
          <p className="text-sm text-gray-500 mt-0.5">Gestión de períodos de pago</p>
        </div>
        <Button onClick={() => { reset(); setModalOpen(true) }}><Plus className="h-4 w-4" /> Nuevo período</Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>Nombre</Th>
              <Th>Inicio</Th>
              <Th>Fin</Th>
              <Th>Estado</Th>
              <Th>Empleados</Th>
              <Th>Total</Th>
              <Th className="text-right">Acciones</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={7} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !data?.content?.length ? (
              <Tr><Td colSpan={7} className="text-center py-12 text-gray-400">No se encontraron períodos</Td></Tr>
            ) : data.content.map(p => (
              <Tr key={p.id}>
                <Td className="font-medium">{p.name}</Td>
                <Td>{fmtDate(p.periodStart)}</Td>
                <Td>{fmtDate(p.periodEnd)}</Td>
                <Td><Badge color={statusBadge(p.status)}>{p.status}</Badge></Td>
                <Td>{p.employeeCount}</Td>
                <Td>{fmtCurrency(p.totalAmount)}</Td>
                <Td className="text-right">
                  {isBorrador(p.status) && (
                    <Button variant="ghost" size="sm" className="text-green-600" onClick={() => setApplying(p)}>
                      <Play className="h-3.5 w-3.5" />
                    </Button>
                  )}
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Nuevo Período de Nómina">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input label="Nombre del período" error={errors.name?.message} {...register('name')} placeholder="Ej: Quincena 1 - Noviembre 2026" />
          <div className="grid grid-cols-2 gap-3">
            <Input label="Fecha inicio" type="date" error={errors.periodStart?.message} {...register('periodStart')} />
            <Input label="Fecha fin" type="date" error={errors.periodEnd?.message} {...register('periodEnd')} />
          </div>
          <Input label="Notas" {...register('notes')} />
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" loading={isSubmitting || createMutation.isPending}>Crear período</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!applying}
        onClose={() => setApplying(null)}
        onConfirm={() => applying && applyMutation.mutate(applying.id)}
        loading={applyMutation.isPending}
        title="¿Aplicar período?"
        message={`¿Deseas procesar y aplicar el período "${applying?.name}"? Se generarán los pagos para todos los empleados activos.`}
        confirmLabel="Aplicar"
        confirmVariant="primary"
      />
    </div>
  )
}
