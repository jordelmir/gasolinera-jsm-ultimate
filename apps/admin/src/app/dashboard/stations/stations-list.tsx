
"use client";

import { useState, useEffect } from "react";
import { MoreHorizontal, PlusCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { StationForm } from "./station-form";
import { getStations, createStation, updateStation, deleteStation, Station } from "@/lib/apiClient";
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

export function StationsList() {
  const [stations, setStations] = useState<Station[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingStation, setEditingStation] = useState<Station | null>(null);

  const fetchStations = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await getStations();
      setStations(data);
    } catch (err: any) {
      setError(err.message);
      toast.error(`Error loading stations: ${err.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchStations();
  }, []);

  const handleOpenForm = (station: Station | null) => {
    setEditingStation(station);
    setIsFormOpen(true);
  };

  const handleCloseForm = () => {
    setIsFormOpen(false);
    setEditingStation(null);
  };

  const handleSaveStation = async (stationData: Station) => {
    try {
      if (editingStation) {
        await updateStation(editingStation.id, stationData);
        toast.success("Station updated successfully!");
      } else {
        await createStation(stationData);
        toast.success("Station created successfully!");
      }
      fetchStations(); // Re-fetch data after save
    } catch (err: any) {
      setError(err.message); // Show error to user
      toast.error(`Error saving station: ${err.message}`);
    }
  };

  const handleDelete = async (stationId: string) => {
    if (confirm("¿Estás seguro de que quieres eliminar esta estación?")) {
        try {
            await deleteStation(stationId);
            toast.success("Station deleted successfully!");
            fetchStations(); // Re-fetch data after delete
        } catch (err: any) {
            setError(err.message);
            toast.error(`Error deleting station: ${err.message}`);
        }
    }
  };

  return (
    <>
      <StationForm 
        station={editingStation}
        isOpen={isFormOpen}
        onClose={handleCloseForm}
        onSave={handleSaveStation}
      />
      <Card>
        <CardHeader>
          <div className="flex justify-between items-center">
            <div>
                <CardTitle>Estaciones de Servicio</CardTitle>
                <CardDescription>
                Gestiona las estaciones de servicio de la red Punto G.
                </CardDescription>
            </div>
            <Button onClick={() => handleOpenForm(null)}>
                <PlusCircle className="mr-2 h-4 w-4" /> Crear Nueva
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <p>Cargando estaciones...</p>
          ) : error ? (
            <p className="text-red-500">Error: {error}</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Nombre</TableHead>
                  <TableHead>Estado</TableHead>
                  <TableHead>Latitud</TableHead>
                  <TableHead>Longitud</TableHead>
                  <TableHead><span className="sr-only">Acciones</span></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {stations.length > 0 ? stations.map((station) => (
                  <TableRow key={station.id}>
                    <TableCell className="font-medium">{station.name}</TableCell>
                    <TableCell>{station.status}</TableCell>
                    <TableCell>{station.latitude}</TableCell>
                    <TableCell>{station.longitude}</TableCell>
                    <TableCell>
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button aria-haspopup="true" size="icon" variant="ghost">
                            <MoreHorizontal className="h-4 w-4" />
                            <span className="sr-only">Toggle menu</span>
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuLabel>Acciones</DropdownMenuLabel>
                          <DropdownMenuItem onSelect={() => handleOpenForm(station)}>Editar</DropdownMenuItem>
                          <DropdownMenuItem onSelect={() => handleDelete(station.id)} className="text-red-600">Eliminar</DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                )) : (
                    <TableRow>
                        <TableCell colSpan={5} className="text-center">No hay estaciones creadas.</TableCell>
                    </TableRow>
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
      <ToastContainer position="bottom-right" />
    </>
  );
}
