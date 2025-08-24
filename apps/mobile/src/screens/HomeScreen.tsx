import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Button } from '../components/Button'; // Import the custom Button component
import { useUserStore } from '../store/userStore';

interface HomeScreenProps {
  navigation: any;
}

const HomeScreen: React.FC<HomeScreenProps> = ({ navigation }) => {
  const setTokens = useUserStore((state) => state.setTokens);

  const handleLogout = () => {
    setTokens(null, null);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>¡Bienvenido a Gasolinera JSM!</Text>
      <Text style={styles.subtitle}>Has iniciado sesión.</Text>
      
      <View style={styles.buttonContainer}>
        <Button title="Escanear QR" onPress={() => navigation.navigate('Scanner')} />
      </View>
      <View style={styles.buttonContainer}>
        <Button title="Ver Sorteos" onPress={() => navigation.navigate('Raffles')} />
      </View>
      <View style={styles.buttonContainer}>
        <Button title="Cerrar Sesión" onPress={handleLogout} />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
    backgroundColor: '#F0F2F5',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#333',
  },
  subtitle: {
    fontSize: 18,
    marginBottom: 30,
    color: '#555',
    textAlign: 'center',
  },
  buttonContainer: {
    width: '80%',
    marginBottom: 15,
  },
});

export default HomeScreen;