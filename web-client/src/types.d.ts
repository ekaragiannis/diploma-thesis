export type DataType = 'cached' | 'hourly' | 'raw' | '';

export type EnergyData = {
  energy_total: number;
  period: string;
  date: string;
};

export interface SensorDataResponse {
  sensor: string;
  source: 'db_raw' | 'db_hourly' | 'redis';
  data: EnergyData[];
  execution_time: number;
}

export interface SensorsResponse {
  sensors: string[];
}

export interface RequestHistoryRecord {
  id: string;
  sensor: string;
  dataType: string;
  timestamp: number;
  execution_time: number;
}
