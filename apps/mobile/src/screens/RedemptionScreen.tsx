import React, { useRef, useEffect, useState } from 'react';
import { View, StyleSheet, Text, ActivityIndicator } from 'react-native';
import { Video, ResizeMode } from 'expo-av';
import { confirmAdWatched } from '../services/api';

export default function RedemptionScreen({ route, navigation }) {
  const { ad, sessionId } = route.params;
  const video = useRef(null);
  const [status, setStatus] = useState({});
  const [isAdWatched, setIsAdWatched] = useState(false);

  useEffect(() => {
    if (status.didJustFinish && !isAdWatched) {
      setIsAdWatched(true);
      confirmAdWatched(sessionId)
        .then(response => {
          alert(`¡Puntos acreditados! Nuevo saldo: ${response.balance}`);
          navigation.navigate('Home');
        })
        .catch(error => {
          alert(`Error al confirmar: ${error.message}`);
          navigation.navigate('Home');
        });
    }
  }, [status]);

  return (
    <View style={styles.container}>
      {isAdWatched ? (
        <ActivityIndicator size="large" color="#fff" />
      ) : (
        <Video
          ref={video}
          style={styles.video}
          source={{ uri: ad.creative_url }}
          useNativeControls={false} // Non-skippable
          resizeMode={ResizeMode.CONTAIN}
          isLooping={false}
          onPlaybackStatusUpdate={status => setStatus(() => status)}
          shouldPlay
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', backgroundColor: '#000' },
  video: { alignSelf: 'center', width: '100%', height: '100%' },
});
