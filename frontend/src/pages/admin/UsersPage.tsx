import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, Pencil } from 'lucide-react'
import { getUsers, createUser, updateUser, User } from '../../api/admin'
import { Table, Thead, Tbody, Tr, Th, Td } from '../../components/ui/Table'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Select from '../../components/ui/Select'
import Modal from '../../components/ui/Modal'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'
import Alert from '../../components/ui/Alert'
import Badge from '../../components/ui/Badge'

const ALL_MODULES = ['billing', 'accounting', 'payroll', 'purchases', 'inventory', 'banking']

const schema = z.object({
  name: z.string().min(1, 'Nombre requerido'),
  email: z.string().email('Correo inválido'),
  role: z.enum(['ADMIN', 'INQUILINO']),
  isActive: z.boolean().default(true),
  moduleAccesses: z.array(z.string()).default([]),
  password: z.string().optional(),
})

type FormData = z.infer<typeof schema>

export default function UsersPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<User | null>(null)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['users', page], queryFn: () => getUsers(page) })

  const { register, handleSubmit, reset, watch, setValue, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { role: 'INQUILINO', isActive: true, moduleAccesses: [], password: '' },
  })

  const selectedModules = watch('moduleAccesses') ?? []

  const toggleModule = (mod: string) => {
    const current = selectedModules
    if (current.includes(mod)) {
      setValue('moduleAccesses', current.filter(m => m !== mod))
    } else {
      setValue('moduleAccesses', [...current, mod])
    }
  }

  const openCreate = () => {
    setEditing(null)
    reset({ name: '', email: '', role: 'INQUILINO', isActive: true, moduleAccesses: [], password: '' })
    setModalOpen(true)
  }

  const openEdit = (u: User) => {
    setEditing(u)
    reset({ name: u.name, email: u.email, role: u.role, isActive: u.isActive, moduleAccesses: u.moduleAccesses ?? [] })
    setModalOpen(true)
  }

  const createMutation = useMutation({
    mutationFn: createUser,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['users'] }); setModalOpen(false) },
    onError: () => setError('Error al crear usuario'),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<User> }) => updateUser(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['users'] }); setModalOpen(false) },
    onError: () => setError('Error al actualizar usuario'),
  })

  const onSubmit = (data: FormData) => {
    if (editing) {
      const { password: _p, ...rest } = data
      updateMutation.mutate({ id: editing.id, data: rest })
    } else {
      createMutation.mutate(data)
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Usuarios</h1>
          <p className="text-sm text-gray-500 mt-0.5">Gestión de usuarios del sistema</p>
        </div>
        <Button onClick={openCreate}><Plus className="h-4 w-4" /> Nuevo usuario</Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>Nombre</Th>
              <Th>Email</Th>
              <Th>Rol</Th>
              <Th>Estado</Th>
              <Th className="text-right">Acciones</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={5} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !data?.content?.length ? (
              <Tr><Td colSpan={5} className="text-center py-12 text-gray-400">No se encontraron usuarios</Td></Tr>
            ) : data.content.map(u => (
              <Tr key={u.id}>
                <Td className="font-medium">{u.name}</Td>
                <Td>{u.email}</Td>
                <Td><Badge color={u.role === 'ADMIN' ? 'blue' : 'gray'}>{u.role}</Badge></Td>
                <Td><Badge color={u.isActive ? 'green' : 'red'}>{u.isActive ? 'Activo' : 'Inactivo'}</Badge></Td>
                <Td className="text-right">
                  <Button variant="ghost" size="sm" onClick={() => openEdit(u)}>
                    <Pencil className="h-3.5 w-3.5" />
                  </Button>
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar usuario' : 'Nuevo usuario'} size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input label="Nombre" error={errors.name?.message} {...register('name')} />
          <Input label="Correo electrónico" type="email" error={errors.email?.message} {...register('email')} />
          {!editing && (
            <Input label="Contraseña" type="password" error={errors.password?.message} {...register('password')} />
          )}
          <Select label="Rol" {...register('role')}>
            <option value="ADMIN">ADMIN</option>
            <option value="INQUILINO">INQUILINO</option>
          </Select>
          <div className="flex items-center gap-2">
            <input type="checkbox" id="isActive" {...register('isActive')} className="h-4 w-4 rounded border-gray-300 text-brand-600 focus:ring-brand-500" />
            <label htmlFor="isActive" className="text-sm text-gray-700">Usuario activo</label>
          </div>
          <div>
            <p className="text-sm font-medium text-gray-700 mb-2">Módulos</p>
            <div className="flex flex-wrap gap-2">
              {ALL_MODULES.map(mod => (
                <button
                  key={mod}
                  type="button"
                  onClick={() => toggleModule(mod)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-medium border transition-colors ${
                    selectedModules.includes(mod)
                      ? 'bg-brand-600 text-white border-brand-600'
                      : 'bg-white text-gray-600 border-gray-300 hover:border-brand-400'
                  }`}
                >
                  {mod}
                </button>
              ))}
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" loading={isSubmitting || createMutation.isPending || updateMutation.isPending}>
              {editing ? 'Guardar cambios' : 'Crear usuario'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
