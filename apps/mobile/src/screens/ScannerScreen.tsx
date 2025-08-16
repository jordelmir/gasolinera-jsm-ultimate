import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, Button, Alert, ActivityIndicator } from 'react-native';
import { CameraView, Camera } from 'expo-camera/next';
import { useNavigation } from '@react-navigation/native';
import { initiateRedemption } from '../api/apiClient'; // Import the API client function

export default function ScannerScreen() {
  const [hasPermission, setHasPermission] = useState<boolean | null>(null);
  const [scanned, setScanned] = useState(false);
  const [loading, setLoading] = useState(false); // State for loading
  const navigation = useNavigation();

  useEffect(() => {
    const getCameraPermissions = async () => {
      const { status } = await Camera.requestCameraPermissionsAsync();
      setHasPermission(status === 'granted');
    };

    getCameraPermissions();
  }, []);

  const handleBarCodeScanned = async ({ type, data }: { type: string; data: string }) => {
    setScanned(true);
    setLoading(true); // Start loading

    try {
      // Call API to initiate redemption
      const response = await initiateRedemption(data, "mock_user_id"); // Assuming userId is passed or retrieved
      
      if (response.success && response.data) {
        Alert.alert('Escaneo Exitoso', 'Redirigiendo al anuncio...');
        navigation.navigate('AdPlayer', {
          adUrl: response.data.adUrl,
          redemptionId: response.data.redemptionId,
        });
      } else {
        Alert.alert(
          'Error de Canje',
          response.error?.message || 'No se pudo procesar el código QR. Por favor, inténtalo de nuevo.'
        );
        setScanned(false); // Allow rescanning on error
      }
    } catch (error: any) {
      Alert.alert(
        'Error de Red',
        error.message || 'No se pudo conectar con el servidor. Por favor, verifica tu conexión.'
      );
      setScanned(false); // Allow rescanning on network error
    } finally {
      setLoading(false); // End loading
    }
  };

  if (hasPermission === null) {
    return <Text>Solicitando permiso de cámara...</Text>;
  }
  if (hasPermission === false) {
    return <Text>Sin acceso a la cámara. Por favor, habilita el permiso en los ajustes.</Text>;
  }

  return (
    <View style={styles.container}>
      {loading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#FFFFFF" />
          <Text style={styles.loadingText}>Validando QR...</Text>
        </View>
      ) : (
        <CameraView
          onBarcodeScanned={scanned ? undefined : handleBarCodeScanned}
          barcodeScannerSettings={{
            barcodeTypes: ['qr'],
          }}
          style={StyleSheet.absoluteFillObject}
        />
      )}
      
      {scanned && !loading && (
        <Button title={'Escanear de Nuevo'} onPress={() => setScanned(false)} />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'black',
  },
  loadingContainer: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    color: 'white',
    marginTop: 10,
    fontSize: 16,
  },
});
