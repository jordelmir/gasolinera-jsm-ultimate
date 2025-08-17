"use client";

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/lib/store/authStore';
import { loginAdvertiser } from '@/lib/apiClient';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

// Basic components for demo purposes
const Card = ({ children }) => <div style={{ border: '1px solid #ccc', padding: '20px', borderRadius: '8px', width: '400px' }}>{children}</div>;
const Input = (props) => <input {...props} style={{ width: '100%', padding: '8px', marginBottom: '10px' }} />;
const Button = (props) => <button {...props} style={{ width: '100%', padding: '10px' }} />;
const Label = (props) => <label {...props} style={{ display: 'block', marginBottom: '5px' }} />;

export default function LoginPage() {
  const router = useRouter();
  const login = useAuthStore((state) => state.login);
  const [email, setEmail] = useState('anunciante@tosty.com');
  const [password, setPassword] = useState('tosty123');
  const [isLoading, setIsLoading] = useState(false);

  const handleLogin = async () => {
    setIsLoading(true);
    try {
      const { token } = await loginAdvertiser(email, password);
      login(token, null);
      toast.success("Login successful!");
      router.push('/dashboard');
    } catch (err: any) {
      toast.error(err.message || 'An unknown error occurred.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <Card>
        <h2>Advertiser Portal Login</h2>
        <Label htmlFor="email">Email</Label>
        <Input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        <Label htmlFor="password">Password</Label>
        <Input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        <Button onClick={handleLogin} disabled={isLoading}>
          {isLoading ? 'Logging in...' : 'Login'}
        </Button>
      </Card>
      <ToastContainer position="bottom-right" />
    </div>
  );
}