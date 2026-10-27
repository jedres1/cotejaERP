import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, Pencil, Trash2 } from 'lucide-react'
import { getAccounts, createAccount, updateAccount, deleteAccount, Account } from '../../api/accounting'
import { Table, Thead, Tbody, Tr, Th, Td } from '../../components/ui/Table'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Select from '../../components/ui/Select'
import Modal from '../../components/ui/Modal'
import ConfirmDialog from '../../components/ui/ConfirmDialog'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'
import Alert from '../../components/ui/Alert'
import Badge from '../../components/ui/Badge'

const schema = z.object({
  code: z.string().min(1, 'Código requerido'),
  name: z.string().min(1, 'Nombre requerido'),
  type: z.string().min(1, 'Tipo requerido'),
  parentId: z.coerce.number().optional().nullable(),
  isLeaf: z.boolean().default(true),
})
type FormData = z.infer<typeof schema>

const typeColorMap: Record<string, 'blue' | 'green' | 'red' | 'yellow' | 'gray'> = {
  activo: 'green',
  pasivo: 'red',
  capital: 'blue',
  ingreso: 'green',
  egreso: 'red',
}

export default function AccountCatalogPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Account | null>(null)
  const [deleting, setDeleting] = useState<Account | null>(null)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['accounts', page], queryFn: () => getAccounts(page) })

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { type: 'activo', isLeaf: true },
  })

  const openCreate = () => {
    setEditing(null)
    reset({ code: '', name: '', type: 'activo', parentId: null, isLeaf: true })
    setModalOpen(true)
  }

  const openEdit = (a: Account) => {
    setEditing(a)
    reset({ code: a.code, name: a.name, type: a.type, parentId: a.parentId, isLeaf: a.isLeaf })
    setModalOpen(true)
  }

  const createMutation = useMutation({
    mutationFn: createAccount,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['accounts'] }); setModalOpen(false) },
    onError: () => setError('Error al crear cuenta'),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Account> }) => updateAccount(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['accounts'] }); setModalOpen(false) },
    onError: () => setError('Error al actualizar cuenta'),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteAccount(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['accounts'] }); setDeleting(null) },
    onError: () => setError('Error al eliminar cuenta'),
  })

  const onSubmit = (data: FormData) => {
    if (editing) {
      updateMutation.mutate({ id: editing.id, data })
    } else {
      createMutation.mutate(data)
    }
  }

  const accounts = data?.content ?? []

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Catálogo de Cuentas</h1>
          <p className="text-sm text-gray-500 mt-0.5">Plan de cuentas contables</p>
        </div>
        <Button onClick={openCreate}><Plus className="h-4 w-4" /> Nueva cuenta</Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>Código</Th>
              <Th>Nombre</Th>
              <Th>Tipo</Th>
              <Th>¿Hoja?</Th>
              <Th className="text-right">Acciones</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={5} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !accounts.length ? (
              <Tr><Td colSpan={5} className="text-center py-12 text-gray-400">No se encontraron cuentas</Td></Tr>
            ) : accounts.map(a => (
              <Tr key={a.id}>
                <Td className="font-mono text-xs font-semibold">{a.code}</Td>
                <Td>{a.name}</Td>
                <Td>
                  <Badge color={typeColorMap[a.type?.toLowerCase()] ?? 'gray'}>{a.type}</Badge>
                </Td>
                <Td>{a.isLeaf ? <Badge color="green">Sí</Badge> : <Badge color="gray">No</Badge>}</Td>
                <Td className="text-right">
                  <div className="flex items-center justify-end gap-2">
                    <Button variant="ghost" size="sm" onClick={() => openEdit(a)}><Pencil className="h-3.5 w-3.5" /></Button>
                    <Button variant="ghost" size="sm" className="text-red-500" onClick={() => setDeleting(a)}><Trash2 className="h-3.5 w-3.5" /></Button>
                  </div>
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar cuenta' : 'Nueva cuenta'}>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <Input label="Código" error={errors.code?.message} {...register('code')} />
            <Select label="Tipo" error={errors.type?.message} {...register('type')}>
              <option value="activo">Activo</option>
              <option value="pasivo">Pasivo</option>
              <option value="capital">Capital</option>
              <option value="ingreso">Ingreso</option>
              <option value="egreso">Egreso</option>
            </Select>
          </div>
          <Input label="Nombre" error={errors.name?.message} {...register('name')} />
          <Select label="Cuenta padre (opcional)" {...register('parentId')}>
            <option value="">Sin cuenta padre</option>
            {accounts.filter(a => !a.isLeaf).map(a => (
              <option key={a.id} value={a.id}>{a.code} — {a.name}</option>
            ))}
          </Select>
          <div className="flex items-center gap-2">
            <input type="checkbox" id="isLeaf" {...register('isLeaf')} className="h-4 w-4 rounded border-gray-300 text-brand-600 focus:ring-brand-500" />
            <label htmlFor="isLeaf" className="text-sm text-gray-700">Cuenta de detalle (hoja)</label>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" loading={isSubmitting || createMutation.isPending || updateMutation.isPending}>
              {editing ? 'Guardar cambios' : 'Crear cuenta'}
            </Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!deleting}
        onClose={() => setDeleting(null)}
        onConfirm={() => deleting && deleteMutation.mutate(deleting.id)}
        loading={deleteMutation.isPending}
        message={`¿Eliminar la cuenta "${deleting?.code} — ${deleting?.name}"?`}
      />
    </div>
  )
}
