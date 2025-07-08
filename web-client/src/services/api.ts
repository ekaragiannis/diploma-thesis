import axios, { type AxiosInstance, type AxiosResponse } from 'axios';
import type { InternalAxiosRequestConfig } from 'axios';

// Create axios instance with base configuration
const api: AxiosInstance = axios.create({
  baseURL: '__BASE_URL__', // Uses the base URL from the environment variable
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Log outgoing requests
    console.log(
      `🚀 Making ${config.method?.toUpperCase()} request to: ${config.url}`
    );

    // You can add auth tokens here if needed
    // const token = localStorage.getItem('authToken');
    // if (token) {
    //   config.headers = {
    //     ...config.headers,
    //     Authorization: `Bearer ${token}`,
    //   };
    // }

    return config;
  },
  (error) => {
    console.error('❌ Request error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response: AxiosResponse) => {
    // Log successful responses
    console.log(`✅ ${response.status} response from: ${response.config.url}`);
    return response;
  },
  (error) => {
    // Handle different types of errors
    if (error.response) {
      console.error(`❌ ${error.response.status} error:`, error.response.data);

      switch (error.response.status) {
        case 401:
          console.error('Unauthorized - please log in again');
          // Redirect to login or refresh token
          break;
        case 403:
          console.error('Forbidden - insufficient permissions');
          break;
        case 404:
          console.error('Resource not found');
          break;
        case 500:
          console.error('Server error - please try again later');
          break;
        default:
          console.error(`HTTP error ${error.response.status}`);
      }
    } else if (error.request) {
      console.error('❌ Network error - no response received');
    } else {
      console.error('❌ Request setup error:', error.message);
    }

    return Promise.reject(error);
  }
);

export default api;
