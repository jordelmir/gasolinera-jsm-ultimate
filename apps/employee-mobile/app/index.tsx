import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Alert,
  Dimensions,
  Modal,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import QRCode from 'react-native-qrcode-svg';
import { useEmployeeStore } from './store/employeeStore';

const { width } = Dimensions.get('window');

export default function EmployeeScreen() {
  const [amount, setAmount] = useState(1); // Múltiplos de 5000
  const [showQR, setShowQR] = useState(false);
  const [currentQR, setCurrentQR] = useState<any>(null);
  const [isGenerating, setIsGenerating] = useState(false);

  const { employee, generateQRCoupon, todayStats, refreshStats } =
    useEmployeeStore();

  useEffect(() => {
    if (employee?.id) {
      refreshStats(employee.id);
    }
  }, [employee?.id]);

  const handleIncrement = () => {
    if (amount < 20) {
      // Máximo 20 múltiplos (₡100,000)
      setAmount(amount + 1);
    }
  };

  const handleDecrement = () => {
    if (amount > 1) {
      setAmount(amount - 1);
    }
  };

  const handleGenerateQR = async () => {
    if (!employee?.id || !employee?.stationId) {
      Alert.alert('Error', 'Información de empleado no disponible');
      return;
    }

    setIsGenerating(true);
    try {
      const qrData = await generateQRCoupon({
        stationId: employee.stationId,
        employeeId: employee.id,
        amount: amount,
      });

      setCurrentQR(qrData);
      setShowQR(true);

      // Refresh stats after generating QR
      refreshStats(employee.id);
    } catch (error) {
      Alert.alert('Error', 'No se pudo generar el código QR');
    } finally {
      setIsGenerating(false);
    }
  };

  const handleCloseQR = () => {
    setShowQR(false);
    setCurrentQR(null);
    setAmount(1); // Reset amount
  };

  const formatCurrency = (amount: number) => {
    return `₡${(amount * 5000).toLocaleString()}`;
  };

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <View style={styles.employeeInfo}>
          <Text style={styles.employeeName}>
            {employee?.name || 'Empleado'}
          </Text>
          <Text style={styles.stationName}>
            {employee?.stationName || 'Estación'}
          </Text>
        </View>
        <View style={styles.statsContainer}>
          <Text style={styles.statsLabel}>Hoy</Text>
          <Text style={styles.statsValue}>
            {todayStats?.totalCoupons || 0} QRs
          </Text>
        </View>
      </View>

      {/* Main Content */}
      <View style={styles.mainContent}>
        <Text style={styles.title}>Generar Código QR</Text>
        <Text style={styles.subtitle}>
          Selecciona el monto de la compra del cliente
        </Text>

        {/* Amount Selector */}
        <View style={styles.amountContainer}>
          <TouchableOpacity
            style={[
              styles.amountButton,
              amount <= 1 && styles.amountButtonDisabled,
            ]}
            onPress={handleDecrement}
            disabled={amount <= 1}
          >
            <Ionicons
              name="remove"
              size={32}
              color={amount <= 1 ? '#C7C7CC' : '#007AFF'}
            />
          </TouchableOpacity>

          <View style={styles.amountDisplay}>
            <Text style={styles.amountValue}>{amount}</Text>
            <Text style={styles.amountLabel}>múltiplos de ₡5,000</Text>
            <Text style={styles.totalAmount}>{formatCurrency(amount)}</Text>
            <Text style={styles.ticketsInfo}>
              = {amount} ticket{amount > 1 ? 's' : ''}
            </Text>
          </View>

          <TouchableOpacity
            style={[
              styles.amountButton,
              amount >= 20 && styles.amountButtonDisabled,
            ]}
            onPress={handleIncrement}
            disabled={amount >= 20}
          >
            <Ionicons
              name="add"
              size={32}
              color={amount >= 20 ? '#C7C7CC' : '#007AFF'}
            />
          </TouchableOpacity>
        </View>

        {/* Generate Button */}
        <TouchableOpacity
          style={[
            styles.generateButton,
            isGenerating && styles.generateButtonDisabled,
          ]}
          onPress={handleGenerateQR}
          disabled={isGenerating}
        >
          <Ionicons name="qr-code-outline" size={24} color="#FFFFFF" />
          <Text style={styles.generateButtonText}>
            {isGenerating ? 'Generando...' : 'Generar QR'}
          </Text>
        </TouchableOpacity>

        {/* Quick Amount Buttons */}
        <View style={styles.quickAmountContainer}>
          <Text style={styles.quickAmountLabel}>Montos frecuentes:</Text>
          <View style={styles.quickAmountButtons}>
            {[1, 2, 4, 6, 10].map((quickAmount) => (
              <TouchableOpacity
                key={quickAmount}
                style={[
                  styles.quickAmountButton,
                  amount === quickAmount && styles.quickAmountButtonActive,
                ]}
                onPress={() => setAmount(quickAmount)}
              >
                <Text
                  style={[
                    styles.quickAmountButtonText,
                    amount === quickAmount &&
                      styles.quickAmountButtonTextActive,
                  ]}
                >
                  {formatCurrency(quickAmount)}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        {/* Daily Stats */}
        <View style={styles.dailyStatsCard}>
          <Text style={styles.dailyStatsTitle}>Estadísticas de Hoy</Text>
          <View style={styles.dailyStatsRow}>
            <View style={styles.dailyStatItem}>
              <Text style={styles.dailyStatValue}>
                {todayStats?.totalCoupons || 0}
              </Text>
              <Text style={styles.dailyStatLabel}>QRs Generados</Text>
            </View>
            <View style={styles.dailyStatItem}>
              <Text style={styles.dailyStatValue}>
                {todayStats?.scannedCoupons || 0}
              </Text>
              <Text style={styles.dailyStatLabel}>Escaneados</Text>
            </View>
            <View style={styles.dailyStatItem}>
              <Text style={styles.dailyStatValue}>
                {todayStats?.conversionRate || 0}%
              </Text>
              <Text style={styles.dailyStatLabel}>Conversión</Text>
            </View>
          </View>
        </View>
      </View>

      {/* QR Modal */}
      <Modal
        visible={showQR}
        animationType="slide"
        presentationStyle="pageSheet"
      >
        <View style={styles.qrModalContainer}>
          <View style={styles.qrModalHeader}>
            <Text style={styles.qrModalTitle}>Código QR Generado</Text>
            <TouchableOpacity onPress={handleCloseQR}>
              <Ionicons name="close" size={24} color="#8E8E93" />
            </TouchableOpacity>
          </View>

          <View style={styles.qrModalContent}>
            <View style={styles.qrContainer}>
              {currentQR && (
                <QRCode
                  value={currentQR.qrCode}
                  size={250}
                  backgroundColor="white"
                  color="black"
                />
              )}
            </View>

            <View style={styles.qrInfo}>
              <Text style={styles.qrInfoTitle}>Información del Cupón</Text>
              <View style={styles.qrInfoRow}>
                <Text style={styles.qrInfoLabel}>Token:</Text>
                <Text style={styles.qrInfoValue}>{currentQR?.token}</Text>
              </View>
              <View style={styles.qrInfoRow}>
                <Text style={styles.qrInfoLabel}>Monto:</Text>
                <Text style={styles.qrInfoValue}>
                  {currentQR && formatCurrency(currentQR.baseTickets)}
                </Text>
              </View>
              <View style={styles.qrInfoRow}>
                <Text style={styles.qrInfoLabel}>Tickets Base:</Text>
                <Text style={styles.qrInfoValue}>{currentQR?.baseTickets}</Text>
              </View>
              <View style={styles.qrInfoRow}>
                <Text style={styles.qrInfoLabel}>Expira:</Text>
                <Text style={styles.qrInfoValue}>
                  {currentQR?.expiresAt
                    ? new Date(currentQR.expiresAt).toLocaleString('es-CR')
                    : 'N/A'}
                </Text>
              </View>
            </View>

            <Text style={styles.qrInstructions}>
              Muestra este código al cliente para que lo escanee con su app
            </Text>

            <TouchableOpacity
              style={styles.closeQRButton}
              onPress={handleCloseQR}
            >
              <Text style={styles.closeQRButtonText}>Listo</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
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
    paddingTop: 60,
    paddingHorizontal: 20,
    paddingBottom: 20,
    backgroundColor: '#FFFFFF',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  employeeInfo: {
    flex: 1,
  },
  employeeName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1D1D1F',
    marginBottom: 2,
  },
  stationName: {
    fontSize: 14,
    color: '#8E8E93',
  },
  statsContainer: {
    alignItems: 'center',
  },
  statsLabel: {
    fontSize: 12,
    color: '#8E8E93',
    marginBottom: 2,
  },
  statsValue: {
    fontSize: 16,
    fontWeight: '600',
    color: '#007AFF',
  },
  mainContent: {
    flex: 1,
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1D1D1F',
    textAlign: 'center',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#8E8E93',
    textAlign: 'center',
    marginBottom: 40,
  },
  amountContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 40,
  },
  amountButton: {
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: '#FFFFFF',
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  amountButtonDisabled: {
    backgroundColor: '#F2F2F7',
  },
  amountDisplay: {
    alignItems: 'center',
    marginHorizontal: 40,
    minWidth: 150,
  },
  amountValue: {
    fontSize: 48,
    fontWeight: 'bold',
    color: '#007AFF',
    marginBottom: 4,
  },
  amountLabel: {
    fontSize: 14,
    color: '#8E8E93',
    marginBottom: 8,
  },
  totalAmount: {
    fontSize: 24,
    fontWeight: '600',
    color: '#1D1D1F',
    marginBottom: 4,
  },
  ticketsInfo: {
    fontSize: 16,
    color: '#FF6B35',
    fontWeight: '500',
  },
  generateButton: {
    backgroundColor: '#007AFF',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 16,
    borderRadius: 12,
    marginBottom: 30,
    shadowColor: '#007AFF',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 6,
  },
  generateButtonDisabled: {
    backgroundColor: '#C7C7CC',
    shadowOpacity: 0,
    elevation: 0,
  },
  generateButtonText: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: '600',
    marginLeft: 8,
  },
  quickAmountContainer: {
    marginBottom: 30,
  },
  quickAmountLabel: {
    fontSize: 16,
    fontWeight: '500',
    color: '#1D1D1F',
    marginBottom: 12,
  },
  quickAmountButtons: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
  },
  quickAmountButton: {
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8,
    marginBottom: 8,
    minWidth: '18%',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  quickAmountButtonActive: {
    backgroundColor: '#007AFF',
  },
  quickAmountButtonText: {
    fontSize: 12,
    color: '#1D1D1F',
    fontWeight: '500',
  },
  quickAmountButtonTextActive: {
    color: '#FFFFFF',
  },
  dailyStatsCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  dailyStatsTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#1D1D1F',
    marginBottom: 16,
    textAlign: 'center',
  },
  dailyStatsRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  dailyStatItem: {
    alignItems: 'center',
  },
  dailyStatValue: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#007AFF',
    marginBottom: 4,
  },
  dailyStatLabel: {
    fontSize: 12,
    color: '#8E8E93',
    textAlign: 'center',
  },
  // QR Modal Styles
  qrModalContainer: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  qrModalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 60,
    paddingHorizontal: 20,
    paddingBottom: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#E5E5EA',
  },
  qrModalTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#1D1D1F',
  },
  qrModalContent: {
    flex: 1,
    padding: 20,
    alignItems: 'center',
  },
  qrContainer: {
    backgroundColor: '#FFFFFF',
    padding: 20,
    borderRadius: 16,
    marginBottom: 30,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 4,
  },
  qrInfo: {
    width: '100%',
    backgroundColor: '#F8F9FA',
    borderRadius: 12,
    padding: 16,
    marginBottom: 20,
  },
  qrInfoTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#1D1D1F',
    marginBottom: 12,
  },
  qrInfoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  qrInfoLabel: {
    fontSize: 14,
    color: '#8E8E93',
  },
  qrInfoValue: {
    fontSize: 14,
    color: '#1D1D1F',
    fontWeight: '500',
  },
  qrInstructions: {
    fontSize: 16,
    color: '#8E8E93',
    textAlign: 'center',
    marginBottom: 30,
    paddingHorizontal: 20,
  },
  closeQRButton: {
    backgroundColor: '#007AFF',
    paddingHorizontal: 40,
    paddingVertical: 16,
    borderRadius: 12,
  },
  closeQRButtonText: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: '600',
  },
});
