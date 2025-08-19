import React, { useState, useEffect } from 'react';
import { collection, query, where, getDocs, doc, updateDoc, serverTimestamp, getDoc } from 'firebase/firestore';
import { db } from "../firebase/config.js";
import { useAuth } from '../context/AuthContext';

const CustomerDashboard = () => {
    const { currentUser } = useAuth();
    const [qrCode, setQrCode] = useState('');
    const [tickets, setTickets] = useState([]);
    const [message, setMessage] = useState('');

    useEffect(() => {
        if (currentUser) {
            fetchTickets();
        }
    }, [currentUser]);

    const fetchTickets = async () => {
        const q = query(collection(db, 'coupons'), where('scannedBy', '==', currentUser.uid));
        const querySnapshot = await getDocs(q);
        const fetchedTickets = [];
        querySnapshot.forEach((doc) => {
            fetchedTickets.push({ id: doc.id, ...doc.data() });
        });
        setTickets(fetchedTickets);
    };

    const handleScanQR = async () => {
        // En una aplicación real, aquí se integraría un escáner de QR.
        // Por ahora, simulamos la entrada del QR.
        const couponRef = doc(db, 'coupons', qrCode);
        const couponDoc = await getDoc(couponRef);

        if (couponDoc.exists() && couponDoc.data().status === 'generated') {
            await updateDoc(couponRef, {
                scannedBy: currentUser.uid,
                scannedAt: serverTimestamp(),
                status: 'scanned'
            });
            setMessage(`¡Cupón activado! Has ganado ${couponDoc.data().value} ticket(s).`);
            fetchTickets();
            setQrCode('');
        } else {
            setMessage('Código QR inválido o ya utilizado.');
        }
    };

    const handleWatchAd = async (ticketId) => {
        // Simular ver un anuncio y duplicar tickets
        const ticketRef = doc(db, 'coupons', ticketId);
        const ticketDoc = await getDoc(ticketRef);

        if (ticketDoc.exists() && !ticketDoc.data().adWatched) {
            await updateDoc(ticketRef, {
                value: ticketDoc.data().value * 2,
                adWatched: true,
                adWatchedAt: serverTimestamp()
            });
            setMessage('¡Anuncio visto! Tus tickets se han duplicado.');
            fetchTickets();
        } else {
            setMessage('Este ticket ya ha duplicado sus puntos o no es válido.');
        }
    };

    return (
        <div className="min-h-screen bg-slate-900 text-white p-4">
            <h1 className="text-3xl font-bold text-cyan-400 mb-6">Bienvenido, Cliente!</h1>

            <div className="mb-8">
                <h2 className="text-2xl font-semibold mb-4">Escanear QR</h2>
                <input
                    type="text"
                    value={qrCode}
                    onChange={(e) => setQrCode(e.target.value)}
                    placeholder="Introduce el código QR"
                    className="w-full p-2 rounded bg-slate-700 border border-slate-600 focus:outline-none focus:ring-2 focus:ring-cyan-500 mb-4"
                />
                <button onClick={handleScanQR} className="w-full bg-cyan-500 hover:bg-cyan-600 text-slate-900 font-bold py-2 px-4 rounded-lg transition-transform transform hover:scale-105">
                    Activar Cupón
                </button>
                {message && <p className="mt-4 text-center text-lg">{message}</p>}
            </div>

            <div>
                <h2 className="text-2xl font-semibold mb-4">Mis Tickets</h2>
                {tickets.length === 0 ? (
                    <p>No tienes tickets activos.</p>
                ) : (
                    <ul>
                        {tickets.map((ticket) => (
                            <li key={ticket.id} className="bg-slate-800 p-4 rounded-lg mb-4 flex justify-between items-center">
                                <span>Tickets: {ticket.value}</span>
                                {!ticket.adWatched && (
                                    <button onClick={() => handleWatchAd(ticket.id)} className="bg-green-500 hover:bg-green-600 text-white font-bold py-1 px-3 rounded-lg text-sm">
                                        Ver Anuncio (Duplicar)
                                    </button>
                                )}
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </div>
    );
};

export default CustomerDashboard;