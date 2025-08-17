"use client";

import { useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Campaign } from "@/lib/apiClient";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod"; // Assuming this resolver is available

// Define Zod schema for validation
const CampaignSchema = z.object({
  name: z.string().min(1, "Name cannot be empty"),
  startDate: z.string().refine((date) => !isNaN(new Date(date).getTime()), "Invalid start date"),
  endDate: z.string().refine((date) => !isNaN(new Date(date).getTime()), "Invalid end date"),
  budget: z.number().min(0, "Budget must be positive"),
  adUrl: z.string().url("Invalid URL format").min(1, "Ad URL cannot be empty"),
}).refine((data) => new Date(data.endDate) >= new Date(data.startDate), {
  message: "End date cannot be before start date",
  path: ["endDate"], 
});

type CampaignFormValues = z.infer<typeof CampaignSchema>;

interface CampaignFormProps {
  campaign: Campaign | null;
  isOpen: boolean;
  onClose: () => void;
  onSave: (campaign: Campaign) => void;
}

export function CampaignForm({ campaign, isOpen, onClose, onSave }: CampaignFormProps) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<CampaignFormValues>({
    resolver: zodResolver(CampaignSchema),
    defaultValues: {
      name: "",
      startDate: "",
      endDate: "",
      budget: 0,
      adUrl: "",
    },
  });

  useEffect(() => {
    if (campaign) {
      reset({
        name: campaign.name,
        startDate: campaign.startDate,
        endDate: campaign.endDate,
        budget: campaign.budget,
        adUrl: campaign.adUrl,
      });
    } else {
      reset();
    }
  }, [campaign, reset]);

  const onSubmit = (data: CampaignFormValues) => {
    const savedCampaign: Campaign = {
      id: campaign?.id || Date.now(), // Mock ID for now
      name: data.name,
      startDate: data.startDate,
      endDate: data.endDate,
      budget: data.budget,
      adUrl: data.adUrl,
    };
    onSave(savedCampaign);
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>{campaign ? "Editar Campaña" : "Crear Nueva Campaña"}</DialogTitle>
          <DialogDescription>
            {campaign ? "Realiza cambios en tu campaña aquí." : "Define los detalles de tu nueva campaña publicitaria."}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="name">Nombre</Label>
            <Input id="name" {...register("name")} />
            {errors.name && <p className="text-red-500 text-sm">{errors.name.message}</p>}
          </div>
          <div className="grid gap-2">
            <Label htmlFor="startDate">Fecha Inicio</Label>
            <Input id="startDate" type="date" {...register("startDate")} />
            {errors.startDate && <p className="text-red-500 text-sm">{errors.startDate.message}</p>}
          </div>
          <div className="grid gap-2">
            <Label htmlFor="endDate">Fecha Fin</Label>
            <Input id="endDate" type="date" {...register("endDate")} />
            {errors.endDate && <p className="text-red-500 text-sm">{errors.endDate.message}</p>}
          </div>
          <div className="grid gap-2">
            <Label htmlFor="budget">Presupuesto</Label>
            <Input id="budget" type="number" step="0.01" {...register("budget", { valueAsNumber: true })} />
            {errors.budget && <p className="text-red-500 text-sm">{errors.budget.message}</p>}
          </div>
          <div className="grid gap-2">
            <Label htmlFor="adUrl">URL Anuncio</Label>
            <Input id="adUrl" {...register("adUrl")} />
            {errors.adUrl && <p className="text-red-500 text-sm">{errors.adUrl.message}</p>}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={onClose} type="button">Cancelar</Button>
            <Button type="submit">Guardar Campaña</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}