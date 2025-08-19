import React from 'react';
// En un proyecto real, se importaría una librería de gráficos como Recharts o Chart.js
// import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from 'recharts';

const OwnerDashboard = () => {
    // Datos de ejemplo
    const data = [
        { name: 'Pistero A', tickets: 400 },
        { name: 'Pistero B', tickets: 300 },
        { name: 'Pistero C', tickets: 500 },
    ];

    return (
        <div className="min-h-screen bg-slate-900 text-white p-8">
            <header className="flex justify-between items-center mb-10">
                <h1 className="text-3xl font-bold text-cyan-400">Dashboard de Administrador</h1>
                <button onClick={() => auth.signOut()} className="bg-red-500 p-2 rounded">Cerrar Sesión</button>
            </header>

            <div className="grid lg:grid-cols-2 gap-8">
                {/* Gráfico de rendimiento */}
                <div className="bg-slate-800 p-6 rounded-lg">
                    <h2 className="text-xl font-semibold mb-4">Tickets Generados por Empleado</h2>
                    <div className="h-64 bg-slate-700 flex items-center justify-center rounded">
                        <p>(Aquí iría un gráfico de barras)</p>
                    </div>
                </div>

                {/* Gestión de empleados */}
                <div className="bg-slate-800 p-6 rounded-lg">
                    <h2 className="text-xl font-semibold mb-4">Gestionar Empleados</h2>
                    <button className="w-full bg-cyan-500 p-2 rounded mb-4">Agregar Nuevo Empleado</button>
                    <ul>
                        <li className="flex justify-between items-center bg-slate-700 p-2 rounded mb-2">
                            <span>empleado1@gas.com</span>
                            <button className="text-red-500">Eliminar</button>
                        </li>
                         <li className="flex justify-between items-center bg-slate-700 p-2 rounded mb-2">
                            <span>empleado2@gas.com</span>
                            <button className="text-red-500">Eliminar</button>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    );
};

export default OwnerDashboard;
