
"use client";

import { useState, useEffect } from "react";
import { PlusCircle, MoreHorizontal } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { getMyCampaigns, createCampaign, updateCampaign, deleteCampaign, Campaign } from "@/lib/apiClient";
import { CampaignForm } from "./campaign-form";
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

export default function CampaignsPage() {
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingCampaign, setEditingCampaign] = useState<Campaign | null>(null);

  const fetchCampaigns = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await getMyCampaigns();
      setCampaigns(data);
    } catch (err: any) {
      setError(err.message);
      toast.error(`Error loading campaigns: ${err.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCampaigns();
  }, []);

  const handleOpenForm = (campaign: Campaign | null) => {
    setEditingCampaign(campaign);
    setIsFormOpen(true);
  };

  const handleCloseForm = () => {
    setIsFormOpen(false);
    setEditingCampaign(null);
  };

  const handleSaveCampaign = async (campaignData: Campaign) => {
    try {
      if (editingCampaign) {
        await updateCampaign(editingCampaign.id, campaignData);
        toast.success("Campaign updated successfully!");
      } else {
        await createCampaign(campaignData);
        toast.success("Campaign created successfully!");
      }
      fetchCampaigns(); // Re-fetch data after save
    } catch (err: any) {
      setError(err.message); // Show error to user
      toast.error(`Error saving campaign: ${err.message}`);
    }
  };

  const handleDelete = async (campaignId: number) => {
    if (confirm("¿Estás seguro de que quieres eliminar esta campaña?")) {
        try {
            await deleteCampaign(campaignId);
            toast.success("Campaign deleted successfully!");
            fetchCampaigns(); // Re-fetch data after delete
        } catch (err: any) {
            setError(err.message);
            toast.error(`Error deleting campaign: ${err.message}`);
        }
    }
  };

  return (
    <>
      <CampaignForm 
        campaign={editingCampaign}
        isOpen={isFormOpen}
        onClose={handleCloseForm}
        onSave={handleSaveCampaign}
      />
      <Card>
        <CardHeader>
          <div className="flex justify-between items-center">
            <div>
                <CardTitle>Mis Campañas Publicitarias</CardTitle>
                <CardDescription>
                Gestiona tus campañas activas y pasadas.
                </CardDescription>
            </div>
            <Button onClick={() => handleOpenForm(null)}>
                <PlusCircle className="mr-2 h-4 w-4" /> Crear Nueva Campaña
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <p>Cargando campañas...</p>
          ) : error ? (
            <p className="text-red-500">Error: {error}</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Nombre</TableHead>
                  <TableHead>Inicio</TableHead>
                  <TableHead>Fin</TableHead>
                  <TableHead>Presupuesto</TableHead>
                  <TableHead>URL Anuncio</TableHead>
                  <TableHead><span className="sr-only">Acciones</span></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {campaigns.length > 0 ? campaigns.map((campaign) => (
                  <TableRow key={campaign.id}>
                    <TableCell className="font-medium">{campaign.name}</TableCell>
                    <TableCell>{campaign.startDate}</TableCell>
                    <TableCell>{campaign.endDate}</TableCell>
                    <TableCell>${campaign.budget.toFixed(2)}</TableCell>
                    <TableCell><a href={campaign.adUrl} target="_blank" rel="noopener noreferrer">Ver Anuncio</a></TableCell>
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
                          <DropdownMenuItem onSelect={() => handleOpenForm(campaign)}>Editar</DropdownMenuItem>
                          <DropdownMenuItem onSelect={() => handleDelete(campaign.id)} className="text-red-600">Eliminar</DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                )) : (
                    <TableRow>
                        <TableCell colSpan={6} className="text-center">No tienes campañas creadas.</TableCell>
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
