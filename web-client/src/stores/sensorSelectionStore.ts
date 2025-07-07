import { create } from 'zustand';
import type { DataType } from '../types';

interface SensorSelectionState {
  selectedSensor: string;
  selectedDataType: DataType;
  setSelectedSensor: (sensor: string) => void;
  setSelectedDataType: (dataType: DataType) => void;
}

/**
 * Zustand store for managing sensor selection state
 *
 * This store handles the selection of a sensor and data type,
 * providing functions to set the selected values.
 */
export const useSensorSelectionStore = create<SensorSelectionState>((set) => ({
  selectedSensor: '',
  selectedDataType: '',
  setSelectedSensor: (sensor) => set({ selectedSensor: sensor }),
  setSelectedDataType: (dataType) => set({ selectedDataType: dataType }),
}));
