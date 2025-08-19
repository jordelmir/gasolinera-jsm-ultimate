"use client";

import { useEffect, useState } from "react";
import { DollarSign, Megaphone, TrendingUp } from "lucide-react";
import { DollarSign, Megaphone, TrendingUp } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Spinner } from "@/components/ui/spinner";
import { toast } from 'react-toastify';


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
        const response = await fetch("http://localhost:3001/campaignPerformanceSummary");
        const data = await response.json();
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
    return (
      <div className="flex items-center justify-center h-64">
        <Spinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center h-64 bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative" role="alert">
        <strong className="font-bold">Error!</strong>
        <span className="block sm:inline">{error}</span>
      </div>
    );
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
          <div className="text-2xl font-bold">N/A</div>
          <p className="text-xs text-muted-foreground">
            Datos no disponibles
          </p>
        </CardContent>
      </Card>
      </div>
  );
}