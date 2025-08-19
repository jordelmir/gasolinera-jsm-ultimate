import React, { useState, useEffect } from 'react';
import { collection, getDocs, doc, setDoc, deleteDoc, addDoc } from 'firebase/firestore';
import { db, auth } from "../firebase/config.js";
import { createUserWithEmailAndPassword } from 'firebase/auth';

const OwnerDashboard = () => {
    const [users, setUsers] = useState([]);
    const [stations, setStations] = useState([]);
    const [newUserEmail, setNewUserEmail] = useState('');
    const [newUserPassword, setNewUserPassword] = useState('');
    const [newUserRole, setNewUserRole] = useState('pistero');
    const [newStationName, setNewStationName] = useState('');

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        // Fetch Users
        const usersCol = collection(db, 'users');
        const userSnapshot = await getDocs(usersCol);
        const userList = userSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        setUsers(userList);

        // Fetch Stations
        const stationsCol = collection(db, 'stations');
        const stationSnapshot = await getDocs(stationsCol);
        const stationList = stationSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        setStations(stationList);
    };

    const handleAddUser = async (e) => {
        e.preventDefault();
        try {
            const userCredential = await createUserWithEmailAndPassword(auth, newUserEmail, newUserPassword);
            await setDoc(doc(db, 'users', userCredential.user.uid), {
                email: newUserEmail,
                role: newUserRole,
                createdAt: new Date()
            });
            alert('Usuario agregado exitosamente!');
            setNewUserEmail('');
            setNewUserPassword('');
            fetchData();
        } catch (error) {
            console.error('Error adding user:', error);
            alert('Error al agregar usuario: ' + error.message);
        }
    };

    const handleDeleteUser = async (userId) => {
        if (confirm('¿Estás seguro de que quieres eliminar a este usuario?')) {
            try {
                await deleteDoc(doc(db, 'users', userId));
                alert('Usuario eliminado exitosamente!');
                fetchData();
            } catch (error) {
                console.error('Error deleting user:', error);
                alert('Error al eliminar usuario: ' + error.message);
            }
        }
    };

    const handleAddStation = async (e) => {
        e.preventDefault();
        try {
            await addDoc(collection(db, 'stations'), {
                name: newStationName,
                createdAt: new Date()
            });
            alert('Gasolinera agregada exitosamente!');
            setNewStationName('');
            fetchData();
        } catch (error) {
            console.error('Error adding station:', error);
            alert('Error al agregar gasolinera: ' + error.message);
        }
    };

    const handleDeleteStation = async (stationId) => {
        if (confirm('¿Estás seguro de que quieres eliminar esta gasolinera?')) {
            try {
                await deleteDoc(doc(db, 'stations', stationId));
                alert('Gasolinera eliminada exitosamente!');
                fetchData();
            } catch (error) {
                console.error('Error deleting station:', error);
                alert('Error al eliminar gasolinera: ' + error.message);
            }
        }
    };

    return (
        <div className="min-h-screen bg-slate-900 text-white p-4">
            <h1 className="text-3xl font-bold text-cyan-400 mb-6">Dashboard del Dueño</h1>

            <div className="mb-8">
                <h2 className="text-2xl font-semibold mb-4">Gestión de Usuarios</h2>
                <form onSubmit={handleAddUser} className="mb-4 p-4 border border-slate-700 rounded-lg">
                    <h3 className="text-xl font-medium mb-2">Agregar Nuevo Usuario</h3>
                    <input
                        type="email"
                        placeholder="Email"
                        value={newUserEmail}
                        onChange={(e) => setNewUserEmail(e.target.value)}
                        className="w-full p-2 rounded bg-slate-700 border border-slate-600 focus:outline-none focus:ring-2 focus:ring-cyan-500 mb-2"
                        required
                    />
                    <input
                        type="password"
                        placeholder="Contraseña"
                        value={newUserPassword}
                        onChange={(e) => setNewUserPassword(e.target.value)}
                        className="w-full p-2 rounded bg-slate-700 border border-slate-600 focus:outline-none focus:ring-2 focus:ring-cyan-500 mb-2"
                        required
                    />
                    <select
                        value={newUserRole}
                        onChange={(e) => setNewUserRole(e.target.value)}
                        className="w-full p-2 rounded bg-slate-700 border border-slate-600 focus:outline-none focus:ring-2 focus:ring-cyan-500 mb-4"
                    >
                        <option value="pistero">Pistero</option>
                        <option value="dueño">Dueño</option>
                        <option value="cliente">Cliente</option>
                    </select>
                    <button type="submit" className="w-full bg-cyan-500 hover:bg-cyan-600 text-slate-900 font-bold py-2 px-4 rounded-lg transition-transform transform hover:scale-105">
                        Agregar Usuario
                    </button>
                </form>

                <h3 className="text-xl font-medium mb-2">Lista de Usuarios</h3>
                <ul>
                    {users.map(user => (
                        <li key={user.id} className="bg-slate-800 p-3 rounded-lg mb-2 flex justify-between items-center">
                            <span>{user.email} ({user.role})</span>
                            <button onClick={() => handleDeleteUser(user.id)} className="bg-red-500 hover:bg-red-600 text-white font-bold py-1 px-3 rounded-lg text-sm">
                                Eliminar
                            </button>
                        </li>
                    ))}
                </ul>
            </div>

            <div>
                <h2 className="text-2xl font-semibold mb-4">Gestión de Gasolineras</h2>
                <form onSubmit={handleAddStation} className="mb-4 p-4 border border-slate-700 rounded-lg">
                    <h3 className="text-xl font-medium mb-2">Agregar Nueva Gasolinera</h3>
                    <input
                        type="text"
                        placeholder="Nombre de la Gasolinera"
                        value={newStationName}
                        onChange={(e) => setNewStationName(e.target.value)}
                        className="w-full p-2 rounded bg-slate-700 border border-slate-600 focus:outline-none focus:ring-2 focus:ring-cyan-500 mb-4"
                        required
                    />
                    <button type="submit" className="w-full bg-cyan-500 hover:bg-cyan-600 text-slate-900 font-bold py-2 px-4 rounded-lg transition-transform transform hover:scale-105">
                        Agregar Gasolinera
                    </button>
                </form>

                <h3 className="text-xl font-medium mb-2">Lista de Gasolineras</h3>
                <ul>
                    {stations.map(station => (
                        <li key={station.id} className="bg-slate-800 p-3 rounded-lg mb-2 flex justify-between items-center">
                            <span>{station.name}</span>
                            <button onClick={() => handleDeleteStation(station.id)} className="bg-red-500 hover:bg-red-600 text-white font-bold py-1 px-3 rounded-lg text-sm">
                                Eliminar
                            </button>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
};

export default OwnerDashboard;