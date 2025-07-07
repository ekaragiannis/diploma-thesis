import { useQuery } from '@tanstack/react-query';
import api from '../services/api';

export interface SensorsResponse {
  sensors: string[];
}

/**
 * Fetches the list of available sensors from the API
 */
export const useSensors = () => {
  return useQuery({
    queryKey: ['sensors'],
    queryFn: async (): Promise<SensorsResponse> => {
      const response = await api.get<SensorsResponse>('/sensors');
      return response.data;
    },
  });
};
