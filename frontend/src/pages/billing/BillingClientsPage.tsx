import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, Pencil } from 'lucide-react'
import { getBillingClients, createBillingClient, updateBillingClient, BillingClient } from '../../api/billing'
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
  documentType: z.string().min(1, 'Tipo requerido'),
  documentNumber: z.string().min(1, 'Número requerido'),
  nrc: z.string().optional(),
  address: z.string().optional(),
  phone: z.string().optional(),
  email: z.string().email('Correo inválido').or(z.literal('')).optional(),
  isActive: z.boolean().default(true),
})
type FormData = z.infer<typeof schema>

export default function BillingClientsPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<BillingClient | null>(null)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['billing-clients', page], queryFn: () => getBillingClients(page) })

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { documentType: 'DUI', isActive: true },
  })

  const openCreate = () => {
    setEditing(null)
    reset({ name: '', documentType: 'DUI', documentNumber: '', nrc: '', address: '', phone: '', email: '', isActive: true })
    setModalOpen(true)
  }

  const openEdit = (c: BillingClient) => {
    setEditing(c)
    reset({
      name: c.name, documentType: c.documentType, documentNumber: c.documentNumber,
      nrc: c.nrc ?? '', address: c.address ?? '', phone: c.phone ?? '', email: c.email ?? '', isActive: c.isActive,
    })
    setModalOpen(true)
  }

  const createMutation = useMutation({
    mutationFn: createBillingClient,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['billing-clients'] }); setModalOpen(false) },
    onError: () => setError('Error al crear cliente'),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<BillingClient> }) => updateBillingClient(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['billing-clients'] }); setModalOpen(false) },
    onError: () => setError('Error al actualizar cliente'),
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
          <h1 className="text-2xl font-bold text-gray-900">Clientes de Facturación</h1>
          <p className="text-sm text-gray-500 mt-0.5">Catálogo de clientes para DTE</p>
        </div>
        <Button onClick={openCreate}><Plus className="h-4 w-4" /> Nuevo cliente</Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>Nombre</Th>
              <Th>Tipo Doc.</Th>
              <Th>N° Documento</Th>
              <Th>NRC</Th>
              <Th>Correo</Th>
              <Th>Estado</Th>
              <Th className="text-right">Acciones</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={7} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !data?.content?.length ? (
              <Tr><Td colSpan={7} className="text-center py-12 text-gray-400">No se encontraron clientes</Td></Tr>
            ) : data.content.map(c => (
              <Tr key={c.id}>
                <Td className="font-medium">{c.name}</Td>
                <Td>{c.documentType}</Td>
                <Td className="font-mono text-xs">{c.documentNumber}</Td>
                <Td>{c.nrc ?? '—'}</Td>
                <Td>{c.email ?? '—'}</Td>
                <Td><Badge color={c.isActive ? 'green' : 'red'}>{c.isActive ? 'Activo' : 'Inactivo'}</Badge></Td>
                <Td className="text-right">
                  <Button variant="ghost" size="sm" onClick={() => openEdit(c)}><Pencil className="h-3.5 w-3.5" /></Button>
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar cliente' : 'Nuevo cliente'} size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input label="Nombre / Razón Social" error={errors.name?.message} {...register('name')} />
          <div className="grid grid-cols-2 gap-3">
            <Select label="Tipo de documento" error={errors.documentType?.message} {...register('documentType')}>
              <option value="DUI">DUI</option>
              <option value="NIT">NIT</option>
              <option value="PASAPORTE">Pasaporte</option>
              <option value="NRC">NRC</option>
            </Select>
            <Input label="N° Documento" error={errors.documentNumber?.message} {...register('documentNumber')} />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Input label="NRC" {...register('nrc')} />
            <Input label="Teléfono" {...register('phone')} />
          </div>
          <Input label="Dirección" {...register('address')} />
          <Input label="Correo electrónico" type="email" {...register('email')} />
          <div className="flex items-center gap-2">
            <input type="checkbox" id="clienteActive" {...register('isActive')} className="h-4 w-4 rounded border-gray-300 text-brand-600 focus:ring-brand-500" />
            <label htmlFor="clienteActive" className="text-sm text-gray-700">Cliente activo</label>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" loading={isSubmitting || createMutation.isPending || updateMutation.isPending}>
              {editing ? 'Guardar cambios' : 'Crear cliente'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
