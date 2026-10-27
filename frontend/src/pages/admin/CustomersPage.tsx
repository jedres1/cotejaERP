import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, Pencil, Trash2 } from 'lucide-react'
import { getCustomers, createCustomer, updateCustomer, deleteCustomer, Customer } from '../../api/admin'
import { Table, Thead, Tbody, Tr, Th, Td } from '../../components/ui/Table'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Modal from '../../components/ui/Modal'
import ConfirmDialog from '../../components/ui/ConfirmDialog'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'
import Alert from '../../components/ui/Alert'

const schema = z.object({
  name: z.string().min(1, 'Nombre requerido'),
  email: z.string().email('Correo inválido').or(z.literal('')),
  phone: z.string().optional(),
})
type FormData = z.infer<typeof schema>

export default function CustomersPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Customer | null>(null)
  const [deleting, setDeleting] = useState<Customer | null>(null)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['customers', page],
    queryFn: () => getCustomers(page),
  })

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  const openCreate = () => {
    setEditing(null)
    reset({ name: '', email: '', phone: '' })
    setModalOpen(true)
  }

  const openEdit = (c: Customer) => {
    setEditing(c)
    reset({ name: c.name, email: c.email ?? '', phone: c.phone ?? '' })
    setModalOpen(true)
  }

  const createMutation = useMutation({
    mutationFn: createCustomer,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['customers'] }); setModalOpen(false) },
    onError: () => setError('Error al crear cliente'),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Customer> }) => updateCustomer(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['customers'] }); setModalOpen(false) },
    onError: () => setError('Error al actualizar cliente'),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteCustomer(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['customers'] }); setDeleting(null) },
    onError: () => setError('Error al eliminar cliente'),
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
          <h1 className="text-2xl font-bold text-gray-900">Clientes</h1>
          <p className="text-sm text-gray-500 mt-0.5">Gestión de clientes del sistema</p>
        </div>
        <Button onClick={openCreate}>
          <Plus className="h-4 w-4" /> Nuevo cliente
        </Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>Nombre</Th>
              <Th>Correo</Th>
              <Th>Teléfono</Th>
              <Th className="text-right">Acciones</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr>
                <Td colSpan={4} className="text-center py-12">
                  <Spinner className="mx-auto" />
                </Td>
              </Tr>
            ) : !data?.content?.length ? (
              <Tr>
                <Td colSpan={4} className="text-center py-12 text-gray-400">
                  No se encontraron clientes
                </Td>
              </Tr>
            ) : data.content.map(c => (
              <Tr key={c.id}>
                <Td className="font-medium">{c.name}</Td>
                <Td>{c.email ?? '—'}</Td>
                <Td>{c.phone ?? '—'}</Td>
                <Td className="text-right">
                  <div className="flex items-center justify-end gap-2">
                    <Button variant="ghost" size="sm" onClick={() => openEdit(c)}>
                      <Pencil className="h-3.5 w-3.5" />
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => setDeleting(c)} className="text-red-500 hover:text-red-600">
                      <Trash2 className="h-3.5 w-3.5" />
                    </Button>
                  </div>
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar cliente' : 'Nuevo cliente'}>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input label="Nombre" error={errors.name?.message} {...register('name')} />
          <Input label="Correo electrónico" type="email" error={errors.email?.message} {...register('email')} />
          <Input label="Teléfono" {...register('phone')} />
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" loading={isSubmitting || createMutation.isPending || updateMutation.isPending}>
              {editing ? 'Guardar cambios' : 'Crear cliente'}
            </Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!deleting}
        onClose={() => setDeleting(null)}
        onConfirm={() => deleting && deleteMutation.mutate(deleting.id)}
        loading={deleteMutation.isPending}
        message={`¿Deseas eliminar al cliente "${deleting?.name}"? Esta acción no se puede deshacer.`}
      />
    </div>
  )
}
