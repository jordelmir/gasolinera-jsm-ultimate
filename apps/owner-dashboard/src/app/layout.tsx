import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'Owner Dashboard - Gasolinera JSM',
  description: 'Dashboard para propietarios de gasolineras',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="es">
      <body className={inter.className}>
        <div id="root">{children}</div>
      </body>
    </html>
  );
}
