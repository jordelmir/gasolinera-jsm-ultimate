import React, { useState } from 'react';
import { View, Text, TextInput, StyleSheet } from 'react-native';
import { Spinner } from '../components/Spinner'; // Import the new Spinner component
import { Button } from '../components/Button'; // Import the custom Button component
import { requestOtp, verifyOtp } from '../api/apiClient';
import { useUserStore } from '../store/userStore';
import Toast from 'react-native-toast-message';

// La navegación ahora se manejaría en un navigator raíz que observa el estado del userStore
const LoginScreen = () => {
  const [phone, setPhone] = useState('');
  const [otp, setOtp] = useState('');
  const [otpRequested, setOtpRequested] = useState(false);
  const [loading, setLoading] = useState(false);
  const setTokens = useUserStore((state) => state.setTokens);

  const handleRequestOtp = async () => {
    if (!phone) {
      Toast.show({
        type: 'error',
        text1: 'Entrada Inválida',
        text2: 'Por favor, introduce un número de teléfono.',
      });
      return;
    }
    setLoading(true);
    try {
      await requestOtp(phone);
      setOtpRequested(true);
      Toast.show({
        type: 'success',
        text1: 'OTP Enviado',
        text2: `Se ha enviado un código a ${phone}.`,
      });
    } catch (error: any) {
      Toast.show({
        type: 'error',
        text1: 'Error',
        text2: error.message,
      });
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async () => {
    if (!otp) {
      Toast.show({
        type: 'error',
        text1: 'Entrada Inválida',
        text2: 'Por favor, introduce el código OTP.',
      });
      return;
    }
    setLoading(true);
    try {
      const { accessToken } = await verifyOtp(phone, otp);
      setTokens(accessToken, null); // Actualiza el estado global, lo que debería disparar la navegación
      Toast.show({
        type: 'success',
        text1: 'Éxito',
        text2: 'Sesión iniciada correctamente!',
      });
    } catch (error: any) {
      Toast.show({
        type: 'error',
        text1: 'Error de Verificación',
        text2: error.message,
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Bienvenido</Text>
      <Text style={styles.subtitle}>Ingresa tu teléfono para continuar</Text>
      
      {!otpRequested ? (
        <>
          <TextInput
            style={styles.input}
            placeholder="Número de Teléfono"
            keyboardType="phone-pad"
            value={phone}
            onChangeText={setPhone}
            editable={!loading}
          />
          {loading ? <Spinner /> : <Button title="Enviar Código" onPress={handleRequestOtp} loading={loading} />}
        </>
      ) : (
        <>
          <TextInput
            style={styles.input}
            placeholder="Código de 6 dígitos"
            keyboardType="number-pad"
            value={otp}
            onChangeText={setOtp}
            editable={!loading}
          />
          {loading ? <Spinner /> : <Button title="Verificar e Iniciar Sesión" onPress={handleVerifyOtp} loading={loading} />}
        </>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
    backgroundColor: '#F0F2F5', // Light gray background
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#333', // Darker text for contrast
    marginBottom: 10,
  },
  subtitle: {
    fontSize: 18,
    color: '#555', // Slightly lighter text
    marginBottom: 30,
    textAlign: 'center',
  },
  input: {
    width: '100%',
    padding: 15,
    borderWidth: 1,
    borderColor: '#007bff', // Blue border for focus
    borderRadius: 10, // More rounded corners
    marginBottom: 20,
    fontSize: 16,
    backgroundColor: '#fff', // White background for input
    shadowColor: '#000', // Subtle shadow
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2, // Android shadow
  },
});

export default LoginScreen;
