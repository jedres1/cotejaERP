import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, Pencil } from 'lucide-react'
import { getSuppliers, createSupplier, updateSupplier, Supplier } from '../../api/purchases'
import { Table, Thead, Tbody, Tr, Th, Td } from '../../components/ui/Table'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Modal from '../../components/ui/Modal'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'
import Alert from '../../components/ui/Alert'

const schema = z.object({
  name: z.string().min(1, 'Nombre requerido'),
  nitDui: z.string().min(1, 'NIT requerido'),
  nrc: z.string().optional(),
  address: z.string().optional(),
  email: z.string().email('Correo inválido').or(z.literal('')).optional(),
  phone: z.string().optional(),
  contactName: z.string().optional(),
  paymentTerms: z.string().optional(),
})
type FormData = z.infer<typeof schema>

export default function SuppliersPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Supplier | null>(null)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['suppliers', page], queryFn: () => getSuppliers(page) })

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const openCreate = () => {
    setEditing(null)
    reset({ name: '', nitDui: '', nrc: '', address: '', email: '', phone: '', contactName: '', paymentTerms: '' })
    setModalOpen(true)
  }

  const openEdit = (s: Supplier) => {
    setEditing(s)
    reset({
      name: s.name, nitDui: s.nitDui, nrc: s.nrc ?? '',
      address: s.address ?? '', email: s.email ?? '', phone: s.phone ?? '',
      contactName: s.contactName ?? '', paymentTerms: s.paymentTerms ?? '',
    })
    setModalOpen(true)
  }

  const createMutation = useMutation({
    mutationFn: createSupplier,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['suppliers'] }); setModalOpen(false) },
    onError: () => setError('Error al crear proveedor'),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Supplier> }) => updateSupplier(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['suppliers'] }); setModalOpen(false) },
    onError: () => setError('Error al actualizar proveedor'),
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
          <h1 className="text-2xl font-bold text-gray-900">Proveedores</h1>
          <p className="text-sm text-gray-500 mt-0.5">Catálogo de proveedores</p>
        </div>
        <Button onClick={openCreate}><Plus className="h-4 w-4" /> Nuevo proveedor</Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>Nombre</Th>
              <Th>NIT/DUI</Th>
              <Th>NRC</Th>
              <Th>Correo</Th>
              <Th>Teléfono</Th>
              <Th className="text-right">Acciones</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={6} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !data?.content?.length ? (
              <Tr><Td colSpan={6} className="text-center py-12 text-gray-400">No se encontraron proveedores</Td></Tr>
            ) : data.content.map(s => (
              <Tr key={s.id}>
                <Td className="font-medium">{s.name}</Td>
                <Td className="font-mono text-xs">{s.nitDui}</Td>
                <Td>{s.nrc ?? '—'}</Td>
                <Td>{s.email ?? '—'}</Td>
                <Td>{s.phone ?? '—'}</Td>
                <Td className="text-right">
                  <Button variant="ghost" size="sm" onClick={() => openEdit(s)}><Pencil className="h-3.5 w-3.5" /></Button>
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar proveedor' : 'Nuevo proveedor'} size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input label="Nombre / Razón Social" error={errors.name?.message} {...register('name')} />
          <div className="grid grid-cols-2 gap-3">
            <Input label="NIT / DUI" error={errors.nitDui?.message} {...register('nitDui')} />
            <Input label="NRC" {...register('nrc')} />
          </div>
          <Input label="Dirección" {...register('address')} />
          <div className="grid grid-cols-2 gap-3">
            <Input label="Correo electrónico" type="email" {...register('email')} />
            <Input label="Teléfono" {...register('phone')} />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Input label="Nombre de contacto" {...register('contactName')} />
            <Input label="Condición de pago" {...register('paymentTerms')} placeholder="Ej: 30 días" />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" loading={isSubmitting || createMutation.isPending || updateMutation.isPending}>
              {editing ? 'Guardar cambios' : 'Crear proveedor'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
