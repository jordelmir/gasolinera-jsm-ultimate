
"use client";

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { getRaffles, closeRafflePeriod, executeRaffleDraw, Raffle, RaffleWinner } from "@/lib/apiClient";
import toast from 'react-hot-toast';

export default function RafflesPage() {
  const queryClient = useQueryClient();

  const { data: raffles, isLoading, isError, error } = useQuery<Raffle[], Error>({ 
    queryKey: ['raffles'],
    queryFn: getRaffles,
  });

  const closePeriodMutation = useMutation<Raffle, Error, string>({
    mutationFn: closeRafflePeriod,
    onSuccess: () => {
      toast.success(`Period closed successfully!`);
      queryClient.invalidateQueries({ queryKey: ['raffles'] }); // Invalidate and refetch raffles
    },
    onError: (err) => {
      let errorMessage = `Error closing period: An unknown error occurred.`;
      if (err.status) {
        errorMessage = `Error closing period: Status ${err.status}`;
      }
      if (err.data && err.data.message) {
        errorMessage = `Error closing period: ${err.data.message}`;
      } else if (err.message) {
        errorMessage = `Error closing period: ${err.message}`;
      }
      toast.error(errorMessage);
    },
  });

  const handleClosePeriod = (period: string) => {
    if (confirm(`¿Estás seguro de que quieres cerrar el período ${period} y construir el Merkle Tree?`)) {
      closePeriodMutation.mutate(period);
    }
  };

  const executeDrawMutation = useMutation<RaffleWinner, Error, number>({
    mutationFn: executeRaffleDraw,
    onSuccess: () => {
      toast.success(`Raffle drawn successfully!`);
      queryClient.invalidateQueries({ queryKey: ['raffles'] }); // Invalidate and refetch raffles
    },
    onError: (err) => {
      let errorMessage = `Error executing draw: An unknown error occurred.`;
      if (err.status) {
        errorMessage = `Error executing draw: Status ${err.status}`;
      }
      if (err.data && err.data.message) {
        errorMessage = `Error executing draw: ${err.data.message}`;
      } else if (err.message) {
        errorMessage = `Error executing draw: ${err.message}`;
      }
      toast.error(errorMessage);
    },
  });

  const handleExecuteDraw = (raffleId: number) => {
    if (confirm(`¿Estás seguro de que quieres ejecutar el sorteo para el ID ${raffleId}? Esto es irreversible.`)) {
      executeDrawMutation.mutate(raffleId);
    }
  };

  // Helper to get current period (e.g., YYYY-MM)
  const getCurrentPeriod = () => {
    const date = new Date();
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    return `${year}-${month}`;
  };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Gestión de Sorteos</h1>
      <Card>
        <CardHeader>
          <div className="flex justify-between items-center">
            <div>
              <CardTitle>Sorteos Activos y Pasados</CardTitle>
              <CardDescription>Aquí podrás gestionar los sorteos de Puntos G.</CardDescription>
            </div>
            <Button onClick={() => handleClosePeriod(getCurrentPeriod())} disabled={closePeriodMutation.isPending || executeDrawMutation.isPending}>
              Cerrar Período Actual ({getCurrentPeriod()})
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <p>Cargando sorteos...</p>
          ) : isError ? (
            <p className="text-red-500">Error: {error?.message || 'An unknown error occurred.'}</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>ID</TableHead>
                  <TableHead>Período</TableHead>
                  <TableHead>Estado</TableHead>
                  <TableHead>Merkle Root</TableHead>
                  <TableHead>Ganador</TableHead>
                  <TableHead>Acciones</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {raffles.length > 0 ? raffles.map((raffle) => (
                  <TableRow key={raffle.id}>
                    <TableCell>{raffle.id}</TableCell>
                    <TableCell>{raffle.period}</TableCell>
                    <TableCell>{raffle.status}</TableCell>
                    <TableCell className="font-mono text-xs">{raffle.merkleRoot.substring(0, 10)}...</TableCell>
                    <TableCell>{raffle.winnerEntryId || 'N/A'}</TableCell>
                    <TableCell>
                      {raffle.status === 'CLOSED' && (
                        <Button size="sm" onClick={() => handleExecuteDraw(raffle.id!)} disabled={closePeriodMutation.isPending || executeDrawMutation.isPending}>
                          Ejecutar Sorteo
                        </Button>
                      )}
                      {raffle.status === 'DRAWN' && (
                        <span className="text-green-600">Sorteo Realizado</span>
                      )}
                    </TableCell>
                  </TableRow>
                )) : (
                  <TableRow>
                    <TableCell colSpan={6} className="text-center">No hay sorteos creados.</TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
      
    </div>
  );
}
