"use client";

import { useEffect, useState } => "react";
import { DollarSign, Megaphone, TrendingUp } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getCampaignPerformanceSummary } from "@/lib/apiClient"; // To be implemented
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

interface CampaignSummary {
  totalImpressions: number;
  totalBudgetSpent: number;
  // impressionsByDay: { date: string; impressions: number }[]; // For a simple graph
}

export default function AdvertiserDashboardPage() {
  const [summary, setSummary] = useState<CampaignSummary | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchSummary = async () => {
      try {
        setIsLoading(true);
        // Mock data for now, replace with actual API call
        const data = await getCampaignPerformanceSummary();
        setSummary(data);
      } catch (err: any) {
        setError(err.message);
        toast.error(`Error loading campaign summary: ${err.message}`);
      } finally {
        setIsLoading(false);
      }
    };
    fetchSummary();
  }, []);

  if (isLoading) {
    return <p>Cargando resumen de campañas...</p>;
  }

  if (error) {
    return <p className="text-red-500">Error: {error}</p>;
  }

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">
            Impresiones Totales
          </CardTitle>
          <Megaphone className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{summary?.totalImpressions.toLocaleString()}</div>
          <p className="text-xs text-muted-foreground">
            En todas tus campañas
          </p>
        </CardContent>
      </Card>
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">
            Presupuesto Gastado
          </CardTitle>
          <DollarSign className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">${summary?.totalBudgetSpent.toFixed(2)}</div>
          <p className="text-xs text-muted-foreground">
            En todas tus campañas
          </p>
        </CardContent>
      </Card>
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">Rendimiento General</CardTitle>
          <TrendingUp className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">+18.5%</div>
          <p className="text-xs text-muted-foreground">
            Comparado con el mes anterior
          </p>
        </CardContent>
      </Card>
      <ToastContainer position="bottom-right" />
    </div>
  );
}