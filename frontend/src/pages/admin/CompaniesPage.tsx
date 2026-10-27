import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, Pencil, CheckCircle, XCircle } from 'lucide-react'
import { getCompanies, createCompany, updateCompany, getCustomers, Company } from '../../api/admin'
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
  customerId: z.coerce.number().min(1, 'Cliente requerido'),
  businessName: z.string().min(1, 'Razón social requerida'),
  tradeName: z.string().optional(),
  nitDui: z.string().min(1, 'NIT requerido'),
  nrc: z.string().optional(),
  address: z.string().optional(),
  email: z.string().email('Correo inválido').or(z.literal('')).optional(),
  phone: z.string().optional(),
  environment: z.string().default('pruebas'),
  moduleAccesses: z.array(z.string()).default([]),
})
type FormData = z.infer<typeof schema>

export default function CompaniesPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Company | null>(null)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['companies', page], queryFn: () => getCompanies(page) })
  const { data: customersData } = useQuery({ queryKey: ['customers', 0], queryFn: () => getCustomers(0) })

  const { register, handleSubmit, reset, watch, setValue, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { moduleAccesses: [], environment: 'pruebas' },
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
    reset({ moduleAccesses: [], environment: 'pruebas' })
    setModalOpen(true)
  }

  const openEdit = (c: Company) => {
    setEditing(c)
    reset({
      customerId: c.customerId,
      businessName: c.businessName,
      tradeName: c.tradeName ?? '',
      nitDui: c.nitDui,
      nrc: c.nrc ?? '',
      address: c.address ?? '',
      email: c.email ?? '',
      phone: c.phone ?? '',
      environment: c.environment ?? 'pruebas',
      moduleAccesses: c.moduleAccesses ?? [],
    })
    setModalOpen(true)
  }

  const createMutation = useMutation({
    mutationFn: createCompany,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['companies'] }); setModalOpen(false) },
    onError: () => setError('Error al crear empresa'),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Company> }) => updateCompany(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['companies'] }); setModalOpen(false) },
    onError: () => setError('Error al actualizar empresa'),
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
          <h1 className="text-2xl font-bold text-gray-900">Empresas</h1>
          <p className="text-sm text-gray-500 mt-0.5">Gestión de empresas del sistema</p>
        </div>
        <Button onClick={openCreate}><Plus className="h-4 w-4" /> Nueva empresa</Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>Razón Social</Th>
              <Th>NIT/DUI</Th>
              <Th>Módulos</Th>
              <Th>Licencia</Th>
              <Th className="text-right">Acciones</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={5} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !data?.content?.length ? (
              <Tr><Td colSpan={5} className="text-center py-12 text-gray-400">No se encontraron empresas</Td></Tr>
            ) : data.content.map(c => (
              <Tr key={c.id}>
                <Td className="font-medium">{c.businessName}</Td>
                <Td>{c.nitDui}</Td>
                <Td>
                  <div className="flex flex-wrap gap-1">
                    {(c.moduleAccesses ?? []).map(m => (
                      <Badge key={m} color="blue">{m}</Badge>
                    ))}
                  </div>
                </Td>
                <Td>
                  {c.hasActiveLicense
                    ? <span className="flex items-center gap-1 text-green-600 text-xs font-medium"><CheckCircle className="h-3.5 w-3.5" /> Activa</span>
                    : <span className="flex items-center gap-1 text-red-500 text-xs font-medium"><XCircle className="h-3.5 w-3.5" /> Inactiva</span>
                  }
                </Td>
                <Td className="text-right">
                  <Button variant="ghost" size="sm" onClick={() => openEdit(c)}>
                    <Pencil className="h-3.5 w-3.5" />
                  </Button>
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar empresa' : 'Nueva empresa'} size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Select label="Cliente" error={errors.customerId?.message} {...register('customerId')}>
            <option value="">Seleccionar cliente</option>
            {customersData?.content?.map(c => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </Select>
          <div className="grid grid-cols-2 gap-3">
            <Input label="Razón Social" error={errors.businessName?.message} {...register('businessName')} />
            <Input label="Nombre Comercial" {...register('tradeName')} />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Input label="NIT / DUI" error={errors.nitDui?.message} {...register('nitDui')} />
            <Input label="NRC" {...register('nrc')} />
          </div>
          <Input label="Dirección" {...register('address')} />
          <div className="grid grid-cols-2 gap-3">
            <Input label="Correo" type="email" {...register('email')} />
            <Input label="Teléfono" {...register('phone')} />
          </div>
          <Select label="Ambiente" {...register('environment')}>
            <option value="pruebas">Pruebas</option>
            <option value="produccion">Producción</option>
          </Select>
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
              {editing ? 'Guardar cambios' : 'Crear empresa'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
