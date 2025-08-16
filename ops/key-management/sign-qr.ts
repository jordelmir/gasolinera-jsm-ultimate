import * as crypto from 'crypto';
import * as fs from 'fs';
import { v4 as uuidv4 } from 'uuid';

function sign(payload: object, privateKey: string): string {
  const payloadBase64 = Buffer.from(JSON.stringify(payload)).toString('base64url');
  
  const signer = crypto.createSign('sha256');
  signer.update(payloadBase64);
  signer.end();
  
  const signature = signer.sign(privateKey, 'base64url');
  
  return `${payloadBase64}.${signature}`;
}

const privateKey = fs.readFileSync('private-key.pem', 'utf-8');

const qrPayload = {
  s: "JSM-01-ALAJUELITA", // ID de la estación
  d: "D03",             // ID del dispensador
  n: uuidv4(),          // Nonce único
  t: Math.floor(Date.now() / 1000), // Timestamp
  exp: Math.floor(Date.now() / 1000) + 3600, // Expira en 1 hora
};

const signedQrToken = sign(qrPayload, privateKey);

console.log('🔑 QR Token Firmado:\n', signedQrToken);
