import { create } from 'zustand';
import type { SensorDataResponse } from '../types';

interface ResultsState {
  results: SensorDataResponse | null;
  setResults: (data: SensorDataResponse) => void;
}

/**
 * Zustand store for managing the results of a sensor data request
 *
 * This store handles the storage and management of the results
 * of a sensor data request, providing functions to set the results.
 */
export const useResultsStore = create<ResultsState>((set) => ({
  results: null,
  setResults: (data) => set({ results: data }),
}));
