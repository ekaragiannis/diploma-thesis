import { useQuery } from '@tanstack/react-query';
import api from '../services/api';
import type { SensorDataResponse } from '../types';

/**
 * Maps frontend data types to backend source parameters
 */
const mapDataTypeToSource = (dataType: 'cached' | 'hourly' | 'raw'): string => {
  switch (dataType) {
    case 'cached':
      return 'redis';
    case 'hourly':
      return 'db_hourly';
    case 'raw':
      return 'db_raw';
    default:
      return 'redis';
  }
};

/**
 * Fetches the sensor data from the API using the unified endpoint
 */
export const useSensorData = (
  sensor: string,
  dataType: 'cached' | 'hourly' | 'raw'
) => {
  return useQuery({
    queryKey: ['sensor-data', sensor, dataType],
    queryFn: async (): Promise<SensorDataResponse> => {
      const source = mapDataTypeToSource(dataType);
      const response = await api.get<SensorDataResponse>(
        `/sensor-data/${sensor}?source=${source}`
      );
      return response.data;
    },
    enabled: false, // Disable automatic execution - only run when manually triggered
  });
};
