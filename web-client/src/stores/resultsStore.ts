import { create } from 'zustand';
import type { SensorDataResponse } from '../types';

interface ResultsState {
  results: SensorDataResponse | null;
  setResults: (data: SensorDataResponse) => void;
  resetResults: () => void;
}

export const useResultsStore = create<ResultsState>((set) => ({
  results: null,
  setResults: (data) => set({ results: data }),
  resetResults: () => set({ results: null }),
}));
