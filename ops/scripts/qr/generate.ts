// QR Generation Script
// This would use a library like `jsonwebtoken` to create signed tokens.

import * as jwt from 'jsonwebtoken';
import { v4 as uuidv4 } from 'uuid';

const QR_SECRET = process.env.QR_SIGNATURE_SECRET || 'qr-code-super-secret-key-for-signing';

function generateQrToken(stationId: string, dispenserId: string) {
    const payload = {
        s: stationId,       // station id
        d: dispenserId,     // dispenser id
        n: uuidv4(),        // nonce
        t: Math.floor(Date.now() / 1000), // timestamp
        exp: Math.floor(Date.now() / 1000) + (60 * 5), // expires in 5 minutes
    };

    // In a real implementation, use ECDSA instead of HMAC for better security
    const token = jwt.sign(payload, QR_SECRET, { algorithm: 'HS256' });
    return token;
}

const count = parseInt(process.argv[2] || '10', 10);
console.log(`Generating ${count} QR codes for station JSM-GUA-01...`);

for (let i = 1; i <= count; i++) {
    const dispenser = `D${String(i).padStart(2, '0')}`;
    const qr = generateQrToken('JSM-GUA-01', dispenser);
    console.log(`-- Dispenser ${dispenser} --`);
    console.log(qr);
    console.log('');
}
