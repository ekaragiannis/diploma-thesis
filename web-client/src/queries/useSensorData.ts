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
    enabled: !!sensor && !!dataType, // Only run query when both sensor and dataType are provided
  });
};
