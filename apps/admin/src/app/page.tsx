// Ruta: apps/admin/src/app/page.tsx
import { redirect } from 'next/navigation';

export default function HomePage() {
  // La entrada principal a la app es ahora la página de login
  redirect('/login');
}