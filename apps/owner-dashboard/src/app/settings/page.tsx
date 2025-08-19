'use client';

import React, { useState } from 'react';
import {
  CogIcon,
  BellIcon,
  ShieldCheckIcon,
  CurrencyDollarIcon,
  GlobeAltIcon,
  UserIcon,
  KeyIcon,
} from '@heroicons/react/24/outline';

export default function SettingsPage() {
  const [settings, setSettings] = useState({
    notifications: {
      email: true,
      push: true,
      sms: false,
      weeklyReports: true,
      raffleAlerts: true,
    },
    business: {
      companyName: 'Gasolinera JSM',
      taxId: '3-101-123456',
      phone: '+506 2222-3333',
      email: 'admin@gasolinera-jsm.com',
      address: 'San José, Costa Rica',
    },
    raffle: {
      weeklyPrize: 40000,
      annualPrize: 15000000,
      drawDay: 'sunday',
      drawTime: '15:00',
      autoDrawEnabled: true,
    },
    security: {
      twoFactorEnabled: false,
      sessionTimeout: 30,
      passwordExpiry: 90,
    },
  });

  const handleSettingChange = (category: string, key: string, value: any) => {
    setSettings((prev) => ({
      ...prev,
      [category]: {
        ...prev[category as keyof typeof prev],
        [key]: value,
      },
    }));
  };

  const handleSave = () => {
    // Aquí se guardarían los settings en la API
    alert('Configuración guardada exitosamente');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                Configuración
              </h1>
              <p className="text-sm text-gray-500 mt-1">
                Administra la configuración de tu sistema
              </p>
            </div>
            <button
              onClick={handleSave}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Guardar Cambios
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Sidebar */}
          <div className="lg:col-span-1">
            <nav className="space-y-2">
              {[
                {
                  id: 'business',
                  name: 'Información del Negocio',
                  icon: UserIcon,
                },
                { id: 'notifications', name: 'Notificaciones', icon: BellIcon },
                {
                  id: 'raffle',
                  name: 'Configuración de Sorteos',
                  icon: CurrencyDollarIcon,
                },
                { id: 'security', name: 'Seguridad', icon: ShieldCheckIcon },
              ].map((item) => (
                <a
                  key={item.id}
                  href={`#${item.id}`}
                  className="flex items-center px-3 py-2 text-sm font-medium text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg"
                >
                  <item.icon className="w-5 h-5 mr-3" />
                  {item.name}
                </a>
              ))}
            </nav>
          </div>

          {/* Content */}
          <div className="lg:col-span-2 space-y-8">
            {/* Business Information */}
            <div
              id="business"
              className="bg-white rounded-xl shadow-sm border border-gray-200"
            >
              <div className="p-6 border-b border-gray-200">
                <h3 className="text-lg font-semibold text-gray-900 flex items-center">
                  <UserIcon className="w-5 h-5 mr-2" />
                  Información del Negocio
                </h3>
              </div>

              <div className="p-6 space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Nombre de la Empresa
                    </label>
                    <input
                      type="text"
                      value={settings.business.companyName}
                      onChange={(e) =>
                        handleSettingChange(
                          'business',
                          'companyName',
                          e.target.value
                        )
                      }
                      className="w-full border border-gray-300 rounded-lg px-3 py-2"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Cédula Jurídica
                    </label>
                    <input
                      type="text"
                      value={settings.business.taxId}
                      onChange={(e) =>
                        handleSettingChange('business', 'taxId', e.target.value)
                      }
                      className="w-full border border-gray-300 rounded-lg px-3 py-2"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Teléfono
                    </label>
                    <input
                      type="tel"
                      value={settings.business.phone}
                      onChange={(e) =>
                        handleSettingChange('business', 'phone', e.target.value)
                      }
                      className="w-full border border-gray-300 rounded-lg px-3 py-2"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Email
                    </label>
                    <input
                      type="email"
                      value={settings.business.email}
                      onChange={(e) =>
                        handleSettingChange('business', 'email', e.target.value)
                      }
                      className="w-full border border-gray-300 rounded-lg px-3 py-2"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Dirección
                  </label>
                  <textarea
                    value={settings.business.address}
                    onChange={(e) =>
                      handleSettingChange('business', 'address', e.target.value)
                    }
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
                    rows={3}
                  />
                </div>
              </div>
            </div>

            {/* Notifications */}
            <div
              id="notifications"
              className="bg-white rounded-xl shadow-sm border border-gray-200"
            >
              <div className="p-6 border-b border-gray-200">
                <h3 className="text-lg font-semibold text-gray-900 flex items-center">
                  <BellIcon className="w-5 h-5 mr-2" />
                  Notificaciones
                </h3>
              </div>

              <div className="p-6 space-y-4">
                {[
                  {
                    key: 'email',
                    label: 'Notificaciones por Email',
                    description: 'Recibir alertas importantes por correo',
                  },
                  {
                    key: 'push',
                    label: 'Notificaciones Push',
                    description:
                      'Notificaciones en tiempo real en el navegador',
                  },
                  {
                    key: 'sms',
                    label: 'Notificaciones SMS',
                    description: 'Alertas críticas por mensaje de texto',
                  },
                  {
                    key: 'weeklyReports',
                    label: 'Reportes Semanales',
                    description: 'Resumen semanal de rendimiento',
                  },
                  {
                    key: 'raffleAlerts',
                    label: 'Alertas de Sorteos',
                    description: 'Notificaciones sobre sorteos y ganadores',
                  },
                ].map((notification) => (
                  <div
                    key={notification.key}
                    className="flex items-center justify-between"
                  >
                    <div>
                      <h4 className="text-sm font-medium text-gray-900">
                        {notification.label}
                      </h4>
                      <p className="text-sm text-gray-500">
                        {notification.description}
                      </p>
                    </div>
                    <label className="relative inline-flex items-center cursor-pointer">
                      <input
                        type="checkbox"
                        checked={
                          settings.notifications[
                            notification.key as keyof typeof settings.notifications
                          ]
                        }
                        onChange={(e) =>
                          handleSettingChange(
                            'notifications',
                            notification.key,
                            e.target.checked
                          )
                        }
                        className="sr-only peer"
                      />
                      <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                    </label>
                  </div>
                ))}
              </div>
            </div>

            {/* Raffle Configuration */}
            <div
              id="raffle"
              className="bg-white rounded-xl shadow-sm border border-gray-200"
            >
              <div className="p-6 border-b border-gray-200">
                <h3 className="text-lg font-semibold text-gray-900 flex items-center">
                  <CurrencyDollarIcon className="w-5 h-5 mr-2" />
                  Configuración de Sorteos
                </h3>
              </div>

              <div className="p-6 space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Premio Semanal (₡)
                    </label>
                    <input
                      type="number"
                      value={settings.raffle.weeklyPrize}
                      onChange={(e) =>
                        handleSettingChange(
                          'raffle',
                          'weeklyPrize',
                          parseInt(e.target.value)
                        )
                      }
                      className="w-full border border-gray-300 rounded-lg px-3 py-2"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Premio Anual (₡)
                    </label>
                    <input
                      type="number"
                      value={settings.raffle.annualPrize}
                      onChange={(e) =>
                        handleSettingChange(
                          'raffle',
                          'annualPrize',
                          parseInt(e.target.value)
                        )
                      }
                      className="w-full border border-gray-300 rounded-lg px-3 py-2"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Día del Sorteo Semanal
                    </label>
                    <select
                      value={settings.raffle.drawDay}
                      onChange={(e) =>
                        handleSettingChange('raffle', 'drawDay', e.target.value)
                      }
                      className="w-full border border-gray-300 rounded-lg px-3 py-2"
                    >
                      <option value="sunday">Domingo</option>
                      <option value="monday">Lunes</option>
                      <option value="tuesday">Martes</option>
                      <option value="wednesday">Miércoles</option>
                      <option value="thursday">Jueves</option>
                      <option value="friday">Viernes</option>
                      <option value="saturday">Sábado</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Hora del Sorteo
                    </label>
                    <input
                      type="time"
                      value={settings.raffle.drawTime}
                      onChange={(e) =>
                        handleSettingChange(
                          'raffle',
                          'drawTime',
                          e.target.value
                        )
                      }
                      className="w-full border border-gray-300 rounded-lg px-3 py-2"
                    />
                  </div>
                </div>

                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="text-sm font-medium text-gray-900">
                      Sorteo Automático
                    </h4>
                    <p className="text-sm text-gray-500">
                      Realizar sorteos automáticamente en la fecha programada
                    </p>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      checked={settings.raffle.autoDrawEnabled}
                      onChange={(e) =>
                        handleSettingChange(
                          'raffle',
                          'autoDrawEnabled',
                          e.target.checked
                        )
                      }
                      className="sr-only peer"
                    />
                    <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                  </label>
                </div>
              </div>
            </div>

            {/* Security */}
            <div
              id="security"
              className="bg-white rounded-xl shadow-sm border border-gray-200"
            >
              <div className="p-6 border-b border-gray-200">
                <h3 className="text-lg font-semibold text-gray-900 flex items-center">
                  <ShieldCheckIcon className="w-5 h-5 mr-2" />
                  Seguridad
                </h3>
              </div>

              <div className="p-6 space-y-4">
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="text-sm font-medium text-gray-900">
                      Autenticación de Dos Factores
                    </h4>
                    <p className="text-sm text-gray-500">
                      Agregar una capa extra de seguridad a tu cuenta
                    </p>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      checked={settings.security.twoFactorEnabled}
                      onChange={(e) =>
                        handleSettingChange(
                          'security',
                          'twoFactorEnabled',
                          e.target.checked
                        )
                      }
                      className="sr-only peer"
                    />
                    <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                  </label>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Tiempo de Sesión (minutos)
                    </label>
                    <input
                      type="number"
                      value={settings.security.sessionTimeout}
                      onChange={(e) =>
                        handleSettingChange(
                          'security',
                          'sessionTimeout',
                          parseInt(e.target.value)
                        )
                      }
                      className="w-full border border-gray-300 rounded-lg px-3 py-2"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Expiración de Contraseña (días)
                    </label>
                    <input
                      type="number"
                      value={settings.security.passwordExpiry}
                      onChange={(e) =>
                        handleSettingChange(
                          'security',
                          'passwordExpiry',
                          parseInt(e.target.value)
                        )
                      }
                      className="w-full border border-gray-300 rounded-lg px-3 py-2"
                    />
                  </div>
                </div>

                <div className="pt-4 border-t border-gray-200">
                  <button className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors">
                    Cambiar Contraseña
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
