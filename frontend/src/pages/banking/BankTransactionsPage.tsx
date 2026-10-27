import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, TrendingUp, TrendingDown } from 'lucide-react'
import { getBankTransactions, createBankTransaction, getBankAccounts } from '../../api/banking'
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
  bankAccountId: z.coerce.number().min(1, 'Cuenta requerida'),
  transactionDate: z.string().min(1, 'Fecha requerida'),
  description: z.string().min(1, 'Descripción requerida'),
  transactionType: z.string().min(1, 'Tipo requerido'),
  amount: z.coerce.number().min(0.01, 'Monto inválido'),
  reference: z.string().optional(),
})
type FormData = z.infer<typeof schema>

const fmtCurrency = (v: number) => `$${v.toFixed(2)}`
const fmtDate = (d: string) => d ? new Date(d).toLocaleDateString('es-SV') : '—'

const isDebit = (type: string) => ['retiro', 'cargo'].includes(type?.toLowerCase())

export default function BankTransactionsPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['bank-transactions', page], queryFn: () => getBankTransactions(page) })
  const { data: accountsData } = useQuery({ queryKey: ['bank-accounts', 0], queryFn: () => getBankAccounts(0) })

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      transactionType: 'deposito',
      transactionDate: new Date().toISOString().split('T')[0],
    },
  })

  const createMutation = useMutation({
    mutationFn: createBankTransaction,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['bank-transactions'] }); setModalOpen(false); reset() },
    onError: () => setError('Error al registrar transacción'),
  })

  const onSubmit = (data: FormData) => createMutation.mutate(data)

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Transacciones Bancarias</h1>
          <p className="text-sm text-gray-500 mt-0.5">Movimientos de cuentas bancarias</p>
        </div>
        <Button onClick={() => { reset(); setModalOpen(true) }}><Plus className="h-4 w-4" /> Nueva transacción</Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>Fecha</Th>
              <Th>Descripción</Th>
              <Th>Cuenta</Th>
              <Th>Tipo</Th>
              <Th>Monto</Th>
              <Th>Referencia</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={6} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !data?.content?.length ? (
              <Tr><Td colSpan={6} className="text-center py-12 text-gray-400">No se encontraron transacciones</Td></Tr>
            ) : data.content.map(t => (
              <Tr key={t.id}>
                <Td>{fmtDate(t.transactionDate)}</Td>
                <Td className="max-w-[180px] truncate">{t.description}</Td>
                <Td>{t.bankAccountName}</Td>
                <Td>
                  <div className="flex items-center gap-1">
                    {isDebit(t.transactionType)
                      ? <TrendingDown className="h-3.5 w-3.5 text-red-500" />
                      : <TrendingUp className="h-3.5 w-3.5 text-green-500" />
                    }
                    <Badge color={isDebit(t.transactionType) ? 'red' : 'green'}>{t.transactionType}</Badge>
                  </div>
                </Td>
                <Td className={`font-semibold ${isDebit(t.transactionType) ? 'text-red-600' : 'text-green-600'}`}>
                  {isDebit(t.transactionType) ? '-' : '+'}{fmtCurrency(t.amount)}
                </Td>
                <Td className="font-mono text-xs">{t.reference ?? '—'}</Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Nueva Transacción" size="lg">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Select label="Cuenta bancaria" error={errors.bankAccountId?.message} {...register('bankAccountId')}>
            <option value="">Seleccionar cuenta</option>
            {accountsData?.content?.map(a => (
              <option key={a.id} value={a.id}>{a.name} — {a.bankName}</option>
            ))}
          </Select>
          <div className="grid grid-cols-2 gap-3">
            <Input label="Fecha" type="date" error={errors.transactionDate?.message} {...register('transactionDate')} />
            <Select label="Tipo" error={errors.transactionType?.message} {...register('transactionType')}>
              <option value="deposito">Depósito</option>
              <option value="retiro">Retiro</option>
              <option value="cargo">Cargo</option>
              <option value="abono">Abono</option>
            </Select>
          </div>
          <Input label="Descripción" error={errors.description?.message} {...register('description')} />
          <div className="grid grid-cols-2 gap-3">
            <Input label="Monto ($)" type="number" step="0.01" error={errors.amount?.message} {...register('amount')} />
            <Input label="Referencia" {...register('reference')} placeholder="N° cheque, transferencia..." />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" loading={isSubmitting || createMutation.isPending}>Registrar</Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
