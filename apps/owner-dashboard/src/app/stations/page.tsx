'use client';

import React, { useState } from 'react';
import {
  BuildingStorefrontIcon,
  PlusIcon,
  PencilIcon,
  TrashIcon,
  MapPinIcon,
  ChartBarIcon,
} from '@heroicons/react/24/outline';

interface Station {
  id: string;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  isActive: boolean;
  employeeCount: number;
  todayTickets: number;
  todayRevenue: number;
  conversionRate: number;
}

const mockStations: Station[] = [
  {
    id: '1',
    name: 'Gasolinera JSM Centro',
    address: 'Avenida Central, San José, Costa Rica',
    latitude: 9.9281,
    longitude: -84.0907,
    isActive: true,
    employeeCount: 8,
    todayTickets: 245,
    todayRevenue: 612500,
    conversionRate: 78.5,
  },
  {
    id: '2',
    name: 'Gasolinera JSM Norte',
    address: 'Barrio Escalante, San José, Costa Rica',
    latitude: 9.935,
    longitude: -84.085,
    isActive: true,
    employeeCount: 6,
    todayTickets: 198,
    todayRevenue: 495000,
    conversionRate: 82.3,
  },
  {
    id: '3',
    name: 'Gasolinera JSM Sur',
    address: 'Desamparados, San José, Costa Rica',
    latitude: 9.9,
    longitude: -84.07,
    isActive: true,
    employeeCount: 5,
    todayTickets: 167,
    todayRevenue: 417500,
    conversionRate: 75.2,
  },
];

