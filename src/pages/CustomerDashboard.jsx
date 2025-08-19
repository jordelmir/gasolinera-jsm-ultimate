import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { db } from '../firebase/config';
import { collection, query, where, onSnapshot } from 'firebase/firestore';

// Simulación de un componente de escáner QR. En un proyecto real, se usaría una librería.
const QRScanner = ({ onScan }) => (
    <div className="text-center p-4 border-2 border-dashed border-slate-600 rounded-lg">
        <p>Área de escáner QR</p>
        <button onClick={() => onScan('ID_DEL_QR_ESCANEADO')} className="mt-4 bg-slate-600 p-2 rounded">Simular Escaneo</button>
    </div>
);

const CustomerDashboard = () => {
    const { currentUser } = useAuth();
    const [totalTickets, setTotalTickets] = useState(0);
    const [myTokens, setMyTokens] = useState([]);
    const [showAd, setShowAd] = useState(false);
    const [scannedCoupon, setScannedCoupon] = useState(null);

    useEffect(() => {
        if (!currentUser) return;
        const q = query(collection(db, 'activatedCoupons'), where('userId', '==', currentUser.uid));
        const unsubscribe = onSnapshot(q, (querySnapshot) => {
            let tickets = 0;
            const tokens = [];
            querySnapshot.forEach((doc) => {
                tickets += doc.data().value;
                tokens.push({id: doc.id, ...doc.data()});
            });
            setTotalTickets(tickets);
            setMyTokens(tokens);
        });
        return unsubscribe;
    }, [currentUser]);

    const handleScan = (couponId) => {
        // Aquí iría la lógica para verificar el cupón en Firestore
        console.log("Cupón escaneado:", couponId);
        setScannedCoupon({ id: couponId, value: 2 }); // Simulación
        // Mostraría un modal para activar
    };
    
    // Lógica para activar, ver anuncio, duplicar, etc. iría aquí.

    return (
        <div className="min-h-screen bg-slate-900 text-white p-8">
            <header className="flex justify-between items-center mb-10">
                <h1 className="text-3xl font-bold text-cyan-400">Mi Dashboard</h1>
                <button onClick={() => auth.signOut()} className="bg-red-500 p-2 rounded">Cerrar Sesión</button>
            </header>

            <div className="grid md:grid-cols-3 gap-8">
                <div className="md:col-span-1 bg-slate-800 p-6 rounded-lg">
                    <h2 className="text-xl font-semibold mb-4">Escanear Cupón</h2>
                    <QRScanner onScan={handleScan} />
                </div>
                <div className="md:col-span-2 bg-slate-800 p-6 rounded-lg">
                    <div className="text-center mb-6">
                        <p className="text-slate-400">Total de Tickets para el Sorteo</p>
                        <p className="text-7xl font-bold text-green-400">{totalTickets}</p>
                    </div>
                    <h3 className="font-semibold mb-2">Mis Tokens de Sorteo</h3>
                    <ul className="space-y-2 max-h-60 overflow-y-auto">
                        {myTokens.map(token => (
                            <li key={token.id} className="bg-slate-700 p-2 rounded text-xs font-mono">{token.id}</li>
                        ))}
                    </ul>
                </div>
            </div>
        </div>
    );
};

export default CustomerDashboard;
