#!/usr/bin/env ts-node

import { Client } from 'pg';
import { createHash, randomBytes } from 'crypto';

interface Station {
  id: string;
  name: string;
  address: string;
  ownerId: string;
}

interface Employee {
  id: string;
  name: string;
  email: string;
  stationId: string;
  role: string;
}

interface User {
  id: string;
  name: string;
  email: string;
  phone: string;
  role: string;
}

const generateId = () => randomBytes(16).toString('hex');
const hashPassword = (password: string) =>
  createHash('sha256').update(password).digest('hex');

async function seedCouponSystem() {
  const client = new Client({
    host: process.env.POSTGRES_HOST || 'localhost',
    port: parseInt(process.env.POSTGRES_PORT || '5432'),
    database: process.env.POSTGRES_DB || 'puntog',
    user: process.env.POSTGRES_USER || 'puntog',
    password: process.env.POSTGRES_PASSWORD || 'changeme',
  });

  try {
    await client.connect();
    console.log('🔌 Conectado a PostgreSQL');

    // Crear tablas si no existen
    await createTables(client);
    console.log('📋 Tablas creadas/verificadas');

    // Crear usuariosprueba
    const users = await createTestUsers(client);
    console.log(`👥 ${users.length} usuarios de prueba creados`);

    // Crear estaciones de prueba
    const stations = await createTestStations(client, users);
    console.log(`⛽ ${stations.length} estaciones de prueba creadas`);

    // Crear empleados de prueba
    const employees = await createTestEmployees(client, stations);
    console.log(`👷 ${employees.length} empleados de prueba creados`);

    // Crear cupones de prueba
    const coupons = await createTestCoupons(client, stations, employees, users);
    console.log(`🎫 ${coupons.length} cupones de prueba creados`);

    // Crear anuncios de prueba
    const ads = await createTestAds(client, users);
    console.log(`📺 ${ads.length} anuncios de prueba creados`);

    console.log('\n✅ Seeding completado exitosamente!');
    console.log('\n📋 Credenciales de prueba:');
    console.log('👤 Cliente: cliente@test.com / password123');
    console.log('👷 Empleado: empleado@test.com / password123');
    console.log('🏢 Dueño: dueno@test.com / password123');
  } catch (error) {
    console.error('❌ Error durante el seeding:', error);
    throw error;
  } finally {
    await client.end();
  }
}

