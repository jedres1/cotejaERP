import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, Pencil } from 'lucide-react'
import { getPlans, createPlan, updatePlan, Plan } from '../../api/admin'
import { Table, Thead, Tbody, Tr, Th, Td } from '../../components/ui/Table'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Modal from '../../components/ui/Modal'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'
import Alert from '../../components/ui/Alert'
import Badge from '../../components/ui/Badge'

const ALL_MODULES = ['billing', 'accounting', 'payroll', 'purchases', 'inventory', 'banking']

const schema = z.object({
  name: z.string().min(1, 'Nombre requerido'),
  description: z.string().optional(),
  price: z.coerce.number().min(0, 'Precio inválido'),
  isActive: z.boolean().default(true),
  moduleAccesses: z.array(z.string()).default([]),
})
type FormData = z.infer<typeof schema>

export default function PlansPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Plan | null>(null)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['plans', page], queryFn: () => getPlans(page) })

  const { register, handleSubmit, reset, watch, setValue, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { moduleAccesses: [], isActive: true },
  })

  const selectedModules = watch('moduleAccesses') ?? []
  const toggleModule = (mod: string) => {
    const current = selectedModules
    setValue('moduleAccesses', current.includes(mod) ? current.filter(m => m !== mod) : [...current, mod])
  }

  const openCreate = () => {
    setEditing(null)
    reset({ name: '', description: '', price: 0, isActive: true, moduleAccesses: [] })
    setModalOpen(true)
  }

  const openEdit = (p: Plan) => {
    setEditing(p)
    reset({ name: p.name, description: p.description ?? '', price: p.price, isActive: p.isActive, moduleAccesses: p.moduleAccesses ?? [] })
    setModalOpen(true)
  }

  const createMutation = useMutation({
    mutationFn: createPlan,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['plans'] }); setModalOpen(false) },
    onError: () => setError('Error al crear plan'),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Plan> }) => updatePlan(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['plans'] }); setModalOpen(false) },
    onError: () => setError('Error al actualizar plan'),
  })

  const onSubmit = (data: FormData) => {
    if (editing) {
      updateMutation.mutate({ id: editing.id, data })
    } else {
      createMutation.mutate(data)
    }
  }

  const fmtCurrency = (v: number) => `$${v.toFixed(2)}`

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Planes</h1>
          <p className="text-sm text-gray-500 mt-0.5">Gestión de planes de suscripción</p>
        </div>
        <Button onClick={openCreate}><Plus className="h-4 w-4" /> Nuevo plan</Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>Nombre</Th>
              <Th>Precio</Th>
              <Th>Módulos</Th>
              <Th>Estado</Th>
              <Th className="text-right">Acciones</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={5} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !data?.content?.length ? (
              <Tr><Td colSpan={5} className="text-center py-12 text-gray-400">No se encontraron planes</Td></Tr>
            ) : data.content.map(p => (
              <Tr key={p.id}>
                <Td className="font-medium">{p.name}</Td>
                <Td>{fmtCurrency(p.price)}</Td>
                <Td>
                  <div className="flex flex-wrap gap-1">
                    {(p.moduleAccesses ?? []).map(m => <Badge key={m} color="blue">{m}</Badge>)}
                  </div>
                </Td>
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

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar plan' : 'Nuevo plan'} size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input label="Nombre del plan" error={errors.name?.message} {...register('name')} />
          <Input label="Descripción" {...register('description')} />
          <Input label="Precio mensual ($)" type="number" step="0.01" error={errors.price?.message} {...register('price')} />
          <div className="flex items-center gap-2">
            <input type="checkbox" id="planActive" {...register('isActive')} className="h-4 w-4 rounded border-gray-300 text-brand-600 focus:ring-brand-500" />
            <label htmlFor="planActive" className="text-sm text-gray-700">Plan activo</label>
          </div>
          <div>
            <p className="text-sm font-medium text-gray-700 mb-2">Módulos incluidos</p>
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
              {editing ? 'Guardar cambios' : 'Crear plan'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
