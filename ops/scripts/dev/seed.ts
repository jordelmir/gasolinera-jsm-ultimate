import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  console.log(`Start seeding ...`);

  // 1. Seed Advertisers
  console.log('Seeding advertisers...');
  const advertiser1 = await prisma.advertiser.upsert({
    where: { name: 'Refrescos Tropical' },
    update: {},
    create: {
      name: 'Refrescos Tropical',
      contact_info: 'mercadeo@tropical.cr',
    },
  });

  const advertiser2 = await prisma.advertiser.upsert({
    where: { name: 'Tosty Snacks' },
    update: {},
    create: {
      name: 'Tosty Snacks',
      contact_info: 'ventas@tosty.com',
    },
  });

  const advertiser3 = await prisma.advertiser.upsert({
    where: { name: 'INS Seguros' },
    update: {},
    create: {
      name: 'INS Seguros',
      contact_info: 'seguros@ins.cr',
    },
  });

  // 2. Seed Ad Campaigns
  console.log('Seeding campaigns...');
  await prisma.adCampaign.create({
    data: {
      name: 'Verano Tropical 2025',
      advertiserId: advertiser1.id,
      start_date: new Date('2025-08-01'),
      end_date: new Date('2025-09-30'),
      budget: 5000.00,
      ad_url: 'https://example.com/tropical_verano.mp4',
    },
  });

  await prisma.adCampaign.create({
    data: {
      name: 'Tosty Crujiente',
      advertiserId: advertiser2.id,
      start_date: new Date('2025-07-15'),
      end_date: new Date('2025-08-30'),
      budget: 3500.00,
      ad_url: 'https://example.com/tosty_crujiente.mp4',
    },
  });

  // 3. Seed Stations
  console.log('Seeding stations...');
  const stationsData = [
    { id: 'JSM-SJO-01', name: 'JSM Paseo Colón', location: '9.9333, -84.0833' },
    { id: 'JSM-SJO-02', name: 'JSM San Pedro', location: '9.9325, -84.0507' },
    { id: 'JSM-ALA-01', name: 'JSM Alajuela Centro', location: '10.0167, -84.2167' },
    { id: 'JSM-CAR-01', name: 'JSM Cartago Basílica', location: '9.8638, -83.9160' },
    { id: 'JSM-HER-01', name: 'JSM Heredia UNA', location: '10.0023, -84.1189' },
    { id: 'JSM-GUA-01', name: 'JSM Liberia', location: '10.6333, -85.4333' },
    { id: 'JSM-PUN-01', name: 'JSM Puntarenas Paseo', location: '9.9766, -84.8322' },
  ];
  for (const data of stationsData) {
    await prisma.station.upsert({ where: { id: data.id }, update: {}, create: data });
  }

  // 4. Seed Users and Redemptions
  console.log('Seeding users and simulating redemptions...');
  for (let i = 0; i < 100; i++) {
    const user = await prisma.user.create({
      data: {
        phone: `+5068888${String(i).padStart(4, '0')}`,
        points: 0,
      },
    });

    // Simulate redemptions for the first 20 users
    if (i < 20) {
      const pointsToCredit = 25;
      const redemption = await prisma.redemption.create({
        data: {
          userId: user.id,
          stationId: stationsData[i % stationsData.length].id,
          points_credited: pointsToCredit,
        },
      });
      await prisma.adImpression.create({
        data: {
          userId: user.id,
          campaignId: (i % 2) + 1, // Alternate between campaign 1 and 2
          redemptionId: redemption.id,
        },
      });
      await prisma.user.update({
        where: { id: user.id },
        data: { points: { increment: pointsToCredit } },
      });
    }
  }

  console.log(`Seeding finished.`);
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });