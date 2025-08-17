import React, { useState, useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import ErrorBoundary from './src/components/ErrorBoundary';
import LoginScreen from './src/screens/LoginScreen';
import HomeScreen from './src/screens/HomeScreen';
import ScannerScreen from './src/screens/ScannerScreen';
import RedemptionScreen from './src/screens/RedemptionScreen';
import AdPlayerScreen from './src/screens/AdPlayerScreen';
import RafflesScreen from './src/screens/RafflesScreen';
import Toast from 'react-native-toast-message';

const Stack = createNativeStackNavigator();

export default function App() {
  const [authToken, setAuthToken] = useState<string | null>(null);

  // In a real app, you would load the token from AsyncStorage on app startup
  useEffect(() => {
    // Simulate loading token from storage
    const storedToken = null; // Replace with actual AsyncStorage.getItem('authToken')
    if (storedToken) {
      setAuthToken(storedToken);
    }
  }, []);

  const handleLogin = (token: string) => {
    setAuthToken(token);
    // In a real app, you would save the token to AsyncStorage
    // AsyncStorage.setItem('authToken', token);
  };

  const handleLogout = () => {
    setAuthToken(null);
    // In a real app, you would remove the token from AsyncStorage
    // AsyncStorage.removeItem('authToken');
  };

  return (
    <ErrorBoundary>
      <NavigationContainer>
        <Stack.Navigator screenOptions={{ headerShown: false }}>
          {authToken ? (
            <Stack.Screen name="Home">
              {props => <HomeScreen {...props} onLogout={handleLogout} />}
            </Stack.Screen>
          ) : (
            <Stack.Screen name="Login">
              {props => <LoginScreen {...props} onLogin={handleLogin} />}
            </Stack.Screen>
          )}
          {/* Other screens that require authentication can be nested within the authenticated flow */}
          <Stack.Screen name="Scanner" component={ScannerScreen} />
          <Stack.Screen name="Redemption" component={RedemptionScreen} />
          <Stack.Screen name="AdPlayer" component={AdPlayerScreen} />
          <Stack.Screen name="Raffles" component={RafflesScreen} />
        </Stack.Navigator>
        <Toast />
      </NavigationContainer>
    </ErrorBoundary>
  );
}