import React from 'react';
import { View, Text, Button, StyleSheet } from 'react-native';

interface HomeScreenProps {
  navigation: any;
  onLogout: () => void;
}

const HomeScreen: React.FC<HomeScreenProps> = ({ navigation, onLogout }) => {
  const handleLogout = () => {
    onLogout();
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>¡Bienvenido a Gasolinera JSM!</Text>
      <Text style={styles.subtitle}>Has iniciado sesión.</Text>
      
      <Button title="Escanear QR" onPress={() => navigation.navigate('Scanner')} />
      <View style={{ marginVertical: 10 }} />
      <Button title="Ver Sorteos" onPress={() => navigation.navigate('Raffles')} />
      <View style={{ marginVertical: 10 }} />
      <Button title="Cerrar Sesión" onPress={handleLogout} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  title: {
    fontSize: 24,
    marginBottom: 10,
    fontWeight: 'bold',
  },
  subtitle: {
    fontSize: 18,
    marginBottom: 20,
    color: '#666',
  },
});

export default HomeScreen;