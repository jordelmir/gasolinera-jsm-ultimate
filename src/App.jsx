import React from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import CustomerDashboard from './pages/CustomerDashboard';
import AttendantDashboard from './pages/AttendantDashboard';
import OwnerDashboard from './pages/OwnerDashboard';
import LoginPage from './pages/LoginPage';

function App() {
  return (
    <AuthProvider>
      <Main />
    </AuthProvider>
  );
}

const Main = () => {
    const { currentUser, userRole } = useAuth();

    if (!currentUser) {
        return <LoginPage />;
    }

    switch (userRole) {
        case 'cliente':
            return <CustomerDashboard />;
        case 'pistero':
            return <AttendantDashboard />;
        case 'dueño':
            return <OwnerDashboard />;
        default:
            return <LoginPage />;
    }
}

export default App;
