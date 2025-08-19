import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext.js';
import { db } from './firebase/config';
import { doc, getDoc } from 'firebase/firestore';
import LoginPage from './pages/LoginPage';
import CustomerDashboard from './pages/CustomerDashboard';
import AttendantDashboard from './pages/AttendantDashboard';
import OwnerDashboard from './pages/OwnerDashboard';

const AppContent = () => {
  const { currentUser } = useAuth();

  // Basic role-based redirection (you'll need to implement actual role checking)
  const [userRole, setUserRole] = useState(null);
  const [loadingRole, setLoadingRole] = useState(true);

  useEffect(() => {
    const fetchUserRole = async () => {
      if (currentUser) {
        try {
          const docRef = doc(db, "users", currentUser.uid);
          const docSnap = await getDoc(docRef);
          if (docSnap.exists()) {
            setUserRole(docSnap.data().role);
          } else {
            // If user document doesn't exist, default to customer or handle as unassigned
            setUserRole("customer"); 
          }
        } catch (error) {
          console.error("Error fetching user role:", error);
          setUserRole("customer"); // Default in case of error
        } finally {
          setLoadingRole(false);
        }
      } else {
        setUserRole(null);
        setLoadingRole(false);
      }
    };

    fetchUserRole();
  }, [currentUser]);

  const getDashboardComponent = () => {
    if (loadingRole) {
      return <div>Loading...</div>; // Or a spinner
    }

    if (!currentUser) {
      return <Navigate to="/login" />;
    }

    switch (userRole) {
      case "customer":
        return <CustomerDashboard />;
      case "attendant":
        return <AttendantDashboard />;
      case "owner":
        return <OwnerDashboard />;
      default:
        return <Navigate to="/login" />;
    }
  };

  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={getDashboardComponent()} />
      {/* Add more routes as needed */}
    </Routes>
  );
};

const App = () => {
  return (
    <Router>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </Router>
  );
};

export default App;