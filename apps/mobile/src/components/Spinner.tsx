import React from 'react';
import { ActivityIndicator, View, StyleSheet } from 'react-native';

interface SpinnerProps {
  size?: "small" | "large";
  color?: string;
}

export const Spinner: React.FC<SpinnerProps> = ({ size = "large", color = "#0000ff" }) => {
  return (
    <View style={styles.spinnerContainer}>
      <ActivityIndicator size={size} color={color} />
    </View>
  );
};

const styles = StyleSheet.create({
  spinnerContainer: {
    justifyContent: 'center',
    alignItems: 'center',
  },
});