async function createTables(client: Client) {
  const queries = [
    // Users table
    `CREATE TABLE IF NOT EXISTS users (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      name VARCHAR(255) NOT NULL,
      email VARCHAR(255) UNIQUE NOT NULL,
      phone VARCHAR(20),
      password_hash VARCHAR(255) NOT NULL,
      role VARCHAR(50) NOT NULL DEFAULT 'CLIENT',
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )`,

    // Stations table
    `CREATE TABLE IF NOT EXISTS stations (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      name VARCHAR(255) NOT NULL,
      address TEXT NOT NULL,
      owner_id UUID NOT NULL REFERENCES users(id),
      latitude DECIMAL(10, 8),
      longitude DECIMAL(11, 8),
      is_active BOOLEAN DEFAULT true,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )`,

    // Employees table
    `CREATE TABLE IF NOT EXISTS employees (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id UUID NOT NULL REFERENCES users(id),
      station_id UUID NOT NULL REFERENCES stations(id),
      employee_code VARCHAR(50) UNIQUE NOT NULL,
      is_active BOOLEAN DEFAULT true,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )`,

    // QR Coupons table
    `CREATE TABLE IF NOT EXISTS qr_coupons (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      qr_code TEXT UNIQUE NOT NULL,
      token VARCHAR(255) UNIQUE NOT NULL,
      station_id UUID NOT NULL REFERENCES stations(id),
      employee_id UUID NOT NULL,
      amount INTEGER NOT NULL,
      base_tickets INTEGER NOT NULL,
      bonus_tickets INTEGER DEFAULT 0,
      total_tickets INTEGER NOT NULL,
      status VARCHAR(50) DEFAULT 'GENERATED',
      scanned_by UUID REFERENCES users(id),
      scanned_at TIMESTAMP,
      activated_at TIMESTAMP,
      expires_at TIMESTAMP NOT NULL,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )`,

    // Advertisements table
    `CREATE TABLE IF NOT EXISTS advertisements (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      title VARCHAR(255) NOT NULL,
      description TEXT,
      video_url TEXT NOT NULL,
      duration INTEGER NOT NULL,
      advertiser_id UUID NOT NULL REFERENCES users(id),
      category VARCHAR(50) DEFAULT 'GENERAL',
      status VARCHAR(50) DEFAULT 'ACTIVE',
      priority INTEGER DEFAULT 1,
      max_views INTEGER DEFAULT -1,
      current_views INTEGER DEFAULT 0,
      start_date TIMESTAMP,
      end_date TIMESTAMP,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )`,

    // Ad Sequences table
    `CREATE TABLE IF NOT EXISTS ad_sequences (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      coupon_id UUID NOT NULL REFERENCES qr_coupons(id),
      user_id UUID NOT NULL REFERENCES users(id),
      current_step INTEGER DEFAULT 1,
      max_steps INTEGER DEFAULT 10,
      base_tickets INTEGER NOT NULL,
      current_tickets INTEGER NOT NULL,
      status VARCHAR(50) DEFAULT 'ACTIVE',
      completed_at TIMESTAMP,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )`,

    // Ad Views table
    `CREATE TABLE IF NOT EXISTS ad_views (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      coupon_id UUID NOT NULL REFERENCES qr_coupons(id),
      user_id UUID NOT NULL REFERENCES users(id),
      ad_id UUID NOT NULL REFERENCES advertisements(id),
      duration INTEGER NOT NULL,
      sequence INTEGER NOT NULL,
      tickets_earned INTEGER NOT NULL,
      status VARCHAR(50) DEFAULT 'STARTED',
      completed_at TIMESTAMP,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )`,

    // Raffles table
    `CREATE TABLE IF NOT EXISTS raffles (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      name VARCHAR(255) NOT NULL,
      description TEXT,
      prize_description TEXT NOT NULL,
      prize_value DECIMAL(12, 2),
      type VARCHAR(50) NOT NULL, -- 'WEEKLY' or 'ANNUAL'
      status VARCHAR(50) DEFAULT 'ACTIVE',
      draw_date TIMESTAMP NOT NULL,
      winner_id UUID REFERENCES users(id),
      winning_ticket_id UUID REFERENCES qr_coupons(id),
      total_participants INTEGER DEFAULT 0,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )`,
  ];

  for (const query of queries) {
    await client.query(query);
  }
}

async function createTestUsers(client: Client): Promise<User[]> {
  const users: User[] = [
    {
      id: generateId(),
      name: 'Juan Cliente',
      email: 'cliente@test.com',
      phone: '+506 8888-1111',
      role: 'CLIENT',
    },
    {
      id: generateId(),
      name: 'María Empleada',
      email: 'empleado@test.com',
      phone: '+506 8888-2222',
      role: 'EMPLOYEE',
    },
    {
      id: generateId(),
      name: 'Carlos Dueño',
      email: 'dueno@test.com',
      phone: '+506 8888-3333',
      role: 'OWNER',
    },
    {
      id: generateId(),
      name: 'Ana Cliente',
      email: 'ana@test.com',
      phone: '+506 8888-4444',
      role: 'CLIENT',
    },
    {
      id: generateId(),
      name: 'Pedro Anunciante',
      email: 'anunciante@test.com',
      phone: '+506 8888-5555',
      role: 'ADVERTISER',
    },
  ];

  const passwordHash = hashPassword('password123');

  for (const user of users) {
    await client.query(
      `INSERT INTO users (id, name, email, phone, password_hash, role)
       VALUES ($1, $2, $3, $4, $5, $6) ON CONFLICT (email) DO NOTHING`,
      [user.id, user.name, user.email, user.phone, passwordHash, user.role]
    );
  }

  return users;
}

async function createTestStations(
  client: Client,
  users: User[]
): Promise<Station[]> {
  const owner = users.find((u) => u.role === 'OWNER')!;

  const stations: Station[] = [
    {
      id: generateId(),
      name: 'Gasolinera JSM Centro',
      address: 'Avenida Central, San José, Costa Rica',
      ownerId: owner.id,
    },
    {
      id: generateId(),
      name: 'Gasolinera JSM Norte',
      address: 'Barrio Escalante, San José, Costa Rica',
      ownerId: owner.id,
    },
    {
      id: generateId(),
      name: 'Gasolinera JSM Sur',
      address: 'Desamparados, San José, Costa Rica',
      ownerId: owner.id,
    },
  ];

  for (const station of stations) {
    await client.query(
      `INSERT INTO stations (id, name, address, owner_id, latitude, longitude)
       VALUES ($1, $2, $3, $4, $5, $6)`,
      [
        station.id,
        station.name,
        station.address,
        station.ownerId,
        9.9281,
        -84.0907,
      ]
    );
  }

  return stations;
}

