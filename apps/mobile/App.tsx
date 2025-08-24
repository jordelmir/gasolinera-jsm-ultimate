import React from 'react';
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
import { useUserStore } from './src/store/userStore'; // Import the Zustand store

const Stack = createNativeStackNavigator();

export default function App() {
  const accessToken = useUserStore((state) => state.accessToken);
  const setTokens = useUserStore((state) => state.setTokens);

  // In a real app, you would load the token from AsyncStorage on app startup
  // This logic should be moved inside the userStore or a dedicated hook
  // For now, we'll keep it simple and assume the store handles persistence

  return (
    <ErrorBoundary>
      <NavigationContainer>
        <Stack.Navigator screenOptions={{ headerShown: false }}>
          {accessToken ? (
            <Stack.Screen name="Home">
              {props => <HomeScreen {...props} onLogout={() => setTokens(null, null)} />}
            </Stack.Screen>
          ) : (
            <Stack.Screen name="Login">
              {props => <LoginScreen {...props} onLogin={(token, refreshToken) => setTokens(token, refreshToken)} />}
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