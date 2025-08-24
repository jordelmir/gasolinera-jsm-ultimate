import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, Button, ActivityIndicator } from 'react-native';
import { CameraView, Camera } from 'expo-camera/next';
import { useNavigation } from '@react-navigation/native';
import { redeemQrCode } from '../api/apiClient';
import Toast from 'react-native-toast-message';

export default function ScannerScreen() {
  const [hasPermission, setHasPermission] = useState<boolean | null>(null);
  const [scanned, setScanned] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigation = useNavigation<any>();

  useEffect(() => {
    const getCameraPermissions = async () => {
      const { status } = await Camera.requestCameraPermissionsAsync();
      setHasPermission(status === 'granted');
    };

    getCameraPermissions();
  }, []);

  const handleBarCodeScanned = async ({ data }: { data: string }) => {
    setScanned(true);
    setLoading(true);
    try {
      const { adUrl, redemptionId } = await redeemQrCode(data);
      navigation.navigate('AdPlayer', { adUrl, redemptionId });
    } catch (error: any) {
      Toast.show({
        type: 'error',
        text1: 'Error de Canje',
        text2: error.message,
      });
      setScanned(false); // Allow rescanning on error
    } finally {
      setLoading(false);
    }
  };

  if (hasPermission === null) {
    return <View style={styles.permissionContainer}><Text>Solicitando permiso de cámara...</Text></View>;
  }
  if (hasPermission === false) {
    return <View style={styles.permissionContainer}><Text>Sin acceso a la cámara.</Text></View>;
  }

  return (
    <View style={styles.container}>
      <CameraView
        onBarcodeScanned={scanned || loading ? undefined : handleBarCodeScanned}
        barcodeScannerSettings={{
          barcodeTypes: ['qr'],
        }}
        style={StyleSheet.absoluteFillObject}
      />
      {loading && (
        <View style={styles.loadingOverlay}>
          <ActivityIndicator size="large" color="#FFFFFF" />
          <Text style={styles.loadingText}>Validando QR...</Text>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  permissionContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingOverlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0,0,0,0.6)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    color: 'white',
    marginTop: 10,
    fontSize: 16,
  },
});
