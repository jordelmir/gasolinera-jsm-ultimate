import React from 'react';
import { View, Text, Button, StyleSheet } from 'react-native';

interface HomeScreenProps {
  navigation: any;
  onLogout: () => void;
}

const HomeScreen: React.FC<HomeScreenProps> = ({ navigation }) => {
  const handleLogout = () => {
    onLogout();
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Welcome to Gasolinera JSM!</Text>
      <Text style={styles.subtitle}>You are logged in.</Text>
      <Button title="Logout" onPress={handleLogout} />
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
  },
  subtitle: {
    fontSize: 18,
    marginBottom: 20,
  },
});

export default HomeScreen;