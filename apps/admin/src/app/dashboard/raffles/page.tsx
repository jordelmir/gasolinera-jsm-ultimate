
"use client";

import { useState, useEffect } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { getRaffles, closeRafflePeriod, executeRaffleDraw, Raffle } from "@/lib/apiClient";
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

export default function RafflesPage() {
  const [raffles, setRaffles] = useState<Raffle[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchRaffles = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await getRaffles();
      setRaffles(data);
    } catch (err: any) {
      setError(err.message);
      toast.error(`Error loading raffles: ${err.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchRaffles();
  }, []);

  const handleClosePeriod = async (period: string) => {
    if (confirm(`¿Estás seguro de que quieres cerrar el período ${period} y construir el Merkle Tree?`)) {
      try {
        setIsLoading(true);
        await closeRafflePeriod(period);
        toast.success(`Period ${period} closed successfully!`);
        fetchRaffles(); // Re-fetch after closing
      } catch (err: any) {
        setError(err.message);
        toast.error(`Error closing period: ${err.message}`);
      } finally {
        setIsLoading(false);
      }
    }
  };

  const handleExecuteDraw = async (raffleId: number) => {
    if (confirm(`¿Estás seguro de que quieres ejecutar el sorteo para el ID ${raffleId}? Esto es irreversible.`)) {
      try {
        setIsLoading(true);
        await executeRaffleDraw(raffleId);
        toast.success(`Raffle ${raffleId} drawn successfully!`);
        fetchRaffles(); // Re-fetch after draw
      } catch (err: any) {
        setError(err.message);
        toast.error(`Error executing draw: ${err.message}`);
      } finally {
        setIsLoading(false);
      }
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
            <Button onClick={() => handleClosePeriod(getCurrentPeriod())} disabled={isLoading}>
              Cerrar Período Actual ({getCurrentPeriod()})
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <p>Cargando sorteos...</p>
          ) : error ? (
            <p className="text-red-500">Error: {error}</p>
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
                        <Button size="sm" onClick={() => handleExecuteDraw(raffle.id!)} disabled={isLoading}>
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
      <ToastContainer position="bottom-right" />
    </div>
  );
}
