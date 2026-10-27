import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, Pencil } from 'lucide-react'
import { getBankAccounts, createBankAccount, updateBankAccount, BankAccount } from '../../api/banking'
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
  bankName: z.string().min(1, 'Banco requerido'),
  accountNumber: z.string().min(1, 'N° Cuenta requerido'),
  accountType: z.string().default('corriente'),
  currency: z.string().default('USD'),
  currentBalance: z.coerce.number().default(0),
})
type FormData = z.infer<typeof schema>

const fmtCurrency = (v: number) => `$${v.toFixed(2)}`

export default function BankAccountsPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<BankAccount | null>(null)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['bank-accounts', page], queryFn: () => getBankAccounts(page) })

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { accountType: 'corriente', currency: 'USD', currentBalance: 0 },
  })

  const openCreate = () => {
    setEditing(null)
    reset({ name: '', bankName: '', accountNumber: '', accountType: 'corriente', currency: 'USD', currentBalance: 0 })
    setModalOpen(true)
  }

  const openEdit = (a: BankAccount) => {
    setEditing(a)
    reset({
      name: a.name, bankName: a.bankName, accountNumber: a.accountNumber,
      accountType: a.accountType, currency: a.currency, currentBalance: a.currentBalance,
    })
    setModalOpen(true)
  }

  const createMutation = useMutation({
    mutationFn: createBankAccount,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['bank-accounts'] }); setModalOpen(false) },
    onError: () => setError('Error al crear cuenta bancaria'),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<BankAccount> }) => updateBankAccount(id, data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['bank-accounts'] }); setModalOpen(false) },
    onError: () => setError('Error al actualizar cuenta bancaria'),
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
          <h1 className="text-2xl font-bold text-gray-900">Cuentas Bancarias</h1>
          <p className="text-sm text-gray-500 mt-0.5">Control bancario de la empresa</p>
        </div>
        <Button onClick={openCreate}><Plus className="h-4 w-4" /> Nueva cuenta</Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>Nombre</Th>
              <Th>Banco</Th>
              <Th>N° Cuenta</Th>
              <Th>Tipo</Th>
              <Th>Saldo</Th>
              <Th>Moneda</Th>
              <Th className="text-right">Acciones</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={7} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !data?.content?.length ? (
              <Tr><Td colSpan={7} className="text-center py-12 text-gray-400">No se encontraron cuentas bancarias</Td></Tr>
            ) : data.content.map(a => (
              <Tr key={a.id}>
                <Td className="font-medium">{a.name}</Td>
                <Td>{a.bankName}</Td>
                <Td className="font-mono text-xs">{a.accountNumber}</Td>
                <Td><Badge color="blue">{a.accountType}</Badge></Td>
                <Td className="font-semibold text-green-700">{fmtCurrency(a.currentBalance)}</Td>
                <Td>{a.currency}</Td>
                <Td className="text-right">
                  <Button variant="ghost" size="sm" onClick={() => openEdit(a)}><Pencil className="h-3.5 w-3.5" /></Button>
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar cuenta' : 'Nueva cuenta bancaria'} size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input label="Nombre descriptivo" error={errors.name?.message} {...register('name')} placeholder="Ej: Cuenta Operaciones Davivienda" />
          <div className="grid grid-cols-2 gap-3">
            <Input label="Banco" error={errors.bankName?.message} {...register('bankName')} />
            <Input label="N° de cuenta" error={errors.accountNumber?.message} {...register('accountNumber')} />
          </div>
          <div className="grid grid-cols-3 gap-3">
            <Select label="Tipo de cuenta" {...register('accountType')}>
              <option value="corriente">Corriente</option>
              <option value="ahorros">Ahorros</option>
              <option value="credito">Crédito</option>
            </Select>
            <Select label="Moneda" {...register('currency')}>
              <option value="USD">USD</option>
              <option value="EUR">EUR</option>
            </Select>
            <Input label="Saldo inicial ($)" type="number" step="0.01" {...register('currentBalance')} />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" loading={isSubmitting || createMutation.isPending || updateMutation.isPending}>
              {editing ? 'Guardar cambios' : 'Crear cuenta'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
