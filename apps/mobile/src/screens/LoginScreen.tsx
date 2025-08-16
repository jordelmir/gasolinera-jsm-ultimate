import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, Alert } from 'react-native';
import { requestOtp, verifyOtp } from '../api/apiClient';

interface LoginScreenProps {
  navigation: any;
  onLogin: (token: string) => void;
}

const LoginScreen: React.FC<LoginScreenProps> = ({ navigation, onLogin }) => {
  const [phone, setPhone] = useState('');
  const [otp, setOtp] = useState('');
  const [otpRequested, setOtpRequested] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleRequestOtp = async () => {
    if (!phone) {
      Alert.alert('Error', 'Please enter a phone number.');
      return;
    }
    setLoading(true);
    const response = await requestOtp(phone);
    setLoading(false);

    if (response.success) {
      Alert.alert('OTP Request', `OTP requested for ${phone}. Check console.`);
      setOtpRequested(true);
    } else {
      Alert.alert('Error', response.error?.message || 'Failed to request OTP.');
    }
  };

  const handleVerifyOtp = async () => {
    if (!phone || !otp) {
      Alert.alert('Error', 'Please enter phone number and OTP.');
      return;
    }
    setLoading(true);
    const response = await verifyOtp(phone, otp);
    setLoading(false);

    if (response.success && response.data?.accessToken) {
      Alert.alert('Success', 'Logged in successfully!');
      onLogin(response.data.accessToken);
    } else {
      Alert.alert('Error', response.error?.message || 'Failed to verify OTP.');
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Login</Text>
      <TextInput
        style={styles.input}
        placeholder="Phone Number"
        keyboardType="phone-pad"
        value={phone}
        onChangeText={setPhone}
        editable={!loading}
      />
      <Button title={loading ? "Loading..." : "Request OTP"} onPress={handleRequestOtp} disabled={otpRequested || loading} />

      {otpRequested && (
        <>
          <TextInput
            style={styles.input}
            placeholder="OTP Code"
            keyboardType="number-pad"
            value={otp}
            onChangeText={setOtp}
            editable={!loading}
          />
          <Button title={loading ? "Loading..." : "Verify OTP and Login"} onPress={handleVerifyOtp} disabled={loading} />
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
  },
  title: {
    fontSize: 24,
    marginBottom: 20,
  },
  input: {
    width: '100%',
    padding: 10,
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 5,
    marginBottom: 10,
  },
});

export default LoginScreen;
