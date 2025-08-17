"use client";

import { useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Station } from "@/lib/apiClient";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod"; // Assuming this resolver is available

// Define Zod schema for validation
const StationSchema = z.object({
  name: z.string().min(1, "Name cannot be empty"),
  latitude: z.number().min(-90, "Latitude must be between -90 and 90").max(90, "Latitude must be between -90 and 90"),
  longitude: z.number().min(-180, "Longitude must be between -180 and 180").max(180, "Longitude must be between -180 and 180"),
});

type StationFormValues = z.infer<typeof StationSchema>;

interface StationFormProps {
  station: Station | null;
  isOpen: boolean;
  onClose: () => void;
  onSave: (station: Station) => void;
}

export function StationForm({ station, isOpen, onClose, onSave }: StationFormProps) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<StationFormValues>({
    resolver: zodResolver(StationSchema),
    defaultValues: {
      name: "",
      latitude: 0,
      longitude: 0,
    },
  });

  useEffect(() => {
    if (station) {
      reset({
        name: station.name,
        latitude: station.latitude,
        longitude: station.longitude,
      });
    } else {
      reset();
    }
  }, [station, reset]);

  const onSubmit = (data: StationFormValues) => {
    const savedStation: Station = {
      id: station?.id || `stn_${Date.now()}`,
      name: data.name,
      latitude: data.latitude,
      longitude: data.longitude,
      status: station?.status || "Activa",
    };
    onSave(savedStation);
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>{station ? "Editar Estación" : "Crear Nueva Estación"}</DialogTitle>
          <DialogDescription>
            {station ? "Realiza cambios en la estación aquí." : "Añade una nueva estación a la red."}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="grid gap-4 py-4">
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="name" className="text-right">
              Nombre
            </Label>
            <Input id="name" {...register("name")} className="col-span-3" />
            {errors.name && <p className="col-span-4 text-red-500 text-sm text-right">{errors.name.message}</p>}
          </div>
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="latitude" className="text-right">
              Latitud
            </Label>
            <Input id="latitude" type="number" step="any" {...register("latitude", { valueAsNumber: true })} className="col-span-3" />
            {errors.latitude && <p className="col-span-4 text-red-500 text-sm text-right">{errors.latitude.message}</p>}
          </div>
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="longitude" className="text-right">
              Longitud
            </Label>
            <Input id="longitude" type="number" step="any" {...register("longitude", { valueAsNumber: true })} className="col-span-3" />
            {errors.longitude && <p className="col-span-4 text-red-500 text-sm text-right">{errors.longitude.message}</p>}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={onClose} type="button">Cancelar</Button>
            <Button type="submit">Guardar Cambios</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}