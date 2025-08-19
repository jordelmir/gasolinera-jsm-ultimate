'use client';

import React, { useState } from 'react';
import {
  ChartBarIcon,
  CalendarIcon,
  TrendingUpIcon,
  TrendingDownIcon,
  UsersIcon,
  TicketIcon,
  CurrencyDollarIcon,
  ClockIcon,
} from '@heroicons/react/24/outline';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  AreaChart,
  Area,
} from 'recharts';

const COLORS = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6'];

const mockData = {
  dailyStats: [
    {
      date: '2024-12-01',
      tickets: 180,
      revenue: 450000,
      users: 145,
      conversion: 78.5,
    },
    {
      date: '2024-12-02',
      tickets: 220,
      revenue: 550000,
      users: 178,
      conversion: 82.1,
    },
    {
      date: '2024-12-03',
      tickets: 190,
      revenue: 475000,
      users: 156,
      conversion: 75.3,
    },
    {
      date: '2024-12-04',
      tickets: 280,
      revenue: 700000,
      users: 234,
      conversion: 85.2,
    },
    {
      date: '2024-12-05',
      tickets: 320,
      revenue: 800000,
      users: 267,
      conversion: 88.7,
    },
    {
      date: '2024-12-06',
      tickets: 380,
      revenue: 950000,
      users: 312,
      conversion: 91.2,
    },
    {
      date: '2024-12-07',
      tickets: 290,
      revenue: 725000,
      users: 245,
      conversion: 86.4,
    },
  ],
  hourlyDistribution: [
    { hour: '06:00', tickets: 45 },
    { hour: '08:00', tickets: 78 },
    { hour: '10:00', tickets: 92 },
    { hour: '12:00', tickets: 156 },
    { hour: '14:00', tickets: 134 },
    { hour: '16:00', tickets: 189 },
    { hour: '18:00', tickets: 167 },
    { hour: '20:00', tickets: 98 },
    { hour: '22:00', tickets: 56 },
  ],
  stationPerformance: [
    { name: 'Centro', tickets: 245, revenue: 612500, share: 35 },
    { name: 'Norte', tickets: 198, revenue: 495000, share: 28 },
    { name: 'Sur', tickets: 167, revenue: 417500, share: 24 },
    { name: 'Este', tickets: 134, revenue: 335000, share: 13 },
  ],
  adPerformance: [
    { step: 1, duration: 10, completion: 95, tickets: 1250 },
    { step: 2, duration: 15, completion: 78, tickets: 975 },
    { step: 3, duration: 30, completion: 62, tickets: 775 },
    { step: 4, duration: 60, completion: 45, tickets: 562 },
    { step: 5, duration: 120, completion: 28, tickets: 350 },
  ],
};

