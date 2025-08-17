"use client";

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useAuthStore } from '@/store/authStore';

// Mock de una función de API
const api = {
  login: async (email, password) => {
    console.log('API Call: /auth/login', { email, password });
    if (email === "admin@gasolinera.com" && password === "password") {
      return { token: "fake-jwt-token", user: { id: "1", name: "Admin User", email: "admin@gasolinera.com" } };
    }
    throw new Error("Credenciales incorrectas.");
  },
  register: async (name, email, password) => {
    console.log('API Call: /auth/register', { name, email, password });
    if (!name || !email || !password) {
        throw new Error("Todos los campos son requeridos.");
    }
    // Simula un nuevo usuario
    return { token: "new-fake-jwt-token", user: { id: "2", name, email } };
  }
};

export default function AuthenticationPage() {
  const router = useRouter();
  const setAuth = useAuthStore((state) => state.setAuth);

  const [loginEmail, setLoginEmail] = useState('admin@gasolinera.com');
  const [loginPassword, setLoginPassword] = useState('password');
  const [registerName, setRegisterName] = useState('');
  const [registerEmail, setRegisterEmail] = useState('');
  const [registerPassword, setRegisterPassword] = useState('');
  
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState("login");

  const handleLogin = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const { token, user } = await api.login(loginEmail, loginPassword);
      setAuth(token, user);
      // En una app real, el token se guardaría en una cookie httpOnly
      // El middleware se encargará de leerla. Aquí simulamos el guardado para el store.
      document.cookie = `auth-token=${token}; path=/; max-age=3600;`; // Expira en 1 hora
      router.push('/dashboard');
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleRegister = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const { token, user } = await api.register(registerName, registerEmail, registerPassword);
      setAuth(token, user);
      document.cookie = `auth-token=${token}; path=/; max-age=3600;`;
      router.push('/dashboard');
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-100 dark:bg-gray-900">
      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-[400px]">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="login">Iniciar Sesión</TabsTrigger>
          <TabsTrigger value="register">Registrarse</TabsTrigger>
        </TabsList>

        <TabsContent value="login">
          <Card>
            <CardHeader>
              <CardTitle>Bienvenido de Nuevo</CardTitle>
              <CardDescription>Ingresa tus credenciales para acceder a tu cuenta.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="login-email">Correo Electrónico</Label>
                <Input id="login-email" type="email" placeholder="tu@correo.com" value={loginEmail} onChange={(e) => setLoginEmail(e.target.value)} disabled={isLoading} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="login-password">Contraseña</Label>
                <Input id="login-password" type="password" value={loginPassword} onChange={(e) => setLoginPassword(e.target.value)} disabled={isLoading} />
              </div>
            </CardContent>
            <CardFooter className="flex flex-col gap-4">
              {error && activeTab === 'login' && <p className="text-sm text-red-500">{error}</p>}
              <Button className="w-full" onClick={handleLogin} disabled={isLoading}>
                {isLoading ? 'Cargando...' : 'Iniciar Sesión'}
              </Button>
            </CardFooter>
          </Card>
        </TabsContent>

        <TabsContent value="register">
          <Card>
            <CardHeader>
              <CardTitle>Crear una Cuenta</CardTitle>
              <CardDescription>Es rápido y fácil. Empieza a acumular puntos hoy.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
                <div className="space-y-2">
                    <Label htmlFor="register-name">Nombre Completo</Label>
                    <Input id="register-name" placeholder="Tu Nombre" value={registerName} onChange={(e) => setRegisterName(e.target.value)} disabled={isLoading} />
                </div>
                <div className="space-y-2">
                    <Label htmlFor="register-email">Correo Electrónico</Label>
                    <Input id="register-email" type="email" placeholder="tu@correo.com" value={registerEmail} onChange={(e) => setRegisterEmail(e.target.value)} disabled={isLoading} />
                </div>
                <div className="space-y-2">
                    <Label htmlFor="register-password">Contraseña</Label>
                    <Input id="register-password" type="password" value={registerPassword} onChange={(e) => setRegisterPassword(e.target.value)} disabled={isLoading} />
                </div>
            </CardContent>
            <CardFooter className="flex flex-col gap-4">
              {error && activeTab === 'register' && <p className="text-sm text-red-500">{error}</p>}
              <Button className="w-full" onClick={handleRegister} disabled={isLoading}>
                {isLoading ? 'Creando...' : 'Crear Cuenta'}
              </Button>
            </CardFooter>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}