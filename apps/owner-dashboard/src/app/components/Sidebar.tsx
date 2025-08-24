'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  HomeIcon,
  BuildingStorefrontIcon,
  UsersIcon,
  TrophyIcon,
  ChartBarIcon,
  CogIcon,
  ArrowRightOnRectangleIcon,
} from '@heroicons/react/24/outline';

const navigation = [
  { name: 'Dashboard', href: '/', icon: HomeIcon },
  { name: 'Estaciones', href: '/stations', icon: BuildingStorefrontIcon },
  { name: 'Empleados', href: '/employees', icon: UsersIcon },
  { name: 'Sorteos', href: '/raffles', icon: TrophyIcon },
  { name: 'Analytics', href: '/analytics', icon: ChartBarIcon },
  { name: 'Configuración', href: '/settings', icon: CogIcon },
];

export default function Sidebar() {
  const pathname = usePathname();

  return (
    <div className="flex flex-col w-64 bg-white shadow-lg">
      {/* Logo */}
      <div className="flex items-center justify-center h-16 px-4 bg-blue-600">
        <h1 className="text-xl font-bold text-white">Gasolinera JSM</h1>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-4 py-6 space-y-2">
        {navigation.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.name}
              href={item.href}
              className={`flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-colors ${
                isActive
                  ? 'bg-blue-100 text-blue-700 border-r-2 border-blue-700'
                  : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
              }`}
            >
              <item.icon className="w-5 h-5 mr-3" />
              {item.name}
            </Link>
          );
        })}
      </nav>

      {/* User Profile */}
      <div className="p-4 border-t border-gray-200">
        <div className="flex items-center mb-4">
          <div className="w-10 h-10 bg-gray-300 rounded-full mr-3"></div>
          <div>
            <p className="text-sm font-medium text-gray-900">Carlos Dueño</p>
            <p className="text-xs text-gray-500">Propietario</p>
          </div>
        </div>

        <button className="flex items-center w-full px-4 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg transition-colors">
          <ArrowRightOnRectangleIcon className="w-4 h-4 mr-2" />
          Cerrar Sesión
        </button>
      </div>
    </div>
  );
}