export default function AnalyticsPage() {
  const [timeRange, setTimeRange] = useState('7d');
  const [selectedMetric, setSelectedMetric] = useState('tickets');

  const totalTickets = mockData.dailyStats.reduce(
    (sum, day) => sum + day.tickets,
    0
  );
  const totalRevenue = mockData.dailyStats.reduce(
    (sum, day) => sum + day.revenue,
    0
  );
  const avgConversion =
    mockData.dailyStats.reduce((sum, day) => sum + day.conversion, 0) /
    mockData.dailyStats.length;
  const totalUsers = mockData.dailyStats.reduce(
    (sum, day) => sum + day.users,
    0
  );

  const getMetricData = () => {
    switch (selectedMetric) {
      case 'revenue':
        return mockData.dailyStats.map((d) => ({ ...d, value: d.revenue }));
      case 'users':
        return mockData.dailyStats.map((d) => ({ ...d, value: d.users }));
      case 'conversion':
        return mockData.dailyStats.map((d) => ({ ...d, value: d.conversion }));
      default:
        return mockData.dailyStats.map((d) => ({ ...d, value: d.tickets }));
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                Analytics Avanzados
              </h1>
              <p className="text-sm text-gray-500 mt-1">
                Análisis detallado del rendimiento de tu negocio
              </p>
            </div>

            <div className="flex items-center space-x-4">
              <select
                value={timeRange}
                onChange={(e) => setTimeRange(e.target.value)}
                className="border border-gray-300 rounded-lg px-3 py-2 text-sm"
              >
                <option value="7d">Últimos 7 días</option>
                <option value="30d">Últimos 30 días</option>
                <option value="90d">Últimos 90 días</option>
                <option value="1y">Último año</option>
              </select>

              <button className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center">
                <CalendarIcon className="w-4 h-4 mr-2" />
                Exportar Reporte
              </button>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* KPI Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">
                  Total Tickets
                </p>
                <p className="text-2xl font-bold text-gray-900">
                  {totalTickets.toLocaleString()}
                </p>
                <div className="flex items-center mt-1">
                  <TrendingUpIcon className="w-4 h-4 text-green-500" />
                  <span className="text-sm text-green-600 ml-1">+12.5%</span>
                </div>
              </div>
              <TicketIcon className="w-8 h-8 text-blue-600" />
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Ingresos</p>
                <p className="text-2xl font-bold text-gray-900">
                  ₡{totalRevenue.toLocaleString()}
                </p>
                <div className="flex items-center mt-1">
                  <TrendingUpIcon className="w-4 h-4 text-green-500" />
                  <span className="text-sm text-green-600 ml-1">+8.3%</span>
                </div>
              </div>
              <CurrencyDollarIcon className="w-8 h-8 text-green-600" />
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">
                  Usuarios Únicos
                </p>
                <p className="text-2xl font-bold text-gray-900">
                  {totalUsers.toLocaleString()}
                </p>
                <div className="flex items-center mt-1">
                  <TrendingDownIcon className="w-4 h-4 text-red-500" />
                  <span className="text-sm text-red-600 ml-1">-2.1%</span>
                </div>
              </div>
              <UsersIcon className="w-8 h-8 text-purple-600" />
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">
                  Conversión Promedio
                </p>
                <p className="text-2xl font-bold text-gray-900">
                  {avgConversion.toFixed(1)}%
                </p>
                <div className="flex items-center mt-1">
                  <TrendingUpIcon className="w-4 h-4 text-green-500" />
                  <span className="text-sm text-green-600 ml-1">+5.7%</span>
                </div>
              </div>
              <ChartBarIcon className="w-8 h-8 text-orange-600" />
            </div>
          </div>
        </div>

        {/* Main Chart */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 mb-8">
          <div className="p-6 border-b border-gray-200">
            <div className="flex justify-between items-center">
              <h3 className="text-lg font-semibold text-gray-900">
                Tendencias Temporales
              </h3>
              <div className="flex space-x-2">
                {[
                  { key: 'tickets', label: 'Tickets' },
                  { key: 'revenue', label: 'Ingresos' },
                  { key: 'users', label: 'Usuarios' },
                  { key: 'conversion', label: 'Conversión' },
                ].map((metric) => (
                  <button
                    key={metric.key}
                    onClick={() => setSelectedMetric(metric.key)}
                    className={`px-3 py-1 text-sm rounded-lg ${
                      selectedMetric === metric.key
                        ? 'bg-blue-100 text-blue-700'
                        : 'text-gray-600 hover:bg-gray-100'
                    }`}
                  >
                    {metric.label}
                  </button>
                ))}
              </div>
            </div>
          </div>

          <div className="p-6">
            <ResponsiveContainer width="100%" height={400}>
              <AreaChart data={getMetricData()}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis
                  dataKey="date"
                  tickFormatter={(value) =>
                    new Date(value).toLocaleDateString('es-CR', {
                      month: 'short',
                      day: 'numeric',
                    })
                  }
                />
                <YAxis />
                <Tooltip
                  labelFormatter={(value) =>
                    new Date(value).toLocaleDateString('es-CR')
                  }
                  formatter={(value, name) => {
                    if (selectedMetric === 'revenue')
                      return [`₡${value.toLocaleString()}`, 'Ingresos'];
                    if (selectedMetric === 'conversion')
                      return [`${value}%`, 'Conversión'];
                    return [value.toLocaleString(), name];
                  }}
                />
                <Area
                  type="monotone"
                  dataKey="value"
                  stroke="#3B82F6"
                  fill="#3B82F6"
                  fillOpacity={0.1}
                  strokeWidth={2}
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Secondary Charts */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Hourly Distribution */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200">
            <div className="p-6 border-b border-gray-200">
              <h3 className="text-lg font-semibold text-gray-900">
                Distribución por Horas
              </h3>
            </div>
            <div className="p-6">
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={mockData.hourlyDistribution}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="hour" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="tickets" fill="#10B981" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Station Performance */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200">
            <div className="p-6 border-b border-gray-200">
              <h3 className="text-lg font-semibold text-gray-900">
                Rendimiento por Estación
              </h3>
            </div>
            <div className="p-6">
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={mockData.stationPerformance}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, share }) => `${name} ${share}%`}
                    outerRadius={80}
                    fill="#8884d8"
                    dataKey="share"
                  >
                    {mockData.stationPerformance.map((entry, index) => (
                      <Cell
                        key={`cell-${index}`}
                        fill={COLORS[index % COLORS.length]}
                      />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        {/* Ad Performance Analysis */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 mb-8">
          <div className="p-6 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">
              Análisis de Rendimiento de Anuncios
            </h3>
          </div>

          <div className="p-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* Completion Rate Chart */}
              <div>
                <h4 className="text-md font-medium text-gray-900 mb-4">
                  Tasa de Finalización por Paso
                </h4>
                <ResponsiveContainer width="100%" height={250}>
                  <BarChart data={mockData.adPerformance}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="step" />
                    <YAxis />
                    <Tooltip
                      formatter={(value) => [`${value}%`, 'Finalización']}
                    />
                    <Bar
                      dataKey="completion"
                      fill="#F59E0B"
                      radius={[4, 4, 0, 0]}
                    />
                  </BarChart>
                </ResponsiveContainer>
              </div>

              {/* Tickets Generated Chart */}
              <div>
                <h4 className="text-md font-medium text-gray-900 mb-4">
                  Tickets Generados por Paso
                </h4>
                <ResponsiveContainer width="100%" height={250}>
                  <LineChart data={mockData.adPerformance}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="step" />
                    <YAxis />
                    <Tooltip />
                    <Line
                      type="monotone"
                      dataKey="tickets"
                      stroke="#8B5CF6"
                      strokeWidth={3}
                      dot={{ fill: '#8B5CF6', strokeWidth: 2, r: 4 }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>
        </div>

        {/* Detailed Stats Table */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200">
          <div className="p-6 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">
              Estadísticas Detalladas por Estación
            </h3>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Estación
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tickets
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Ingresos
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Participación
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Ticket Promedio
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tendencia
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {mockData.stationPerformance.map((station, index) => (
                  <tr key={station.name} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div
                          className={`w-3 h-3 rounded-full mr-3`}
                          style={{ backgroundColor: COLORS[index] }}
                        ></div>
                        <div className="text-sm font-medium text-gray-900">
                          Gasolinera JSM {station.name}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {station.tickets.toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      ₡{station.revenue.toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {station.share}%
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      ₡
                      {Math.round(
                        station.revenue / station.tickets
                      ).toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      <div className="flex items-center">
                        <TrendingUpIcon className="w-4 h-4 text-green-500 mr-1" />
                        <span className="text-green-600">
                          +{(Math.random() * 20).toFixed(1)}%
                        </span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
