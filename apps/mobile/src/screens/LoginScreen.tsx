import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, ActivityIndicator } from 'react-native';
import { requestOtp, verifyOtp, setAuthToken } from '../api/apiClient';
import { useUserStore } from '../store/userStore';
import Toast from 'react-native-toast-message';

// La navegación ahora se manejaría en un navigator raíz que observa el estado del userStore
const LoginScreen = () => {
  const [phone, setPhone] = useState('');
  const [otp, setOtp] = useState('');
  const [otpRequested, setOtpRequested] = useState(false);
  const [loading, setLoading] = useState(false);
  const login = useUserStore((state) => state.login);

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
      setAuthToken(accessToken); // Configura el token para futuras llamadas de API
      login(accessToken); // Actualiza el estado global, lo que debería disparar la navegación
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
          {loading ? <ActivityIndicator size="large" color="#0000ff" /> : <Button title="Enviar Código" onPress={handleRequestOtp} />}
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
          {loading ? <ActivityIndicator size="large" color="#0000ff" /> : <Button title="Verificar e Iniciar Sesión" onPress={handleVerifyOtp} />}
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
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 10,
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
    marginBottom: 30,
  },
  input: {
    width: '100%',
    padding: 15,
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 8,
    marginBottom: 20,
    fontSize: 16,
  },
});

export default LoginScreen;
