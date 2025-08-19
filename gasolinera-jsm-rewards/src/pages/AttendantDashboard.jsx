import React, { useState } from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { collection, addDoc, serverTimestamp } from 'firebase/firestore';
import { db } from "../firebase/config.js";
import { useAuth } from '../context/AuthContext';

const AttendantDashboard = () => {
    const [multiples, setMultiples] = useState(1);
    const [qrValue, setQrValue] = useState(null);
    const { currentUser } = useAuth();

    const generateQR = async () => {
        const newCoupon = {
            value: multiples,
            generatedBy: currentUser.uid,
            createdAt: serverTimestamp(),
            status: 'generated' // generated, scanned, activated
        };
        const docRef = await addDoc(collection(db, 'coupons'), newCoupon);
        setQrValue(docRef.id);
    };

    const reset = () => {
        setMultiples(1);
        setQrValue(null);
    };

    if (qrValue) {
        return (
            <div className="min-h-screen flex flex-col items-center justify-center bg-slate-900 text-white p-4">
                <h1 className="text-3xl font-bold text-cyan-400 mb-4">Escanee el Código</h1>
                <div className="bg-white p-4 rounded-lg">
                    <QRCodeSVG value={qrValue} size={256} />
                </div>
                <p className="mt-4 text-lg">Este QR otorga {multiples} ticket(s).</p>
                <button onClick={reset} className="mt-8 bg-cyan-500 hover:bg-cyan-600 text-slate-900 font-bold py-3 px-6 rounded-lg text-xl">
                    Siguiente Cliente
                </button>
            </div>
        );
    }

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-slate-900 text-white p-4">
            <h1 className="text-4xl font-bold text-cyan-400 mb-8">Generador de Cupones</h1>
            <p className="text-xl mb-4">Cantidad de tickets a generar:</p>
            <div className="flex items-center gap-4 mb-8">
                <button onClick={() => setMultiples(m => Math.max(1, m - 1))} className="bg-slate-700 w-16 h-16 rounded-full text-4xl">-</button>
                <span className="text-7xl font-bold w-24 text-center">{multiples}</span>
                <button onClick={() => setMultiples(m => m + 1)} className="bg-slate-700 w-16 h-16 rounded-full text-4xl">+</button>
            </div>
            <button onClick={generateQR} className="bg-cyan-500 hover:bg-cyan-600 text-slate-900 font-bold py-3 px-6 rounded-lg text-xl transition-transform transform hover:scale-105">
                Generar QR
            </button>
        </div>
    );
};

export default AttendantDashboard;