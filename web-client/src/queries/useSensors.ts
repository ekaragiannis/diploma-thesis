import { useQuery } from '@tanstack/react-query';
import api from '../services/api';

export interface SensorsResponse {
  sensors: string[];
}

export const useSensors = () => {
  return useQuery({
    queryKey: ['sensors'],
    queryFn: async (): Promise<SensorsResponse> => {
      const response = await api.get<SensorsResponse>('/sensors');
      return response.data;
    },
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
};