async function createTestEmployees(
  client: Client,
  stations: Station[]
): Promise<Employee[]> {
  const employees: Employee[] = [
    {
      id: generateId(),
      name: 'María Empleada',
      email: 'empleado@test.com',
      stationId: stations[0].id,
      role: 'EMPLOYEE',
    },
    {
      id: generateId(),
      name: 'José Dispensador',
      email: 'jose@test.com',
      stationId: stations[1].id,
      role: 'EMPLOYEE',
    },
    {
      id: generateId(),
      name: 'Carmen Cajera',
      email: 'carmen@test.com',
      stationId: stations[2].id,
      role: 'EMPLOYEE',
    },
  ];

  for (const employee of employees) {
    // Buscar el user_id correspondiente
    const userResult = await client.query(
      'SELECT id FROM users WHERE email = $1',
      [employee.email]
    );

    if (userResult.rows.length > 0) {
      await client.query(
        `INSERT INTO employees (id, user_id, station_id, employee_code)
         VALUES ($1, $2, $3, $4)`,
        [
          employee.id,
          userResult.rows[0].id,
          employee.stationId,
          `EMP-${employee.id.slice(0, 8)}`,
        ]
      );
    }
  }

  return employees;
}

async function createTestCoupons(
  client: Client,
  stations: Station[],
  employees: Employee[],
  users: User[]
) {
  const coupons = [];
  const clientUsers = users.filter((u) => u.role === 'CLIENT');

  for (let i = 0; i < 20; i++) {
    const station = stations[i % stations.length];
    const employee = employees.find((e) => e.stationId === station.id)!;
    const client = clientUsers[i % clientUsers.length];

    const amount = Math.floor(Math.random() * 5) + 1; // 1-5 múltiplos
    const token = `TOKEN-${generateId().slice(0, 12).toUpperCase()}`;
    const qrCode = `QR-${token}-${Date.now()}`;

    const couponId = generateId();
    const expiresAt = new Date();
    expiresAt.setHours(expiresAt.getHours() + 24);

    await client.query(
      `INSERT INTO qr_coupons (
        id, qr_code, token, station_id, employee_id, amount,
        base_tickets, total_tickets, status, scanned_by,
        scanned_at, expires_at
      ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)`,
      [
        couponId,
        qrCode,
        token,
        station.id,
        employee.id,
        amount,
        amount,
        amount,
        'SCANNED',
        client.id,
        new Date(),
        expiresAt,
      ]
    );

    coupons.push({ id: couponId, token, amount });
  }

  return coupons;
}

async function createTestAds(client: Client, users: User[]) {
  const advertiser = users.find((u) => u.role === 'ADVERTISER')!;

  const ads = [
    {
      title: 'Coca-Cola Refrescante',
      description: 'La bebida que refresca tu día',
      videoUrl: 'https://example.com/ads/coca-cola.mp4',
      duration: 15,
      category: 'FOOD_BEVERAGE',
    },
    {
      title: 'Toyota Nuevos Modelos',
      description: 'Descubre la nueva línea Toyota 2024',
      videoUrl: 'https://example.com/ads/toyota.mp4',
      duration: 30,
      category: 'AUTOMOTIVE',
    },
    {
      title: 'Samsung Galaxy',
      description: 'El smartphone que necesitas',
      videoUrl: 'https://example.com/ads/samsung.mp4',
      duration: 20,
      category: 'TECHNOLOGY',
    },
  ];

  for (const ad of ads) {
    await client.query(
      `INSERT INTO advertisements (
        title, description, video_url, duration, advertiser_id, category
      ) VALUES ($1, $2, $3, $4, $5, $6)`,
      [
        ad.title,
        ad.description,
        ad.videoUrl,
        ad.duration,
        advertiser.id,
        ad.category,
      ]
    );
  }

  return ads;
}

// Ejecutar el seeding
if (require.main === module) {
  seedCouponSystem()
    .then(() => {
      console.log('🎉 Seeding completado exitosamente');
      process.exit(0);
    })
    .catch((error) => {
      console.error('💥 Error en seeding:', error);
      process.exit(1);
    });
}

export { seedCouponSystem };
