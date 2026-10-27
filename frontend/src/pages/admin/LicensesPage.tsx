import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getLicenses } from '../../api/admin'
import { Table, Thead, Tbody, Tr, Th, Td } from '../../components/ui/Table'
import Pagination from '../../components/ui/Pagination'
import Spinner from '../../components/ui/Spinner'
import Badge, { statusBadge } from '../../components/ui/Badge'

const fmtDate = (d: string) => d ? new Date(d).toLocaleDateString('es-SV') : '—'

export default function LicensesPage() {
  const [page, setPage] = useState(0)

  const { data, isLoading } = useQuery({
    queryKey: ['licenses', page],
    queryFn: () => getLicenses(page),
  })

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Licencias</h1>
        <p className="text-sm text-gray-500 mt-0.5">Licencias asignadas a clientes</p>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <Table>
          <Thead>
            <Tr>
              <Th>Cliente</Th>
              <Th>Plan</Th>
              <Th>Inicio</Th>
              <Th>Vencimiento</Th>
              <Th>Estado</Th>
            </Tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <Tr><Td colSpan={5} className="text-center py-12"><Spinner className="mx-auto" /></Td></Tr>
            ) : !data?.content?.length ? (
              <Tr><Td colSpan={5} className="text-center py-12 text-gray-400">No se encontraron licencias</Td></Tr>
            ) : data.content.map(l => (
              <Tr key={l.id}>
                <Td className="font-medium">{l.customerName}</Td>
                <Td>{l.planName}</Td>
                <Td>{fmtDate(l.startDate)}</Td>
                <Td>{fmtDate(l.endDate)}</Td>
                <Td>
                  <Badge color={statusBadge(l.status)}>{l.status}</Badge>
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
        {data && <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />}
      </div>
    </div>
  )
}
