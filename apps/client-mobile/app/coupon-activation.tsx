import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Alert,
  ScrollView,
  Dimensions,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { router, useLocalSearchParams } from 'expo-router';
import { Video, ResizeMode } from 'expo-av';
import { useCouponStore } from './store/couponStore';
import { useUserStore } from './store/userStore';

const { width } = Dimensions.get('window');

export default function CouponActivationScreen() {
  const { couponId } = useLocalSearchParams<{ couponId: string }>();
  const [currentStep, setCurrentStep] = useState(0);
  const [isWatchingAd, setIsWatchingAd] = useState(false);
  const [adProgress, setAdProgress] = useState(0);
  const [sequence, setSequence] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(false);

  const { activateCoupon, completeAdStep, getAdSequence } = useCouponStore();
  const { user } = useUserStore();

  useEffect(() => {
    if (couponId && user?.id) {
      loadSequence();
    }
  }, [couponId, user?.id]);

  const loadSequence = async () => {
    try {
      if (!user?.id || !couponId) return;

      const sequenceData = await getAdSequence(user.id, couponId);
      setSequence(sequenceData);
      setCurrentStep(sequenceData?.currentStep || 1);
    } catch (error) {
      console.error('Error loading sequence:', error);
    }
  };

  const handleActivateCoupon = async () => {
    if (!user?.id || !couponId) return;

    setIsLoading(true);
    try {
      const result = await activateCoupon(couponId, user.id);
      await loadSequence();

      Alert.alert(
        '¡Cupón Activado!',
        'Ahora puedes ver anuncios para ganar más tickets',
        [{ text: 'Continuar' }]
      );
    } catch (error) {
      Alert.alert('Error', 'No se pudo activar el cupón');
    } finally {
      setIsLoading(false);
    }
  };

  const handleWatchAd = () => {
    if (!sequence?.nextAd) return;

    setIsWatchingAd(true);
    setAdProgress(0);

    // Simular progreso del anuncio
    const duration = sequence.nextAdDuration * 1000; // Convertir a ms
    const interval = 100; // Actualizar cada 100ms
    const increment = (interval / duration) * 100;

    const progressInterval = setInterval(() => {
      setAdProgress((prev) => {
        const newProgress = prev + increment;
        if (newProgress >= 100) {
          clearInterval(progressInterval);
          handleAdCompleted();
          return 100;
        }
        return newProgress;
      });
    }, interval);
  };

  const handleAdCompleted = async () => {
    if (!sequence?.sequenceId || !user?.id) return;

    try {
      const result = await completeAdStep(sequence.sequenceId, user.id);
      setSequence(result);
      setCurrentStep(result.currentStep);
      setIsWatchingAd(false);
      setAdProgress(0);

      Alert.alert(
        '¡Anuncio Completado!',
        `Ganaste ${result.potentialTicketsFromNextAd} tickets adicionales`,
        [
          {
            text: result.canContinue ? 'Ver Otro Anuncio' : 'Finalizar',
            onPress: result.canContinue ? undefined : () => router.back(),
          },
        ]
      );
    } catch (error) {
      Alert.alert('Error', 'No se pudo completar el anuncio');
      setIsWatchingAd(false);
    }
  };

  const handleSkipAd = () => {
    Alert.alert(
      'Saltar Anuncio',
      '¿Estás seguro? No ganarás tickets adicionales.',
      [
        { text: 'Continuar Viendo', style: 'cancel' },
        {
          text: 'Saltar',
          style: 'destructive',
          onPress: () => {
            setIsWatchingAd(false);
            setAdProgress(0);
          },
        },
      ]
    );
  };

  const getDurationText = (seconds: number) => {
    if (seconds < 60) return `${seconds}s`;
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return remainingSeconds > 0
      ? `${minutes}m ${remainingSeconds}s`
      : `${minutes}m`;
  };

  if (isWatchingAd) {
    return (
      <View style={styles.adContainer}>
        <View style={styles.adHeader}>
          <Text style={styles.adTitle}>Anuncio {currentStep}</Text>
          <TouchableOpacity onPress={handleSkipAd}>
            <Text style={styles.skipButton}>Saltar</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.videoContainer}>
          <Video
            source={{
              uri: sequence?.nextAd?.videoUrl || 'https://example.com/ad.mp4',
            }}
            style={styles.video}
            useNativeControls={false}
            resizeMode={ResizeMode.CONTAIN}
            shouldPlay
            isLooping={false}
          />
        </View>

        <View style={styles.progressContainer}>
          <View style={styles.progressBar}>
            <View style={[styles.progressFill, { width: `${adProgress}%` }]} />
          </View>
          <Text style={styles.progressText}>
            {Math.round(adProgress)}% completado
          </Text>
        </View>

        <View style={styles.adInfo}>
          <Text style={styles.adInfoText}>
            Ganarás {sequence?.potentialTicketsFromNextAd || 0} tickets
            adicionales
          </Text>
        </View>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()}>
          <Ionicons name="arrow-back" size={24} color="#007AFF" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Activar Cupón</Text>
        <View style={styles.placeholder} />
      </View>

      {!sequence ? (
        <View style={styles.activationCard}>
          <Ionicons name="ticket-outline" size={64} color="#FF6B35" />
          <Text style={styles.activationTitle}>¡Cupón Listo!</Text>
          <Text style={styles.activationSubtitle}>
            Activa tu cupón para comenzar a ganar tickets
          </Text>

          <TouchableOpacity
            style={styles.activateButton}
            onPress={handleActivateCoupon}
            disabled={isLoading}
          >
            <Text style={styles.activateButtonText}>
              {isLoading ? 'Activando...' : 'Activar Cupón'}
            </Text>
          </TouchableOpacity>
        </View>
      ) : (
        <View style={styles.sequenceContainer}>
          <View style={styles.progressHeader}>
            <Text style={styles.progressTitle}>Secuencia de Anuncios</Text>
            <Text style={styles.progressSubtitle}>
              Paso {sequence.currentStep} de {sequence.maxSteps}
            </Text>
          </View>

          <View style={styles.ticketsCard}>
            <Text style={styles.ticketsLabel}>Tickets Actuales</Text>
            <Text style={styles.ticketsCount}>{sequence.currentTickets}</Text>
          </View>

          {sequence.canContinue && (
            <View style={styles.nextAdCard}>
              <View style={styles.nextAdHeader}>
                <Ionicons
                  name="play-circle-outline"
                  size={32}
                  color="#007AFF"
                />
                <View style={styles.nextAdInfo}>
                  <Text style={styles.nextAdTitle}>Próximo Anuncio</Text>
                  <Text style={styles.nextAdDuration}>
                    Duración: {getDurationText(sequence.nextAdDuration)}
                  </Text>
                </View>
              </View>

              <Text style={styles.nextAdReward}>
                Ganarás {sequence.potentialTicketsFromNextAd} tickets
                adicionales
              </Text>

              <TouchableOpacity
                style={styles.watchAdButton}
                onPress={handleWatchAd}
              >
                <Ionicons name="play" size={20} color="#FFFFFF" />
                <Text style={styles.watchAdButtonText}>Ver Anuncio</Text>
              </TouchableOpacity>
            </View>
          )}

          {sequence.isCompleted && (
            <View style={styles.completedCard}>
              <Ionicons name="checkmark-circle" size={48} color="#34C759" />
              <Text style={styles.completedTitle}>¡Secuencia Completada!</Text>
              <Text style={styles.completedSubtitle}>
                Has ganado un total de {sequence.currentTickets} tickets
              </Text>

              <TouchableOpacity
                style={styles.finishButton}
                onPress={() => router.back()}
              >
                <Text style={styles.finishButtonText}>Finalizar</Text>
              </TouchableOpacity>
            </View>
          )}

          <View style={styles.stepsIndicator}>
            {Array.from({ length: sequence.maxSteps }, (_, i) => (
              <View
                key={i}
                style={[
                  styles.stepDot,
                  i < sequence.currentStep - 1 && styles.stepCompleted,
                  i === sequence.currentStep - 1 && styles.stepCurrent,
                ]}
              />
            ))}
          </View>
        </View>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8F9FA',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingTop: 60,
    paddingHorizontal: 20,
    paddingBottom: 20,
    backgroundColor: '#FFFFFF',
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#1D1D1F',
  },
  placeholder: {
    width: 24,
  },
  activationCard: {
    backgroundColor: '#FFFFFF',
    margin: 20,
    padding: 32,
    borderRadius: 16,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 4,
  },
  activationTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1D1D1F',
    marginTop: 16,
    marginBottom: 8,
  },
  activationSubtitle: {
    fontSize: 16,
    color: '#8E8E93',
    textAlign: 'center',
    marginBottom: 24,
  },
  activateButton: {
    backgroundColor: '#007AFF',
    paddingHorizontal: 32,
    paddingVertical: 16,
    borderRadius: 12,
    minWidth: 200,
  },
  activateButtonText: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: '600',
    textAlign: 'center',
  },
  sequenceContainer: {
    padding: 20,
  },
  progressHeader: {
    alignItems: 'center',
    marginBottom: 20,
  },
  progressTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#1D1D1F',
    marginBottom: 4,
  },
  progressSubtitle: {
    fontSize: 16,
    color: '#8E8E93',
  },
  ticketsCard: {
    backgroundColor: '#FFFFFF',
    padding: 20,
    borderRadius: 12,
    alignItems: 'center',
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  ticketsLabel: {
    fontSize: 14,
    color: '#8E8E93',
    marginBottom: 4,
  },
  ticketsCount: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#FF6B35',
  },
  nextAdCard: {
    backgroundColor: '#FFFFFF',
    padding: 20,
    borderRadius: 12,
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  nextAdHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  nextAdInfo: {
    marginLeft: 12,
    flex: 1,
  },
  nextAdTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#1D1D1F',
    marginBottom: 2,
  },
  nextAdDuration: {
    fontSize: 14,
    color: '#8E8E93',
  },
  nextAdReward: {
    fontSize: 16,
    color: '#FF6B35',
    fontWeight: '500',
    marginBottom: 16,
    textAlign: 'center',
  },
  watchAdButton: {
    backgroundColor: '#007AFF',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 12,
    borderRadius: 8,
  },
  watchAdButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
    marginLeft: 8,
  },
  completedCard: {
    backgroundColor: '#FFFFFF',
    padding: 24,
    borderRadius: 12,
    alignItems: 'center',
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  completedTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#1D1D1F',
    marginTop: 12,
    marginBottom: 8,
  },
  completedSubtitle: {
    fontSize: 16,
    color: '#8E8E93',
    textAlign: 'center',
    marginBottom: 20,
  },
  finishButton: {
    backgroundColor: '#34C759',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
  },
  finishButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  },
  stepsIndicator: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 20,
  },
  stepDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#E5E5EA',
    marginHorizontal: 4,
  },
  stepCompleted: {
    backgroundColor: '#34C759',
  },
  stepCurrent: {
    backgroundColor: '#007AFF',
    width: 12,
    height: 12,
    borderRadius: 6,
  },
  // Ad viewing styles
  adContainer: {
    flex: 1,
    backgroundColor: '#000000',
  },
  adHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 60,
    paddingHorizontal: 20,
    paddingBottom: 20,
  },
  adTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  skipButton: {
    fontSize: 16,
    color: '#FFFFFF',
    opacity: 0.8,
  },
  videoContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  video: {
    width: width,
    height: width * (9 / 16), // 16:9 aspect ratio
  },
  progressContainer: {
    paddingHorizontal: 20,
    paddingVertical: 16,
  },
  progressBar: {
    height: 4,
    backgroundColor: 'rgba(255, 255, 255, 0.3)',
    borderRadius: 2,
    marginBottom: 8,
  },
  progressFill: {
    height: '100%',
    backgroundColor: '#007AFF',
    borderRadius: 2,
  },
  progressText: {
    fontSize: 14,
    color: '#FFFFFF',
    textAlign: 'center',
  },
  adInfo: {
    paddingHorizontal: 20,
    paddingBottom: 40,
  },
  adInfoText: {
    fontSize: 16,
    color: '#FFFFFF',
    textAlign: 'center',
    opacity: 0.9,
  },
});
