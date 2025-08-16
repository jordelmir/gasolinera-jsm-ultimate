import { PrismaClient } from '@prisma/client'; // Assuming Prisma for seed script

const prisma = new PrismaClient();

async function main() {
  console.log(`Start seeding ...`);

  // Seed Advertisers
  const advertiser1 = await prisma.advertiser.create({
    data: {
      name: 'Coca-Cola',
      contact_info: 'contact@cocacola.com',
    },
  });

  // Seed Stations
  const station1 = await prisma.station.create({
    data: {
        id: 'JSM-GUA-01',
        name: 'JSM Guadalupe',
        location: '9.9449, -84.0515' // Approx. coordinates
    }
  });
  const station2 = await prisma.station.create({
    data: {
        id: 'JSM-CUR-01',
        name: 'JSM Curridabat',
        location: '9.9199, -84.0402'
    }
  });

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
