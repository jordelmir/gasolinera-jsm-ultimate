import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, ActivityIndicator } from 'react-native';
import { getRaffles, getRaffleWinner, Raffle, RaffleWinner } from '../api/apiClient';
import Toast from 'react-native-toast-message';

export default function RafflesScreen() {
  const [raffles, setRaffles] = useState<Raffle[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchRaffles = async () => {
      try {
        setIsLoading(true);
        const data = await getRaffles();
        setRaffles(data);
      } catch (err: any) {
        setError(err.message);
        Toast.show({
          type: 'error',
          text1: 'Error al cargar sorteos',
          text2: err.message,
        });
      } finally {
        setIsLoading(false);
      }
    };
    fetchRaffles();
  }, []);

  const renderRaffleItem = ({ item }: { item: Raffle }) => (
    <View style={styles.raffleCard}>
      <Text style={styles.rafflePeriod}>Período: {item.period}</Text>
      <Text style={styles.raffleStatus}>Estado: {item.status}</Text>
      {item.status === 'DRAWN' && item.winnerEntryId && (
        <Text style={styles.raffleWinner}>Ganador: {item.winnerEntryId}</Text>
      )}
      {item.status === 'DRAWN' && !item.winnerEntryId && (
        <Text style={styles.raffleWinner}>Ganador: Pendiente/No encontrado</Text>
      )}
      {item.merkleRoot && (
        <Text style={styles.merkleRoot}>Merkle Root: {item.merkleRoot.substring(0, 10)}...</Text>
      )}
      {/* TODO: Add button to view proof of inclusion */}
    </View>
  );

  if (isLoading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#0000ff" />
        <Text>Cargando sorteos...</Text>
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.errorContainer}>
        <Text style={styles.errorText}>Error: {error}</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Sorteos de Puntos G</Text>
      {raffles.length === 0 ? (
        <Text>No hay sorteos disponibles en este momento.</Text>
      ) : (
        <FlatList
          data={raffles}
          keyExtractor={(item) => item.id.toString()}
          renderItem={renderRaffleItem}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
  },
  raffleCard: {
    backgroundColor: '#fff',
    padding: 15,
    borderRadius: 8,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.2,
    shadowRadius: 1.41,
    elevation: 2,
  },
  rafflePeriod: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 5,
  },
  raffleStatus: {
    fontSize: 16,
    color: '#555',
    marginBottom: 5,
  },
  raffleWinner: {
    fontSize: 16,
    color: '#28a745',
    fontWeight: 'bold',
  },
  merkleRoot: {
    fontSize: 12,
    color: '#888',
    marginTop: 5,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  errorText: {
    color: 'red',
    fontSize: 16,
    textAlign: 'center',
  },
});