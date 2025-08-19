'use client';

import React, { useState } from 'react';
import {
  TrophyIcon,
  PlusIcon,
  CalendarIcon,
  GiftIcon,
  UsersIcon,
  ClockIcon,
  CheckCircleIcon,
  XCircleIcon
} from '@heroicons/react/24/outline';

interface Raffle {
  id: string;
  name: string;
  description: string;
  prizeDescription: string;
  prizeValue: number;
  type: 'WEEKLY' | 'ANNUAL';
  status: 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
  drawDate: string;
  drawTime: string;
  winnerId?: string;
  winnerName?: string;
  totalParticipants: number;
  totalTickets: number;
  createdAt: string;
}

const mockRaffles: Raffle[] = [
  {
    id: '1',
    name: 'Sorteo Semanal #47',
    description: 'Sorteo semanal de efectivo',
    prizeDescription: 'Cuarenta mil colones en efectivo',
    prizeValue: 40000,
    type: 'WEEKLY',
    status: 'ACTIVE',
    drawDate: '2024-12-15',
    drawTime: '15:00',
    totalParticipants: 1247,
    totalTickets: 3891,
    createdAt: '2024-12-08'
  },
  {
    id: '2',
    name: 'Gran Sorteo Anual 2024',
    description: 'Sorteo anual del automóvil',
    prizeDescription: 'Toyota Corolla 2024 0km',
    prizeValue: 15000000,
    type: 'ANNUAL',
    status: 'ACTIVE',
    drawDate: '2024-12-31',
    drawTime: '20:00',
    totalParticipants: 5432,
    totalTickets: 18765,
    createdAt: '2024-01-01'
  },
  {
    id: '3',
    name: 'Sorteo Semanal #46',
    description: 'Sorteo semanal de efectivo',
    prizeDescription: 'Cuarenta mil colones en efectivo',
    prizeValue: 40000,
    type: 'WEEKLY',
    status: 'COMPLETED',
    drawDate: '2024-12-08',
    drawTime: '15:00',
    winnerId: '123',
    winnerName: 'María González',
    totalParticipants: 1156,
    totalTickets: 3245,
    createdAt: '2024-12-01'
  }
];

