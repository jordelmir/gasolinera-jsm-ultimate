'use client';

import React, { useState } from 'react';
import {
  TrophyIcon,
  PlusIcon,
  CalendarIcon,
  GiftIcon,
  UsersIcon,
  CheckCircleIcon,
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

export default function RafflesPage() {
  const [raffles] = useState<Raffle[]>([
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
      createdAt: '2024-12-08',
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
      createdAt: '2024-01-01',
    },
  ]);

  const activeRaffles = raffles.filter((r) => r.status === 'ACTIVE');
  const completedRaffles = raffles.filter((r) => r.status === 'COMPLETED');
  const totalPrizeValue = activeRaffles.reduce(
    (sum, r) => sum + r.prizeValue,
    0
  );
  const totalParticipants = activeRaffles.reduce(
    (sum, r) => sum + r.totalParticipants,
    0
  );

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
            <button className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center">
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
                <p className="text-sm font-medium text-gray-600">
                  Sorteos Activos
                </p>
                <p className="text-2xl font-bold text-gray-900">
                  {activeRaffles.length}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
            <div className="flex items-center">
              <GiftIcon className="w-8 h-8 text-green-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">
                  Valor Total Premios
                </p>
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
                <p className="text-sm font-medium text-gray-600">
                  Participantes
                </p>
                <p className="text-2xl font-bold text-gray-900">
                  {totalParticipants}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
            <div className="flex items-center">
              <CheckCircleIcon className="w-8 h-8 text-purple-600" />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Completados</p>
                <p className="text-2xl font-bold text-gray-900">
                  {completedRaffles.length}
                </p>
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
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {activeRaffles.map((raffle) => (
                  <div
                    key={raffle.id}
                    className="border border-gray-200 rounded-lg p-6"
                  >
                    <div className="flex items-start justify-between mb-4">
                      <div>
                        <h4 className="text-lg font-semibold text-gray-900">
                          {raffle.name}
                        </h4>
                        <p className="text-sm text-gray-500">
                          {raffle.description}
                        </p>
                      </div>
                      <span
                        className={`px-2 py-1 text-xs font-semibold rounded-full ${
                          raffle.type === 'WEEKLY'
                            ? 'bg-blue-100 text-blue-800'
                            : 'bg-purple-100 text-purple-800'
                        }`}
                      >
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
                          {new Date(raffle.drawDate).toLocaleDateString(
                            'es-CR'
                          )}{' '}
                          a las {raffle.drawTime}
                        </span>
                      </div>

                      <div className="flex items-center text-sm text-gray-600">
                        <UsersIcon className="w-4 h-4 mr-2" />
                        <span>
                          {raffle.totalParticipants} participantes •{' '}
                          {raffle.totalTickets} tickets
                        </span>
                      </div>
                    </div>

                    <div className="flex space-x-2 mt-4">
                      <button className="flex-1 bg-green-600 text-white px-3 py-2 rounded-lg hover:bg-green-700 text-sm">
                        Realizar Sorteo
                      </button>
                      <button className="flex-1 bg-blue-600 text-white px-3 py-2 rounded-lg hover:bg-blue-700 text-sm">
                        Ver Detalles
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Completed Raffles Table */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200">
          <div className="p-6 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">
              Historial de Sorteos
            </h3>
          </div>

          <div className="p-6">
            {completedRaffles.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-gray-500">No hay sorteos completados</p>
              </div>
            ) : (
              <div className="space-y-4">
                {completedRaffles.map((raffle) => (
                  <div
                    key={raffle.id}
                    className="border border-gray-200 rounded-lg p-4"
                  >
                    <div className="flex justify-between items-center">
                      <div>
                        <h4 className="font-medium text-gray-900">
                          {raffle.name}
                        </h4>
                        <p className="text-sm text-gray-500">
                          {raffle.prizeDescription}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="font-semibold text-green-600">
                          ₡{raffle.prizeValue.toLocaleString()}
                        </p>
                        <p className="text-sm text-gray-500">
                          Ganador: {raffle.winnerName || 'N/A'}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
