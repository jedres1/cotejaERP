import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, Pencil } from 'lucide-react'
import { getEmployees, createEmployee, updateEmployee, Employee } from '../../api/payroll'
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
  code: z.string().min(1, 'Código requerido'),
  name: z.string().min(1, 'Nombre requerido'),
  email: z.string().email('Correo inválido').or(z.literal('')).optional(),
  dui: z.string().min(1, 'DUI requerido'),
  isssNumber: z.string().optional(),
  nup: z.string().optional(),
  afp: z.string().optional(),
  position: z.string().min(1, 'Cargo requerido'),
  department: z.string().optional(),
  hireDate: z.string().min(1, 'Fecha de ingreso requerida'),
  salary: z.coerce.number().min(0, 'Salario inválido'),
  bankName: z.string().optional(),
  bankAccount: z.string().optional(),
  isActive: z.boolean().default(true),
})
type FormData = z.infer<typeof schema>

const fmtCurrency = (v: number) => `$${v.toFixed(2)}`

export default function EmployeesPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Employee | null>(null)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['employees', page], queryFn: () => getEmployees(page) })

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { isActive: true },
  })

  const openCreate = () => {
    setEditing(null)
    reset({ code: '', name: '', email: '', dui: '', position: '', hireDate: '', salary: 0, isActive: true })
    setModalOpen(true)
  }

  const openEdit = (e: Employee) => {
    setEditing(e)
    reset({
      code: e.code, name: e.name, email: e.email ?? '', dui: e.dui,
      isssNumber: e.isssNumber ?? '', nup: e.nup ?? '', afp: e.afp ?? '',
      position: e.position, department: e.department ?? '',
      hireDate: e.hireDate?.split('T')[0] ?? '', salary: e.salary,
      bankName: e.bankName ?? '', bankAccount: e.bankAccount ?? '', isActive: e.isActive,
    })
    setModalOpen(true)
  }

  const createMutation = useMutation({
    mutationFn: createEmployee,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['employees'] }); setModalOpen(false) },
    onError: () => setError('Error al crear empleado'),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Employee> }) => updateEmployee(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['employees'] }); setModalOpen(false) },
    onError: () => setError('Error al actualizar empleado'),
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
          <h1 className="text-2xl font-bold text-gray-900">Empleados</h1>
          <p className="text-sm text-gray-500 mt-0.5">Gestión de planilla de empleados</p>
        </div>
        <Button onClick={openCreate}><Plus className="h-4 w-4" /> Nuevo empleado</Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>Código</Th>
              <Th>Nombre</Th>
              <Th>DUI</Th>
              <Th>Cargo</Th>
              <Th>Salario</Th>
              <Th>Estado</Th>
              <Th className="text-right">Acciones</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={7} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !data?.content?.length ? (
              <Tr><Td colSpan={7} className="text-center py-12 text-gray-400">No se encontraron empleados</Td></Tr>
            ) : data.content.map(e => (
              <Tr key={e.id}>
                <Td className="font-mono text-xs">{e.code}</Td>
                <Td className="font-medium">{e.name}</Td>
                <Td className="font-mono text-xs">{e.dui}</Td>
                <Td>{e.position}</Td>
                <Td>{fmtCurrency(e.salary)}</Td>
                <Td><Badge color={e.isActive ? 'green' : 'red'}>{e.isActive ? 'Activo' : 'Inactivo'}</Badge></Td>
                <Td className="text-right">
                  <Button variant="ghost" size="sm" onClick={() => openEdit(e)}><Pencil className="h-3.5 w-3.5" /></Button>
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar empleado' : 'Nuevo empleado'} size="xl">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <Input label="Código" error={errors.code?.message} {...register('code')} />
            <Input label="DUI" error={errors.dui?.message} {...register('dui')} />
          </div>
          <Input label="Nombre completo" error={errors.name?.message} {...register('name')} />
          <div className="grid grid-cols-2 gap-3">
            <Input label="Correo electrónico" type="email" {...register('email')} />
            <Input label="Cargo" error={errors.position?.message} {...register('position')} />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Input label="Departamento" {...register('department')} />
            <Input label="Fecha de ingreso" type="date" error={errors.hireDate?.message} {...register('hireDate')} />
          </div>
          <div className="grid grid-cols-3 gap-3">
            <Input label="N° ISSS" {...register('isssNumber')} />
            <Input label="NUP" {...register('nup')} />
            <Select label="AFP" {...register('afp')}>
              <option value="">Sin AFP</option>
              <option value="crecer">Crecer</option>
              <option value="confia">Confía</option>
            </Select>
          </div>
          <Input label="Salario ($)" type="number" step="0.01" error={errors.salary?.message} {...register('salary')} />
          <div className="grid grid-cols-2 gap-3">
            <Input label="Banco" {...register('bankName')} />
            <Input label="N° Cuenta bancaria" {...register('bankAccount')} />
          </div>
          <div className="flex items-center gap-2">
            <input type="checkbox" id="empActive" {...register('isActive')} className="h-4 w-4 rounded border-gray-300 text-brand-600 focus:ring-brand-500" />
            <label htmlFor="empActive" className="text-sm text-gray-700">Empleado activo</label>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" loading={isSubmitting || createMutation.isPending || updateMutation.isPending}>
              {editing ? 'Guardar cambios' : 'Crear empleado'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