export default function RafflesPage() {
  const [raffles, setRaffles] = useState<Raffle[]>(mockRaffles);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedRaffle, setSelectedRaffle] = useState<Raffle | null>(null);
  const [newRaffle, setNewRaffle] = useState({
    name: '',
    description: '',
    prizeDescription: '',
    prizeValue: 0,
    type: 'WEEKLY' as 'WEEKLY' | 'ANNUAL',
    drawDate: '',
    drawTime: '15:00'
  });

  const handleCreateRaffle = () => {
    const raffle: Raffle = {
      id: Date.now().toString(),
      name: newRaffle.name,
      description: newRaffle.description,
      prizeDescription: newRaffle.prizeDescription,
      prizeValue: newRaffle.prizeValue,
      type: newRaffle.type,
      status: 'ACTIVE',
      drawDate: newRaffle.drawDate,
      drawTime: newRaffle.drawTime,
      totalParticipants: 0,
      totalTickets: 0,
      createdAt: new Date().toISOString().split('T')[0]
    };

    setRaffles([raffle, ...raffles]);
    setNewRaffle({
      name: '',
      description: '',
      prizeDescription: '',
      prizeValue: 0,
      type: 'WEEKLY',
      drawDate: '',
      drawTime: '15:00'
    });
    setShowCreateModal(false);
  };

  const handleDrawRaffle = (raffleId: string) => {
    // Simular sorteo
    const winners = ['Juan Pérez', 'Ana García', 'Carlos López', 'María Rodríguez'];
    const randomWinner = winners[Math.floor(Math.random() * winners.length)];

    setRaffles(raffles.map(raffle =>
      raffle.id === raffleId
        ? {
            ...raffle,
            status: 'COMPLETED' as const,
            winnerId: 'random-id',
            winnerName: randomWinner
          }
        : raffle
    ));

    alert(`¡Sorteo realizado! Ganador: ${randomWinner}`);
  };

  const handleCancelRaffle = (raffleId: string) => {
    if (confirm('¿Estás seguro de cancelar este sorteo?')) {
      setRaffles(raffles.map(raffle =>
        raffle.id === raffleId
          ? { ...raffle, status: 'CANCELLED' as const }
          : raffle
      ));
    }
  };

  const activeRaffles = raffles.filter(r => r.status === 'ACTIVE');
  const completedRaffles = raffles.filter(r => r.status === 'COMPLETED');
  const totalPrizeValue = activeRaffles.reduce((sum, r) => sum + r.prizeValue, 0);
  const totalParticipants = activeRaffles.reduce((sum, r) => sum + r.totalParticipants, 0);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                Gestión de Sorteos
              </h1>
              <p className="text-sm text-gray-500 mt-1">
                Configura y administra los sorteos semanales y anuales
              </p>
            </div>
            <button
              onClick={() => setShowCreateModal(true)}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center"
            >
              <PlusIcon className="w-5 h-5 mr-2" />
              Nuevo Sorteo
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
            <div className="flex items-center">
              <TrophyIcon className="w-8 h-8 text-yellow-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Sorteos Activos</p>
                <p className="text-2xl font-bold text-gray-900">{activeRaffles.length}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
            <div className="flex items-center">
              <GiftIcon className="w-8 h-8 text-green-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Valor Total Premios</p>
                <p className="text-2xl font-bold text-gray-900">
                  ₡{totalPrizeValue.toLocaleString()}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
            <div className="flex items-center">
              <UsersIcon className="w-8 h-8 text-blue-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Participantes</p>
                <p className="text-2xl font-bold text-gray-900">{totalParticipants}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
            <div className="flex items-center">
              <CheckCircleIcon className="w-8 h-8 text-purple-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Completados</p>
                <p className="text-2xl font-bold text-gray-900">{completedRaffles.length}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Active Raffles */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 mb-8">
          <div className="p-6 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">
              Sorteos Activos
            </h3>
          </div>

          <div className="p-6">
            {activeRaffles.length === 0 ? (
              <div className="text-center py-8">
                <TrophyIcon className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-500">No hay sorteos activos</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6"></div>     {activeRaffles.map((raffle) => (
                  <div key={raffle.id} className="border border-gray-200 rounded-lg p-6">
                    <div className="flex items-start justify-between mb-4">
                      <div>
                        <h4 className="text-lg font-semibold text-gray-900">
                          {raffle.name}
                        </h4>
                        <p className="text-sm text-gray-500">{raffle.description}</p>
                      </div>
                      <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
                        raffle.type === 'WEEKLY'
                          ? 'bg-blue-100 text-blue-800'
                          : 'bg-purple-100 text-purple-800'
                      }`}>
                        {raffle.type === 'WEEKLY' ? 'Semanal' : 'Anual'}
                      </span>
                    </div>

                    <div className="space-y-3">
                      <div className="flex items-center text-sm text-gray-600">
                        <GiftIcon className="w-4 h-4 mr-2" />
                        <span>{raffle.prizeDescription}</span>
                      </div>

                      <div className="flex items-center text-sm text-gray-600">
                        <span className="font-semibold text-green-600">
                          ₡{raffle.prizeValue.toLocaleString()}
                        </span>
                      </div>

                      <div className="flex items-center text-sm text-gray-600">
                        <CalendarIcon className="w-4 h-4 mr-2" />
                        <span>
                          {new Date(raffle.drawDate).toLocaleDateString('es-CR')} a las {raffle.drawTime}
                        </span>
                      </div>

                      <div className="flex items-center text-sm text-gray-600">
                        <UsersIcon className="w-4 h-4 mr-2" />
                        <span>
                          {raffle.totalParticipants} participantes • {raffle.totalTickets} tickets
                        </span>
                      </div>
                    </div>

                    <div className="flex space-x-2 mt-4">
                      <button
                        onClick={() => handleDrawRaffle(raffle.id)}
                        className="flex-1 bg-green-600 text-white px-3 py-2 rounded-lg hover:bg-green-700 text-sm"
                      >
                        Realizar Sorteo
                      </button>
                      <button
                        onClick={() => setSelectedRaffle(raffle)}
                        className="flex-1 bg-blue-600 text-white px-3 py-2 rounded-lg hover:bg-blue-700 text-sm"
                      >
                        Ver Detalles
                      </button>
                      <button
                        onClick={() => handleCancelRaffle(raffle.id)}
                        className="px-3 py-2 border border-red-300 text-red-600 rounded-lg hover:bg-red-50 text-sm"
                      >
                        Cancelar
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Completed Raffles */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200">
          <div className="p-6 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">
              Historial de Sorteos
            </h3>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Sorteo
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Premio
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Fecha
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Ganador
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Participantes
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Estado
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {raffles.filter(r => r.status !== 'ACTIVE').map((raffle) => (
                  <tr key={raffle.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm font-medium text-gray-900">
                          {raffle.name}
                        </div>
                        <div className="text-sm text-gray-500">
                          {raffle.type === 'WEEKLY' ? 'Semanal' : 'Anual'}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {raffle.prizeDescription}
                      </div>
                      <div className="text-sm font-semibold text-green-600">
                        ₡{raffle.prizeValue.toLocaleString()}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {new Date(raffle.drawDate).toLocaleDateString('es-CR')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {raffle.winnerName || 'N/A'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {raffle.totalParticipants}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                        raffle.status === 'COMPLETED'
                          ? 'bg-green-100 text-green-800'
                          : 'bg-red-100 text-red-800'
                      }`}>
                        {raffle.status === 'COMPLETED' ? 'Completado' : 'Cancelado'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Create Raffle Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md max-h-[90vh] overflow-y-auto">
            <h3 className="text-lg font-semibold mb-4">Nuevo Sorteo</h3>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nombre del Sorteo
                </label>
                <input
                  type="text"
                  value={newRaffle.name}
                  onChange={(e) => setNewRaffle({...newRaffle, name: e.target.value})}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  placeholder="Sorteo Semanal #48"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Descripción
                </label>
                <textarea
                  value={newRaffle.description}
                  onChange={(e) => setNewRaffle({...newRaffle, description: e.target.value})}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  rows={2}
                  placeholder="Descripción del sorteo..."
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Tipo de Sorteo
                </label>
                <select
                  value={newRaffle.type}
                  onChange={(e) => setNewRaffle({...newRaffle, type: e.target.value as 'WEEKLY' | 'ANNUAL'})}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                >
                  <option value="WEEKLY">Semanal</option>
                  <option value="ANNUAL">Anual</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Descripción del Premio
                </label>
                <input
                  type="text"
                  value={newRaffle.prizeDescription}
                  onChange={(e) => setNewRaffle({...newRaffle, prizeDescription: e.target.value})}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  placeholder="Cuarenta mil colones en efectivo"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Valor del Premio (₡)
                </label>
                <input
                  type="number"
                  value={newRaffle.prizeValue}
                  onChange={(e) => setNewRaffle({...newRaffle, prizeValue: parseInt(e.target.value) || 0})}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  placeholder="40000"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Fecha del Sorteo
                  </label>
                  <input
                    type="date"
                    value={newRaffle.drawDate}
                    onChange={(e) => setNewRaffle({...newRaffle, drawDate: e.target.value})}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Hora del Sorteo
                  </label>
                  <input
                    type="time"
                    value={newRaffle.drawTime}
                    onChange={(e) => setNewRaffle({...newRaffle, drawTime: e.target.value})}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  />
                </div>
              </div>
            </div>

            <div className="flex justify-end space-x-3 mt-6">
              <button
                onClick={() => setShowCreateModal(false)}
                className="px-4 py-2 text-gray-600 hover:text-gray-800"
              >
                Cancelar
              </button>
              <button
                onClick={handleCreateRaffle}
                disabled={!newRaffle.name || !newRaffle.prizeDescription || !newRaffle.drawDate}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
              >
                Crear Sorteo
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Raffle Details Modal */}
      {selectedRaffle && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-lg">
            <div className="flex justify-between items-start mb-4">
              <h3 className="text-lg font-semibold">{selectedRaffle.name}</h3>
              <button
                onClick={() => setSelectedRaffle(null)}
                className="text-gray-400 hover:text-gray-600"
              >
                <XCircleIcon className="w-6 h-6" />
              </button>
            </div>

            <div className="space-y-4">
              <div>
                <h4 className="font-medium text-gray-900">Premio</h4>
                <p className="text-gray-600">{selectedRaffle.prizeDescription}</p>
                <p className="text-lg font-semibold text-green-600">
                  ₡{selectedRaffle.prizeValue.toLocaleString()}
                </p>
              </div>

              <div>
                <h4 className="font-medium text-gray-900">Fecha y Hora</h4>
                <p className="text-gray-600">
                  {new Date(selectedRaffle.drawDate).toLocaleDateString('es-CR')} a las {selectedRaffle.drawTime}
                </p>
              </div>

              <div>
                <h4 className="font-medium text-gray-900">Participación</h4>
                <p className="text-gray-600">
                  {selectedRaffle.totalParticipants} participantes con {selectedRaffle.totalTickets} tickets
                </p>
              </div>

              {selectedRaffle.winnerName && (
                <div>
                  <h4 className="font-medium text-gray-900">Ganador</h4>
                  <p className="text-gray-600">{selectedRaffle.winnerName}</p>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}