export default function StationsPage() {
  const [stations, setStations] = useState<Station[]>(mockStations);
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingStation, setEditingStation] = useState<Station | null>(null);
  const [newStation, setNewStation] = useState({
    name: '',
    address: '',
    latitude: 0,
    longitude: 0,
  });

  const handleAddStation = () => {
    const station: Station = {
      id: Date.now().toString(),
      name: newStation.name,
      address: newStation.address,
      latitude: newStation.latitude,
      longitude: newStation.longitude,
      isActive: true,
      employeeCount: 0,
      todayTickets: 0,
      todayRevenue: 0,
      conversionRate: 0,
    };

    setStations([...stations, station]);
    setNewStation({ name: '', address: '', latitude: 0, longitude: 0 });
    setShowAddModal(false);
  };

  const handleEditStation = (station: Station) => {
    setEditingStation(station);
  };

  const handleUpdateStation = () => {
    if (!editingStation) return;

    setStations(
      stations.map((s) => (s.id === editingStation.id ? editingStation : s))
    );
    setEditingStation(null);
  };

  const handleDeleteStation = (id: string) => {
    if (confirm('¿Estás seguro de eliminar esta estación?')) {
      setStations(stations.filter((s) => s.id !== id));
    }
  };

  const toggleStationStatus = (id: string) => {
    setStations(
      stations.map((s) => (s.id === id ? { ...s, isActive: !s.isActive } : s))
    );
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                Gestión de Estaciones
              </h1>
              <p className="text-sm text-gray-500 mt-1">
                Administra tus gasolineras y su rendimiento
              </p>
            </div>
            <button
              onClick={() => setShowAddModal(true)}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center"
            >
              <PlusIcon className="w-5 h-5 mr-2" />
              Nueva Estación
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
            <div className="flex items-center">
              <BuildingStorefrontIcon className="w-8 h-8 text-blue-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">
                  Total Estaciones
                </p>
                <p className="text-2xl font-bold text-gray-900">
                  {stations.length}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
            <div className="flex items-center">
              <ChartBarIcon className="w-8 h-8 text-green-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">
                  Estaciones Activas
                </p>
                <p className="text-2xl font-bold text-gray-900">
                  {stations.filter((s) => s.isActive).length}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
            <div className="flex items-center">
              <div className="w-8 h-8 bg-orange-100 rounded-lg flex items-center justify-center">
                <span className="text-orange-600 font-bold">👥</span>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">
                  Total Empleados
                </p>
                <p className="text-2xl font-bold text-gray-900">
                  {stations.reduce((sum, s) => sum + s.employeeCount, 0)}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
            <div className="flex items-center">
              <div className="w-8 h-8 bg-purple-100 rounded-lg flex items-center justify-center">
                <span className="text-purple-600 font-bold">🎫</span>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Tickets Hoy</p>
                <p className="text-2xl font-bold text-gray-900">
                  {stations.reduce((sum, s) => sum + s.todayTickets, 0)}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Stations Table */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200">
          <div className="p-6 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">
              Lista de Estaciones
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
                    Empleados
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tickets Hoy
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Ingresos Hoy
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Conversión
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Estado
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Acciones
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {stations.map((station) => (
                  <tr key={station.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm font-medium text-gray-900">
                          {station.name}
                        </div>
                        <div className="text-sm text-gray-500 flex items-center">
                          <MapPinIcon className="w-4 h-4 mr-1" />
                          {station.address}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {station.employeeCount}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {station.todayTickets}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      ₡{station.todayRevenue.toLocaleString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {station.conversionRate}%
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <button
                        onClick={() => toggleStationStatus(station.id)}
                        className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          station.isActive
                            ? 'bg-green-100 text-green-800'
                            : 'bg-red-100 text-red-800'
                        }`}
                      >
                        {station.isActive ? 'Activa' : 'Inactiva'}
                      </button>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex space-x-2">
                        <button
                          onClick={() => handleEditStation(station)}
                          className="text-blue-600 hover:text-blue-900"
                        >
                          <PencilIcon className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDeleteStation(station.id)}
                          className="text-red-600 hover:text-red-900"
                        >
                          <TrashIcon className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Add Station Modal */}
      {showAddModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-lg font-semibold mb-4">Nueva Estación</h3>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nombre
                </label>
                <input
                  type="text"
                  value={newStation.name}
                  onChange={(e) =>
                    setNewStation({ ...newStation, name: e.target.value })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  placeholder="Gasolinera JSM..."
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Dirección
                </label>
                <textarea
                  value={newStation.address}
                  onChange={(e) =>
                    setNewStation({ ...newStation, address: e.target.value })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  rows={3}
                  placeholder="Dirección completa..."
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Latitud
                  </label>
                  <input
                    type="number"
                    step="0.000001"
                    value={newStation.latitude}
                    onChange={(e) =>
                      setNewStation({
                        ...newStation,
                        latitude: parseFloat(e.target.value),
                      })
                    }
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Longitud
                  </label>
                  <input
                    type="number"
                    step="0.000001"
                    value={newStation.longitude}
                    onChange={(e) =>
                      setNewStation({
                        ...newStation,
                        longitude: parseFloat(e.target.value),
                      })
                    }
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  />
                </div>
              </div>
            </div>

            <div className="flex justify-end space-x-3 mt-6">
              <button
                onClick={() => setShowAddModal(false)}
                className="px-4 py-2 text-gray-600 hover:text-gray-800"
              >
                Cancelar
              </button>
              <button
                onClick={handleAddStation}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                Crear Estación
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Edit Station Modal */}
      {editingStation && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-lg font-semibold mb-4">Editar Estación</h3>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nombre
                </label>
                <input
                  type="text"
                  value={editingStation.name}
                  onChange={(e) =>
                    setEditingStation({
                      ...editingStation,
                      name: e.target.value,
                    })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Dirección
                </label>
                <textarea
                  value={editingStation.address}
                  onChange={(e) =>
                    setEditingStation({
                      ...editingStation,
                      address: e.target.value,
                    })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  rows={3}
                />
              </div>
            </div>

            <div className="flex justify-end space-x-3 mt-6">
              <button
                onClick={() => setEditingStation(null)}
                className="px-4 py-2 text-gray-600 hover:text-gray-800"
              >
                Cancelar
              </button>
              <button
                onClick={handleUpdateStation}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                Actualizar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
