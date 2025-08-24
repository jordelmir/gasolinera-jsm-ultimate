import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Alert,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { useUserStore } from '../store/userStore';
import { useCouponStore } from '../store/couponStore';

export default function HomeScreen() {
  const { user } = useUserStore();
  const { totalTickets, activeCoupons, refreshUserData } = useCouponStore();

  const handleScanQR = () => {
    router.push('/scanner');
  };

  const handleViewCoupons = () => {
    router.push('/coupons');
  };

  const handleProfile = () => {
    router.push('/profile');
  };

  React.useEffect(() => {
    if (user?.id) {
      refreshUserData(user.id);
    }
  }, [user?.id]);

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <View style={styles.welcomeSection}>
          <Text style={styles.welcomeText}>
            ¡Hola, {user?.name || 'Usuario'}!
          </Text>
          <Text style={styles.subtitleText}>
            Escanea códigos QR y gana increíbles premios
          </Text>
        </View>
        <TouchableOpacity style={styles.profileButton} onPress={handleProfile}>
          <Ionicons name="person-circle-outline" size={32} color="#007AFF" />
        </TouchableOpacity>
      </View>

      {/* Tickets Counter */}
      <View style={styles.ticketsCard}>
        <View style={styles.ticketsHeader}>
          <Ionicons name="ticket-outline" size={24} color="#FF6B35" />
          <Text style={styles.ticketsTitle}>Mis Tickets</Text>
        </View>
        <Text style={styles.ticketsCount}>{totalTickets}</Text>
        <Text style={styles.ticketsSubtitle}>Tickets activos para sorteos</Text>
      </View>

      {/* Main Actions */}
      <View style={styles.actionsContainer}>
        <TouchableOpacity style={styles.scanButton} onPress={handleScanQR}>
          <View style={styles.scanButtonContent}>
            <Ionicons name="qr-code-outline" size={48} color="#FFFFFF" />
            <Text style={styles.scanButtonText}>Escanear QR</Text>
            <Text style={styles.scanButtonSubtext}>
              Escanea el código del dispensador
            </Text>
          </View>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.couponsButton}
          onPress={handleViewCoupons}
        >
          <View style={styles.actionButtonContent}>
            <Ionicons name="list-outline" size={32} color="#007AFF" />
            <Text style={styles.actionButtonText}>Mis Cupones</Text>
            <Text style={styles.actionButtonSubtext}>
              {activeCoupons.length} cupones activos
            </Text>
          </View>
        </TouchableOpacity>
      </View>

      {/* Raffle Info */}
      <View style={styles.raffleSection}>
        <Text style={styles.raffleSectionTitle}>Próximos Sorteos</Text>

        <View style={styles.raffleCard}>
          <View style={styles.raffleCardHeader}>
            <Ionicons name="calendar-outline" size={20} color="#34C759" />
            <Text style={styles.raffleCardTitle}>Sorteo Semanal</Text>
          </View>
          <Text style={styles.rafflePrize}>₡40,000</Text>
          <Text style={styles.raffleDate}>Próximo: Domingo</Text>
        </View>

        <View style={styles.raffleCard}>
          <View style={styles.raffleCardHeader}>
            <Ionicons name="car-outline" size={20} color="#FF9500" />
            <Text style={styles.raffleCardTitle}>Sorteo Anual</Text>
          </View>
          <Text style={styles.rafflePrize}>¡Un Carro!</Text>
          <Text style={styles.raffleDate}>Diciembre 2024</Text>
        </View>
      </View>

      {/* Recent Activity */}
      {activeCoupons.length > 0 && (
        <View style={styles.recentSection}>
          <Text style={styles.recentTitle}>Actividad Reciente</Text>
          {activeCoupons.slice(0, 3).map((coupon) => (
            <View key={coupon.id} style={styles.recentItem}>
              <Ionicons name="checkmark-circle" size={20} color="#34C759" />
              <View style={styles.recentItemContent}>
                <Text style={styles.recentItemText}>Cupón {coupon.token}</Text>
                <Text style={styles.recentItemSubtext}>
                  {coupon.totalTickets} tickets • {coupon.status}
                </Text>
              </View>
            </View>
          ))}
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
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 20,
    paddingTop: 60,
    backgroundColor: '#FFFFFF',
  },
  welcomeSection: {
    flex: 1,
  },
  welcomeText: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1D1D1F',
    marginBottom: 4,
  },
  subtitleText: {
    fontSize: 16,
    color: '#8E8E93',
  },
  profileButton: {
    padding: 8,
  },
  ticketsCard: {
    backgroundColor: '#FFFFFF',
    margin: 20,
    padding: 24,
    borderRadius: 16,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 4,
  },
  ticketsHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  ticketsTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#1D1D1F',
    marginLeft: 8,
  },
  ticketsCount: {
    fontSize: 48,
    fontWeight: 'bold',
    color: '#FF6B35',
    marginBottom: 4,
  },
  ticketsSubtitle: {
    fontSize: 14,
    color: '#8E8E93',
  },
  actionsContainer: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  scanButton: {
    backgroundColor: '#007AFF',
    borderRadius: 16,
    padding: 24,
    marginBottom: 16,
    shadowColor: '#007AFF',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 6,
  },
  scanButtonContent: {
    alignItems: 'center',
  },
  scanButtonText: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#FFFFFF',
    marginTop: 12,
    marginBottom: 4,
  },
  scanButtonSubtext: {
    fontSize: 14,
    color: '#FFFFFF',
    opacity: 0.8,
  },
  couponsButton: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 4,
  },
  actionButtonContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  actionButtonText: {
    fontSize: 18,
    fontWeight: '600',
    color: '#1D1D1F',
    marginLeft: 16,
    flex: 1,
  },
  actionButtonSubtext: {
    fontSize: 14,
    color: '#8E8E93',
    marginLeft: 16,
  },
  raffleSection: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  raffleSectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#1D1D1F',
    marginBottom: 16,
  },
  raffleCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  raffleCardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  raffleCardTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1D1D1F',
    marginLeft: 8,
  },
  rafflePrize: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#FF6B35',
    marginBottom: 4,
  },
  raffleDate: {
    fontSize: 14,
    color: '#8E8E93',
  },
  recentSection: {
    paddingHorizontal: 20,
    marginBottom: 40,
  },
  recentTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#1D1D1F',
    marginBottom: 16,
  },
  recentItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  recentItemContent: {
    marginLeft: 12,
    flex: 1,
  },
  recentItemText: {
    fontSize: 16,
    fontWeight: '500',
    color: '#1D1D1F',
    marginBottom: 2,
  },
  recentItemSubtext: {
    fontSize: 14,
    color: '#8E8E93',
  },
});
