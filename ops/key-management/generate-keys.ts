import * as crypto from 'crypto';
import * as fs from 'fs';

// Genera un par de claves ECDSA (curva secp256k1, la misma de Bitcoin/Ethereum)
const { privateKey, publicKey } = crypto.generateKeyPairSync('ec', {
  namedCurve: 'secp256k1',
  publicKeyEncoding: { type: 'spki', format: 'pem' },
  privateKeyEncoding: { type: 'pkcs8', format: 'pem' },
});

fs.writeFileSync('private-key.pem', privateKey);
fs.writeFileSync('public-key.pem', publicKey);

console.log('✅ Claves ECDSA generadas: private-key.pem, public-key.pem');
console.log('🔒 Protege private-key.pem como si fuera oro. Súbela a tu gestor de secretos (Vault, KMS).');
console.log('📢 Distribuye public-key.pem a los servicios que necesiten verificar firmas (redemption-service, app móvil).');
