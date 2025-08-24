import React, { useState, useEffect, useRef } from 'react';
import { View, Text, StyleSheet, Button, ActivityIndicator } from 'react-native';
import { useNavigation, useRoute, RouteProp } from '@react-navigation/native';
import { WebView } from 'react-native-webview';
import { confirmAdWatched } from '../api/apiClient';
import Toast from 'react-native-toast-message';

const AD_DURATION_SECONDS = 15;

type AdPlayerScreenRouteProp = RouteProp<{ params: { adUrl: string; redemptionId: string } }, 'params'>;

export default function AdPlayerScreen() {
  const navigation = useNavigation<any>();
  const route = useRoute<AdPlayerScreenRouteProp>();
  const { adUrl, redemptionId } = route.params;

  const [timeLeft, setTimeLeft] = useState(AD_DURATION_SECONDS);
  const [isConfirming, setIsConfirming] = useState(false);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    intervalRef.current = setInterval(() => {
      setTimeLeft((prevTime) => prevTime - 1);
    }, 1000);

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, []);

  useEffect(() => {
    if (timeLeft <= 0) {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
      handleAdFinished();
    }
  }, [timeLeft]);

  const handleAdFinished = async () => {
    setIsConfirming(true);
    try {
      await confirmAdWatched(redemptionId);
      Toast.show({
        type: 'success',
        text1: '¡Recompensa Obtenida!',
        text2: 'Has ganado puntos por ver el anuncio.',
      });
      navigation.navigate('Home'); // Navigate back to home or a success screen
    } catch (error: any) {
      Toast.show({
        type: 'error',
        text1: 'Error de Confirmación',
        text2: error.message,
      });
      navigation.goBack(); // Go back if ad confirmation fails
    } finally {
      setIsConfirming(false);
    }
  };

  const progress = ((AD_DURATION_SECONDS - timeLeft) / AD_DURATION_SECONDS) * 100;

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.timerText}>Tu recompensa estará disponible en {timeLeft} segundos...</Text>
        <View style={styles.progressBarContainer}>
          <View style={[styles.progressBar, { width: `${progress}%` }]} />
        </View>
      </View>
      <WebView 
        source={{ uri: adUrl }}
        style={styles.webview}
        onError={(e) => Toast.show({
          type: 'error',
          text1: 'Error',
          text2: `No se pudo cargar el anuncio: ${e.nativeEvent.description}`,
        })}
      />
      {isConfirming &&
        <View style={styles.loadingOverlay}>
            <ActivityIndicator size="large" color="#FFFFFF" />
            <Text style={styles.loadingText}>Confirmando recompensa...</Text>
        </View>
      }
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000',
  },
  header: {
    padding: 15,
    backgroundColor: '#1a1a1a',
  },
  timerText: {
    color: '#fff',
    textAlign: 'center',
    marginBottom: 10,
    fontSize: 16,
  },
  progressBarContainer: {
    height: 8,
    backgroundColor: '#444',
    borderRadius: 4,
  },
  progressBar: {
    height: '100%',
    backgroundColor: '#4CAF50',
    borderRadius: 4,
  },
  webview: {
    flex: 1,
  },
  loadingOverlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0,0,0,0.7)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    color: 'white',
    marginTop: 10,
    fontSize: 16,
  },
});