import { create } from 'zustand';
import type { DataType } from '../types';

interface SensorSelectionState {
  selectedSensor: string;
  selectedDataType: DataType;
  setSelectedSensor: (sensor: string) => void;
  setSelectedDataType: (dataType: DataType) => void;
  reset: () => void;
}

export const useSensorSelectionStore = create<SensorSelectionState>((set) => ({
  selectedSensor: '',
  selectedDataType: '',
  setSelectedSensor: (sensor) => set({ selectedSensor: sensor }),
  setSelectedDataType: (dataType) => set({ selectedDataType: dataType }),
  reset: () => set({ selectedSensor: '', selectedDataType: '' }),
}));
