import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, Pencil } from 'lucide-react'
import { getBillingProducts, createBillingProduct, updateBillingProduct, BillingProduct } from '../../api/billing'
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
  name: z.string().min(1, 'Nombre requerido'),
  code: z.string().min(1, 'Código requerido'),
  description: z.string().optional(),
  price: z.coerce.number().min(0, 'Precio inválido'),
  productType: z.string().default('bien'),
  isActive: z.boolean().default(true),
})
type FormData = z.infer<typeof schema>

const fmtCurrency = (v: number) => `$${v.toFixed(2)}`

export default function BillingProductsPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<BillingProduct | null>(null)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['billing-products', page], queryFn: () => getBillingProducts(page) })

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { productType: 'bien', isActive: true },
  })

  const openCreate = () => {
    setEditing(null)
    reset({ name: '', code: '', description: '', price: 0, productType: 'bien', isActive: true })
    setModalOpen(true)
  }

  const openEdit = (p: BillingProduct) => {
    setEditing(p)
    reset({ name: p.name, code: p.code, description: p.description ?? '', price: p.price, productType: p.productType, isActive: p.isActive })
    setModalOpen(true)
  }

  const createMutation = useMutation({
    mutationFn: createBillingProduct,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['billing-products'] }); setModalOpen(false) },
    onError: () => setError('Error al crear producto'),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<BillingProduct> }) => updateBillingProduct(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['billing-products'] }); setModalOpen(false) },
    onError: () => setError('Error al actualizar producto'),
  })

  const onSubmit = (data: FormData) => {
    if (editing) {
      updateMutation.mutate({ id: editing.id, data })
    } else {
      createMutation.mutate(data)
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Productos / Servicios</h1>
          <p className="text-sm text-gray-500 mt-0.5">Catálogo de productos para facturación</p>
        </div>
        <Button onClick={openCreate}><Plus className="h-4 w-4" /> Nuevo producto</Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>Nombre</Th>
              <Th>Código</Th>
              <Th>Precio</Th>
              <Th>Tipo</Th>
              <Th>Estado</Th>
              <Th className="text-right">Acciones</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={6} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !data?.content?.length ? (
              <Tr><Td colSpan={6} className="text-center py-12 text-gray-400">No se encontraron productos</Td></Tr>
            ) : data.content.map(p => (
              <Tr key={p.id}>
                <Td className="font-medium">{p.name}</Td>
                <Td className="font-mono text-xs">{p.code}</Td>
                <Td>{fmtCurrency(p.price)}</Td>
                <Td><Badge color="blue">{p.productType}</Badge></Td>
                <Td><Badge color={p.isActive ? 'green' : 'red'}>{p.isActive ? 'Activo' : 'Inactivo'}</Badge></Td>
                <Td className="text-right">
                  <Button variant="ghost" size="sm" onClick={() => openEdit(p)}><Pencil className="h-3.5 w-3.5" /></Button>
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar producto' : 'Nuevo producto'}>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input label="Nombre" error={errors.name?.message} {...register('name')} />
          <div className="grid grid-cols-2 gap-3">
            <Input label="Código" error={errors.code?.message} {...register('code')} />
            <Input label="Precio ($)" type="number" step="0.01" error={errors.price?.message} {...register('price')} />
          </div>
          <Input label="Descripción" {...register('description')} />
          <Select label="Tipo" {...register('productType')}>
            <option value="bien">Bien</option>
            <option value="servicio">Servicio</option>
          </Select>
          <div className="flex items-center gap-2">
            <input type="checkbox" id="prodActive" {...register('isActive')} className="h-4 w-4 rounded border-gray-300 text-brand-600 focus:ring-brand-500" />
            <label htmlFor="prodActive" className="text-sm text-gray-700">Producto activo</label>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" loading={isSubmitting || createMutation.isPending || updateMutation.isPending}>
              {editing ? 'Guardar cambios' : 'Crear producto'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
