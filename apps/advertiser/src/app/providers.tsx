"use client";

import { useEffect } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useAuthStore } from "@/store/authStore"; // Assuming this store exists
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

export default function Providers({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const token = useAuthStore((state) => state.token);

  useEffect(() => {
    // List of routes that require authentication
    const protectedRoutes = ['/dashboard']; // Adjust protected routes as needed for advertiser app

    // Check if the current path is a protected route
    const isProtectedRoute = protectedRoutes.some(route => pathname.startsWith(route));

    // If on a protected route and no token, redirect to login
    if (isProtectedRoute && !token) {
      router.push('/login');
    }
    // If on login page and already authenticated, redirect to dashboard
    else if (pathname === '/login' && token) {
      router.push('/dashboard');
    }
  }, [token, pathname, router]);

  return (
    <>
      {children}
      <ToastContainer position="bottom-right" />
    </>
  );
}
