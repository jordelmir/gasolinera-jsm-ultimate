import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, Button, ActivityIndicator, Alert } from 'react-native';
import { useNavigation, useRoute } from '@react-navigation/native';
import { confirmAdWatched } from '../api/apiClient'; // Import the API client function
// import { Video } from 'expo-av'; // You would use a video player here

interface AdPlayerScreenRouteParams {
  adUrl: string;
  redemptionId: string;
}

export default function AdPlayerScreen() {
  const navigation = useNavigation();
  const route = useRoute();
  const { adUrl, redemptionId } = route.params as AdPlayerScreenRouteParams;

  const [adDuration, setAdDuration] = useState(10); // Simulate 10-second ad
  const [timeLeft, setTimeLeft] = useState(adDuration);
  const [progress, setProgress] = useState(0); // New state for progress
  const [loading, setLoading] = useState(true);
  const [adFinished, setAdFinished] = useState(false);
  const [confirmingAd, setConfirmingAd] = useState(false);

  useEffect(() => {
    // Simulate ad loading
    const loadAd = async () => {
      await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate network delay
      setLoading(false);
      // In a real app, you'd load the video here
    };
    loadAd();
  }, []);

  useEffect(() => {
    if (!loading && !adFinished && timeLeft > 0) {
      const timer = setTimeout(() => {
        setTimeLeft(timeLeft - 1);
        // Calculate progress based on simulated ad
        const newProgress = (adDuration - (timeLeft - 1)) / adDuration;
        setProgress(newProgress);
      }, 1000);
      return () => clearTimeout(timer);
    } else if (timeLeft === 0 && !adFinished) {
      setAdFinished(true);
      handleAdFinished();
    }
  }, [timeLeft, loading, adFinished, redemptionId, adDuration]); // Add adDuration to dependencies

  const handleAdFinished = async () => {
    setConfirmingAd(true);
    try {
      const response = await confirmAdWatched(redemptionId);
      if (response.success) {
        Alert.alert('Anuncio Terminado', `¡Puntos acreditados! Balance: ${response.data?.balance}`);
        navigation.navigate('Home'); // Navigate back to home or a success screen
      } else {
        Alert.alert('Error', response.error?.message || 'No se pudieron acreditar los puntos.');
      }
    } catch (error: any) {
      Alert.alert('Error de Red', error.message || 'No se pudo conectar con el servidor para confirmar el anuncio.');
    } finally {
      setConfirmingAd(false);
    }
  };

  const handleSkipAd = () => {
    Alert.alert('Anuncio Saltado', 'Debes ver el anuncio completo para ganar puntos.');
    navigation.goBack(); // Go back if ad is skipped
  };

  if (loading || confirmingAd) {
    return (
      <View style={styles.container}>
        <ActivityIndicator size="large" color="#FFFFFF" />
        <Text style={styles.loadingText}>{confirmingAd ? "Confirmando anuncio..." : "Cargando anuncio..."}</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Reproduciendo Anuncio</Text>
      <Text style={styles.adUrl}>URL: {adUrl}</Text>
      
      {/* Placeholder for video player */}
      <View style={styles.videoPlayerPlaceholder}>
        <Text style={styles.videoText}>Video del Anuncio Aquí</Text>
        <Text style={styles.videoText}>Tiempo restante: {timeLeft}s</Text>
        {/* Progress Bar */}
        <View style={styles.progressBarContainer}>
            <View style={[styles.progressBar, { width: `${progress * 100}%` }]} />
        </View>
        <Text style={styles.timerText}>Tu recompensa estará disponible en {timeLeft} segundos...</Text>
      </View>

      {!adFinished && (
        <Button title="Saltar Anuncio (No recomendado)" onPress={handleSkipAd} />
      )}
      {adFinished && (
        <Button title="Anuncio Terminado. Continuar" onPress={() => navigation.navigate('Home')} />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'black',
    padding: 20,
  },
  title: {
    fontSize: 24,
    color: 'white',
    marginBottom: 20,
  },
  adUrl: {
    color: 'gray',
    marginBottom: 30,
  },
  videoPlayerPlaceholder: {
    width: '100%',
    height: 200,
    backgroundColor: '#333',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 20,
  },
  videoText: {
    color: 'white',
    fontSize: 18,
  },
  loadingText: {
    color: 'white',
    marginTop: 10,
    fontSize: 16,
  },
  progressBarContainer: {
    width: '90%',
    height: 10,
    backgroundColor: '#555',
    borderRadius: 5,
    marginTop: 10,
    overflow: 'hidden',
  },
  progressBar: {
    height: '100%',
    backgroundColor: '#4CAF50', // Green color for progress
    borderRadius: 5,
  },
  timerText: {
    color: 'white',
    fontSize: 16,
    marginTop: 10,
  },
});