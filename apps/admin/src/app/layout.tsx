import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import dynamic from "next/dynamic";
import ClientOnly from "../components/ClientOnly";

const DynamicProviders = dynamic(() => import("./providers"), { ssr: false });

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Admin Dashboard - Gasolinera JSM",
  description: "Manage stations, campaigns, and raffles",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <ClientOnly>
          <DynamicProviders>
            {children}
          </DynamicProviders>
        </ClientOnly>
      </body>
    </html>
  );
}