export type DataType = 'cached' | 'hourly' | 'raw' | '';

export interface SensorDataResponse {
  sensor: string;
  data: Record<string, number>;
  execution_time: number;
}

export interface SensorsResponse {
  sensors: string[];
}
