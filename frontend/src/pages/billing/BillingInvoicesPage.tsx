import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm, useFieldArray } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, Ban, Trash2 } from 'lucide-react'
import {
  getInvoices, createInvoice, anularInvoice,
  getBillingClients, getBillingProducts,
  BillingInvoice
} from '../../api/billing'
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

const itemSchema = z.object({
  productId: z.coerce.number().min(1, 'Producto requerido'),
  quantity: z.coerce.number().min(1, 'Mínimo 1'),
  unitPrice: z.coerce.number().min(0),
  description: z.string().optional(),
})

const schema = z.object({
  documentType: z.string().min(1, 'Tipo requerido'),
  clienteId: z.coerce.number().min(1, 'Cliente requerido'),
  items: z.array(itemSchema).min(1, 'Agrega al menos un ítem'),
  notes: z.string().optional(),
})
type FormData = z.infer<typeof schema>

const fmtCurrency = (v: number) => `$${v.toFixed(2)}`
const fmtDate = (d: string) => d ? new Date(d).toLocaleDateString('es-SV') : '—'

export default function BillingInvoicesPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [anulando, setAnulando] = useState<BillingInvoice | null>(null)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['billing-invoices', page], queryFn: () => getInvoices(page) })
  const { data: clientesData } = useQuery({ queryKey: ['billing-clients', 0], queryFn: () => getBillingClients(0) })
  const { data: productsData } = useQuery({ queryKey: ['billing-products', 0], queryFn: () => getBillingProducts(0) })

  const { register, handleSubmit, control, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { documentType: '01', items: [{ productId: 0, quantity: 1, unitPrice: 0 }] },
  })

  const { fields, append, remove } = useFieldArray({ control, name: 'items' })

  const createMutation = useMutation({
    mutationFn: createInvoice,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['billing-invoices'] }); setModalOpen(false); reset() },
    onError: () => setError('Error al emitir factura'),
  })

  const anularMutation = useMutation({
    mutationFn: (id: number) => anularInvoice(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['billing-invoices'] }); setAnulando(null) },
    onError: () => setError('Error al anular factura'),
  })

  const onSubmit = (data: FormData) => {
    createMutation.mutate(data)
  }

  const canAnular = (status: string) => !['anulado', 'anulada'].includes(status?.toLowerCase())

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Facturas</h1>
          <p className="text-sm text-gray-500 mt-0.5">Facturación electrónica (DTE)</p>
        </div>
        <Button onClick={() => { reset(); setModalOpen(true) }}>
          <Plus className="h-4 w-4" /> Nueva factura
        </Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>N° Control</Th>
              <Th>Tipo</Th>
              <Th>Cliente</Th>
              <Th>Total</Th>
              <Th>Estado</Th>
              <Th>Fecha</Th>
              <Th className="text-right">Acciones</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={7} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !data?.content?.length ? (
              <Tr><Td colSpan={7} className="text-center py-12 text-gray-400">No se encontraron facturas</Td></Tr>
            ) : data.content.map(inv => (
              <Tr key={inv.id}>
                <Td className="font-mono text-xs">{inv.controlNumber ?? '—'}</Td>
                <Td>{inv.documentType}</Td>
                <Td>{inv.clienteNombre}</Td>
                <Td>{fmtCurrency(inv.total)}</Td>
                <Td><Badge color={statusBadge(inv.status)}>{inv.status}</Badge></Td>
                <Td>{fmtDate(inv.emissionDate)}</Td>
                <Td className="text-right">
                  {canAnular(inv.status) && (
                    <Button variant="ghost" size="sm" className="text-red-500" onClick={() => setAnulando(inv)}>
                      <Ban className="h-3.5 w-3.5" />
                    </Button>
                  )}
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      {/* New Invoice Modal */}
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Nueva Factura" size="xl">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <Select label="Tipo de documento" error={errors.documentType?.message} {...register('documentType')}>
              <option value="01">01 - Factura</option>
              <option value="03">03 - Comprobante de Crédito Fiscal</option>
            </Select>
            <Select label="Cliente" error={errors.clienteId?.message} {...register('clienteId')}>
              <option value="">Seleccionar cliente</option>
              {clientesData?.content?.map(c => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </Select>
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <p className="text-sm font-medium text-gray-700">Ítems</p>
              <Button
                type="button"
                variant="secondary"
                size="sm"
                onClick={() => append({ productId: 0, quantity: 1, unitPrice: 0 })}
              >
                <Plus className="h-3.5 w-3.5" /> Agregar ítem
              </Button>
            </div>
            {errors.items?.root && <p className="text-xs text-red-500 mb-2">{errors.items.root.message}</p>}
            <div className="space-y-2">
              {fields.map((field, idx) => (
                <div key={field.id} className="flex gap-2 items-start">
                  <div className="flex-1">
                    <Select {...register(`items.${idx}.productId`)} error={errors.items?.[idx]?.productId?.message}>
                      <option value="">Producto</option>
                      {productsData?.content?.map(p => (
                        <option key={p.id} value={p.id}>{p.name}</option>
                      ))}
                    </Select>
                  </div>
                  <div className="w-20">
                    <Input type="number" placeholder="Cant." {...register(`items.${idx}.quantity`)} />
                  </div>
                  <div className="w-28">
                    <Input type="number" step="0.01" placeholder="Precio" {...register(`items.${idx}.unitPrice`)} />
                  </div>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    className="text-red-500 mt-1"
                    onClick={() => remove(idx)}
                    disabled={fields.length === 1}
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              ))}
            </div>
          </div>

          <Input label="Notas" {...register('notes')} />

          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" loading={isSubmitting || createMutation.isPending}>Emitir DTE</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!anulando}
        onClose={() => setAnulando(null)}
        onConfirm={() => anulando && anularMutation.mutate(anulando.id)}
        loading={anularMutation.isPending}
        title="¿Anular factura?"
        message={`¿Deseas anular la factura ${anulando?.controlNumber ?? ''}? Esta acción no se puede deshacer.`}
        confirmLabel="Anular"
      />
    </div>
  )
}
