import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm, useFieldArray } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Plus, CheckCircle, Ban, Trash2 } from 'lucide-react'
import {
  getJournalEntries, createJournalEntry, approveJournalEntry, anularJournalEntry,
  getAccounts, JournalEntry
} from '../../api/accounting'
import { Table, Thead, Tbody, Tr, Th, Td } from '../../components/ui/Table'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import Select from '../../components/ui/Select'
import Modal from '../../components/ui/Modal'
import ConfirmDialog from '../../components/ui/ConfirmDialog'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'
import Alert from '../../components/ui/Alert'
import Badge, { statusBadge } from '../../components/ui/Badge'

const lineSchema = z.object({
  accountId: z.coerce.number().min(1, 'Cuenta requerida'),
  description: z.string().optional(),
  debit: z.coerce.number().min(0),
  credit: z.coerce.number().min(0),
})

const schema = z.object({
  entryDate: z.string().min(1, 'Fecha requerida'),
  description: z.string().min(1, 'Descripción requerida'),
  reference: z.string().optional(),
  lines: z.array(lineSchema).min(2, 'Mínimo 2 líneas'),
})
type FormData = z.infer<typeof schema>

const fmtCurrency = (v: number) => `$${v.toFixed(2)}`
const fmtDate = (d: string) => d ? new Date(d).toLocaleDateString('es-SV') : '—'

export default function JournalPage() {
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [anulando, setAnulando] = useState<JournalEntry | null>(null)
  const [error, setError] = useState<string | null>(null)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['journal', page], queryFn: () => getJournalEntries(page) })
  const { data: accountsData } = useQuery({ queryKey: ['accounts', 0], queryFn: () => getAccounts(0) })

  const { register, handleSubmit, control, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      entryDate: new Date().toISOString().split('T')[0],
      lines: [
        { accountId: 0, description: '', debit: 0, credit: 0 },
        { accountId: 0, description: '', debit: 0, credit: 0 },
      ],
    },
  })

  const { fields, append, remove } = useFieldArray({ control, name: 'lines' })

  const leafAccounts = (accountsData?.content ?? []).filter(a => a.isLeaf)

  const createMutation = useMutation({
    mutationFn: createJournalEntry,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['journal'] }); setModalOpen(false); reset() },
    onError: () => setError('Error al crear partida'),
  })

  const approveMutation = useMutation({
    mutationFn: (id: number) => approveJournalEntry(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['journal'] }),
    onError: () => setError('Error al aprobar partida'),
  })

  const anularMutation = useMutation({
    mutationFn: (id: number) => anularJournalEntry(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['journal'] }); setAnulando(null) },
    onError: () => setError('Error al anular partida'),
  })

  const onSubmit = (data: FormData) => createMutation.mutate(data)

  const isBorrador = (status: string) => status?.toLowerCase() === 'borrador'
  const isAnulable = (status: string) => !['anulado'].includes(status?.toLowerCase())

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Libro Diario</h1>
          <p className="text-sm text-gray-500 mt-0.5">Partidas contables</p>
        </div>
        <Button onClick={() => { reset(); setModalOpen(true) }}><Plus className="h-4 w-4" /> Nueva partida</Button>
      </div>

      {error && <Alert type="error" onClose={() => setError(null)}>{error}</Alert>}

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>N°</Th>
              <Th>Fecha</Th>
              <Th>Descripción</Th>
              <Th>Debe</Th>
              <Th>Haber</Th>
              <Th>Estado</Th>
              <Th className="text-right">Acciones</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={7} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !data?.content?.length ? (
              <Tr><Td colSpan={7} className="text-center py-12 text-gray-400">No se encontraron partidas</Td></Tr>
            ) : data.content.map(e => (
              <Tr key={e.id}>
                <Td className="font-mono text-xs">{e.entryNumber}</Td>
                <Td>{fmtDate(e.entryDate)}</Td>
                <Td className="max-w-[200px] truncate">{e.description}</Td>
                <Td>{fmtCurrency(e.totalDebit)}</Td>
                <Td>{fmtCurrency(e.totalCredit)}</Td>
                <Td><Badge color={statusBadge(e.status)}>{e.status}</Badge></Td>
                <Td className="text-right">
                  <div className="flex items-center justify-end gap-1">
                    {isBorrador(e.status) && (
                      <Button variant="ghost" size="sm" className="text-green-600" onClick={() => approveMutation.mutate(e.id)}>
                        <CheckCircle className="h-3.5 w-3.5" />
                      </Button>
                    )}
                    {isAnulable(e.status) && (
                      <Button variant="ghost" size="sm" className="text-red-500" onClick={() => setAnulando(e)}>
                        <Ban className="h-3.5 w-3.5" />
                      </Button>
                    )}
                  </div>
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Nueva Partida Contable" size="xl">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <Input label="Fecha" type="date" error={errors.entryDate?.message} {...register('entryDate')} />
            <Input label="Referencia" {...register('reference')} />
          </div>
          <Input label="Descripción" error={errors.description?.message} {...register('description')} />

          <div>
            <div className="flex items-center justify-between mb-2">
              <p className="text-sm font-medium text-gray-700">Líneas de la partida</p>
              <Button
                type="button"
                variant="secondary"
                size="sm"
                onClick={() => append({ accountId: 0, description: '', debit: 0, credit: 0 })}
              >
                <Plus className="h-3.5 w-3.5" /> Línea
              </Button>
            </div>
            {errors.lines?.root && <p className="text-xs text-red-500 mb-2">{errors.lines.root.message}</p>}
            <div className="space-y-2">
              <div className="grid grid-cols-12 gap-1 text-xs font-medium text-gray-500 px-1">
                <span className="col-span-4">Cuenta</span>
                <span className="col-span-3">Descripción</span>
                <span className="col-span-2">Debe</span>
                <span className="col-span-2">Haber</span>
                <span className="col-span-1"></span>
              </div>
              {fields.map((field, idx) => (
                <div key={field.id} className="grid grid-cols-12 gap-1 items-start">
                  <div className="col-span-4">
                    <Select {...register(`lines.${idx}.accountId`)} error={errors.lines?.[idx]?.accountId?.message}>
                      <option value="">Cuenta</option>
                      {leafAccounts.map(a => (
                        <option key={a.id} value={a.id}>{a.code} — {a.name}</option>
                      ))}
                    </Select>
                  </div>
                  <div className="col-span-3">
                    <Input placeholder="Descripción" {...register(`lines.${idx}.description`)} />
                  </div>
                  <div className="col-span-2">
                    <Input type="number" step="0.01" placeholder="0.00" {...register(`lines.${idx}.debit`)} />
                  </div>
                  <div className="col-span-2">
                    <Input type="number" step="0.01" placeholder="0.00" {...register(`lines.${idx}.credit`)} />
                  </div>
                  <div className="col-span-1 flex justify-center pt-1">
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      className="text-red-400"
                      onClick={() => remove(idx)}
                      disabled={fields.length <= 2}
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <Button type="button" variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" loading={isSubmitting || createMutation.isPending}>Crear partida</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!anulando}
        onClose={() => setAnulando(null)}
        onConfirm={() => anulando && anularMutation.mutate(anulando.id)}
        loading={anularMutation.isPending}
        title="¿Anular partida?"
        message="¿Deseas anular esta partida contable? Esta acción no se puede deshacer."
        confirmLabel="Anular"
      />
    </div>
  )
}
