import { useQuery } from '@tanstack/react-query';
import api from '../services/api';
import type { SensorDataResponse } from '../types';

/**
 * Fetches the sensor data from the API
 */
export const useSensorData = (
  sensor: string,
  dataType: 'cached' | 'hourly' | 'raw'
) => {
  return useQuery({
    queryKey: ['sensor-data', sensor, dataType],
    queryFn: async (): Promise<SensorDataResponse> => {
      const response = await api.get<SensorDataResponse>(
        `/sensor-data/${sensor}/${dataType}`
      );
      return response.data;
    },
    enabled: false, // Disable automatic execution - only run when manually triggered
  });
};
