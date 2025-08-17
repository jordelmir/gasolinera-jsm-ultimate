// Ruta: apps/admin/src/app/dashboard/page.tsx

export default function DashboardPage() {
  // TODO: Añadir lógica de protección de ruta.
  // Si el usuario no está autenticado (no hay token), se le debe redirigir a /login.
  // Usaremos un Hook o Context para gestionar esto en el futuro.

  return (
    <main className="flex flex-1 flex-col gap-4 p-4 md:gap-8 md:p-8">
      <div className="grid gap-4 md:grid-cols-2 md:gap-8 lg:grid-cols-4">
        <h1 className="text-2xl font-bold">Dashboard Principal</h1>
        {/* Aquí construiremos el dashboard real en la Fase 2 */}
      </div>
    </main>
  );
}