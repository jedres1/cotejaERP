import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus } from 'lucide-react'
import {
  getMovements, createMovement,
  getProductTypes, getWarehouses
} from '../../api/inventory'
import { Table, Thead, Tbody, Tr, Th, Td } from '../../components/ui/Table'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Select from '../../components/ui/Select'
import Modal from '../../components/ui/Modal'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'
import Alert from '../../components/ui/Alert'
import Badge from '../../components/ui/Badge'

const schema = z.object({
  movementType: z.string().min(1, 'Tipo requerido'),
  productTypeId: z.coerce.number().min(1, 'Producto requerido'),
  warehouseId: z.coerce.number().min(1, 'Bodega requerida'),
  quantity: z.coerce.number().min(1, 'Cantidad mínima 1'),
  unitCost: z.coerce.number().min(0),
  notes: z.string().optional(),
})
type FormData = z.infer<typeof schema>

const typeColor = (t: string) => {
  const s = t?.toLowerCase()
  if (s === 'entrada') return 'green'
  if (s === 'salida') return 'red'
  return 'yellow'
}

const fmtCurrency = (v: number) => `$${v.toFixed(2)}`
const fmtDate = (d: string) => d ? new Date(d).toLocaleDateString('es-SV') : '—'

export default function InventoryMovementsPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['inventory-movements', page], queryFn: () => getMovements(page) })
  const { data: productTypes } = useQuery({ queryKey: ['product-types'], queryFn: getProductTypes })
  const { data: warehouses } = useQuery({ queryKey: ['warehouses'], queryFn: getWarehouses })

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { movementType: 'entrada' },
  })

  const createMutation = useMutation({
    mutationFn: createMovement,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['inventory-movements'] }); setModalOpen(false); reset() },
    onError: () => setError('Error al registrar movimiento'),
  })

  const onSubmit = (data: FormData) => createMutation.mutate(data)

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Movimientos de Inventario</h1>
          <p className="text-sm text-gray-500 mt-0.5">Entradas, salidas y ajustes</p>
        </div>
        <Button onClick={() => { reset(); setModalOpen(true) }}><Plus className="h-4 w-4" /> Nuevo movimiento</Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>Tipo</Th>
              <Th>Producto</Th>
              <Th>Bodega</Th>
              <Th>Cantidad</Th>
              <Th>Costo Unit.</Th>
              <Th>Fecha</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={6} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !data?.content?.length ? (
              <Tr><Td colSpan={6} className="text-center py-12 text-gray-400">No se encontraron movimientos</Td></Tr>
            ) : data.content.map(m => (
              <Tr key={m.id}>
                <Td><Badge color={typeColor(m.movementType) as 'green' | 'red' | 'yellow'}>{m.movementType}</Badge></Td>
                <Td>{m.productTypeName}</Td>
                <Td>{m.warehouseName}</Td>
                <Td>{m.quantity}</Td>
                <Td>{fmtCurrency(m.unitCost)}</Td>
                <Td>{fmtDate(m.movementDate)}</Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Nuevo Movimiento" size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Select label="Tipo de movimiento" error={errors.movementType?.message} {...register('movementType')}>
            <option value="entrada">Entrada</option>
            <option value="salida">Salida</option>
            <option value="ajuste">Ajuste</option>
          </Select>
          <Select label="Producto" error={errors.productTypeId?.message} {...register('productTypeId')}>
            <option value="">Seleccionar producto</option>
            {(productTypes ?? []).map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
          </Select>
          <Select label="Bodega" error={errors.warehouseId?.message} {...register('warehouseId')}>
            <option value="">Seleccionar bodega</option>
            {(warehouses ?? []).map(w => <option key={w.id} value={w.id}>{w.name}</option>)}
          </Select>
          <div className="grid grid-cols-2 gap-3">
            <Input label="Cantidad" type="number" error={errors.quantity?.message} {...register('quantity')} />
            <Input label="Costo unitario ($)" type="number" step="0.01" {...register('unitCost')} />
          </div>
          <Input label="Notas" {...register('notes')} />
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" loading={isSubmitting || createMutation.isPending}>Registrar</Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
