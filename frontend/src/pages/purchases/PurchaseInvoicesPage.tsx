import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, CheckCircle, CreditCard } from 'lucide-react'
import {
  getPurchaseInvoices, createPurchaseInvoice, approvePurchaseInvoice, payPurchaseInvoice,
  getSuppliers, PurchaseInvoice
} from '../../api/purchases'
import { Table, Thead, Tbody, Tr, Th, Td } from '../../components/ui/Table'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Select from '../../components/ui/Select'
import Modal from '../../components/ui/Modal'
import ConfirmDialog from '../../components/ui/ConfirmDialog'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'
import Alert from '../../components/ui/Alert'
import Badge, { statusBadge } from '../../components/ui/Badge'

const schema = z.object({
  supplierId: z.coerce.number().min(1, 'Proveedor requerido'),
  documentType: z.string().min(1, 'Tipo requerido'),
  invoiceNumber: z.string().min(1, 'N° Factura requerido'),
  purchaseDate: z.string().min(1, 'Fecha requerida'),
  subtotal: z.coerce.number().min(0),
  iva: z.coerce.number().min(0),
  total: z.coerce.number().min(0),
  paymentMethod: z.string().optional(),
  notes: z.string().optional(),
})
type FormData = z.infer<typeof schema>

const fmtCurrency = (v: number) => `$${v.toFixed(2)}`
const fmtDate = (d: string) => d ? new Date(d).toLocaleDateString('es-SV') : '—'

export default function PurchaseInvoicesPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [approving, setApproving] = useState<PurchaseInvoice | null>(null)
  const [paying, setPaying] = useState<PurchaseInvoice | null>(null)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['purchase-invoices', page], queryFn: () => getPurchaseInvoices(page) })
  const { data: suppliersData } = useQuery({ queryKey: ['suppliers', 0], queryFn: () => getSuppliers(0) })

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { documentType: 'CCF', purchaseDate: new Date().toISOString().split('T')[0] },
  })

  const createMutation = useMutation({
    mutationFn: createPurchaseInvoice,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['purchase-invoices'] }); setModalOpen(false); reset() },
    onError: () => setError('Error al registrar factura'),
  })

  const approveMutation = useMutation({
    mutationFn: (id: number) => approvePurchaseInvoice(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['purchase-invoices'] }); setApproving(null) },
    onError: () => setError('Error al aprobar factura'),
  })

  const payMutation = useMutation({
    mutationFn: (id: number) => payPurchaseInvoice(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['purchase-invoices'] }); setPaying(null) },
    onError: () => setError('Error al registrar pago'),
  })

  const onSubmit = (data: FormData) => createMutation.mutate(data)

  const canApprove = (status: string) => ['registrado', 'pendiente'].includes(status?.toLowerCase())
  const canPay = (status: string) => status?.toLowerCase() === 'aprobado'

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Facturas de Compra</h1>
          <p className="text-sm text-gray-500 mt-0.5">Registro de compras a proveedores</p>
        </div>
        <Button onClick={() => { reset(); setModalOpen(true) }}><Plus className="h-4 w-4" /> Nueva factura</Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>N° Factura</Th>
              <Th>Proveedor</Th>
              <Th>Fecha</Th>
              <Th>Total</Th>
              <Th>Estado</Th>
              <Th className="text-right">Acciones</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={6} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !data?.content?.length ? (
              <Tr><Td colSpan={6} className="text-center py-12 text-gray-400">No se encontraron facturas</Td></Tr>
            ) : data.content.map(inv => (
              <Tr key={inv.id}>
                <Td className="font-mono text-xs">{inv.invoiceNumber}</Td>
                <Td className="font-medium">{inv.supplierName}</Td>
                <Td>{fmtDate(inv.purchaseDate)}</Td>
                <Td>{fmtCurrency(inv.total)}</Td>
                <Td><Badge color={statusBadge(inv.status)}>{inv.status}</Badge></Td>
                <Td className="text-right">
                  <div className="flex items-center justify-end gap-1">
                    {canApprove(inv.status) && (
                      <Button variant="ghost" size="sm" className="text-green-600" onClick={() => setApproving(inv)}>
                        <CheckCircle className="h-3.5 w-3.5" />
                      </Button>
                    )}
                    {canPay(inv.status) && (
                      <Button variant="ghost" size="sm" className="text-blue-600" onClick={() => setPaying(inv)}>
                        <CreditCard className="h-3.5 w-3.5" />
                      </Button>
                    )}
                  </div>
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Nueva Factura de Compra" size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Select label="Proveedor" error={errors.supplierId?.message} {...register('supplierId')}>
            <option value="">Seleccionar proveedor</option>
            {suppliersData?.content?.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
          </Select>
          <div className="grid grid-cols-2 gap-3">
            <Select label="Tipo de documento" error={errors.documentType?.message} {...register('documentType')}>
              <option value="CCF">CCF</option>
              <option value="Factura">Factura</option>
              <option value="Tiquete">Tiquete</option>
            </Select>
            <Input label="N° Factura" error={errors.invoiceNumber?.message} {...register('invoiceNumber')} />
          </div>
          <Input label="Fecha de compra" type="date" error={errors.purchaseDate?.message} {...register('purchaseDate')} />
          <div className="grid grid-cols-3 gap-3">
            <Input label="Subtotal ($)" type="number" step="0.01" {...register('subtotal')} />
            <Input label="IVA ($)" type="number" step="0.01" {...register('iva')} />
            <Input label="Total ($)" type="number" step="0.01" error={errors.total?.message} {...register('total')} />
          </div>
          <Select label="Método de pago" {...register('paymentMethod')}>
            <option value="">Sin especificar</option>
            <option value="efectivo">Efectivo</option>
            <option value="transferencia">Transferencia</option>
            <option value="cheque">Cheque</option>
            <option value="credito">Crédito</option>
          </Select>
          <Input label="Notas" {...register('notes')} />
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" loading={isSubmitting || createMutation.isPending}>Registrar</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!approving}
        onClose={() => setApproving(null)}
        onConfirm={() => approving && approveMutation.mutate(approving.id)}
        loading={approveMutation.isPending}
        title="¿Aprobar factura?"
        message={`¿Deseas aprobar la factura "${approving?.invoiceNumber}" por ${fmtCurrency(approving?.total ?? 0)}?`}
        confirmLabel="Aprobar"
        confirmVariant="primary"
      />

      <ConfirmDialog
        open={!!paying}
        onClose={() => setPaying(null)}
        onConfirm={() => paying && payMutation.mutate(paying.id)}
        loading={payMutation.isPending}
        title="¿Registrar pago?"
        message={`¿Deseas marcar como pagada la factura "${paying?.invoiceNumber}" por ${fmtCurrency(paying?.total ?? 0)}?`}
        confirmLabel="Pagar"
        confirmVariant="primary"
      />
    </div>
  )
}
