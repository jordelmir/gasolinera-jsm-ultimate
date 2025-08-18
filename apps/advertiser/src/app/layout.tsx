import type { Metadata } from "next";
import "./globals.css"; // Import the global CSS

export const metadata: Metadata = {
  title: "Advertiser Portal - Gasolinera JSM",
};

import Providers from "./providers";

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
