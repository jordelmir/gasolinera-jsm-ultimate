import { ApiResponse, Product } from '@gasolinera-jsm/shared';

const API_BASE_URL = 'http://localhost:8080'; // TODO: Make configurable

/**
 * Simulates fetching products from an API.
 * In a real application, this would make an actual network request.
 */
export async function fetchProducts(): Promise<ApiResponse<Product[]>> {
  try {
    // Simulate API call delay
    await new Promise(resolve => setTimeout(resolve, 1000));

    // Simulate a successful response
    const mockProducts: Product[] = [
      { id: 'prod1', name: 'Gasolina Regular', price: 1.20, unit: 'gal' },
      { id: 'prod2', name: 'Gasolina Premium', price: 1.50, unit: 'gal' },
      { id: 'prod3', name: 'Diesel', price: 1.00, unit: 'gal' },
    ];

    // Simulate a random error for demonstration
    if (Math.random() < 0.2) { // 20% chance of error
      throw new Error('Simulated network error');
    }

    return {
      success: true,
      data: mockProducts,
    };
  } catch (error: any) {
    console.error("Error fetching products:", error);
    return {
      success: false,
      data: null,
      error: {
        message: error.message || 'An unknown error occurred',
        code: 'FETCH_ERROR',
      },
    };
  }
}

export async function requestOtp(phone: string): Promise<ApiResponse<null>> {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/otp/request`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ phone }),
    });

    if (!response.ok) {
      const errorData = await response.json();
      return {
        success: false,
        data: null,
        error: {
          message: errorData.message || 'Failed to request OTP',
          code: response.status.toString(),
        },
      };
    }

    return {
      success: true,
      data: null,
    };
  } catch (error: any) {
    console.error("Error requesting OTP:", error);
    return {
      success: false,
      data: null,
      error: {
        message: error.message || 'Network error',
        code: 'NETWORK_ERROR',
      },
    };
  }
}

export async function verifyOtp(phone: string, code: string): Promise<ApiResponse<{ accessToken: string }>> {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/otp/verify`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ phone, code }),
    });

    const responseData = await response.json();

    if (!response.ok) {
      return {
        success: false,
        data: null,
        error: {
          message: responseData.message || 'Failed to verify OTP',
          code: response.status.toString(),
        },
      };
    }

    return {
      success: true,
      data: { accessToken: responseData.accessToken },
    };
  } catch (error: any) {
    console.error("Error verifying OTP:", error);
    return {
      success: false,
      data: null,
      error: {
        message: error.message || 'Network error',
        code: 'NETWORK_ERROR',
      },
    };
  }
}
