import { redirect } from 'next/navigation';

/**
 * Esta es la página de inicio del sitio.
 * Su único propósito es redirigir al usuario al dashboard principal.
 */
export default function HomePage() {
  redirect('/dashboard');
}
