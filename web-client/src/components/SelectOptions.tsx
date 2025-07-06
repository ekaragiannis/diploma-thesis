import styled from '@emotion/styled';
import { useEffect, useState } from 'react';
import { useRequestHistory } from '../context/RequestHistoryContext';
import api from '../services/api';
import Button from './Button';
import Dropdown from './Dropdown';

interface SelectOptionsProps {
  onRunClick: (data: SensorData) => void;
}

export interface SensorData {
  sensor: string;
  data: Record<string, number>;
  execution_time: number;
}

const StyledDiv = styled.div`
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  gap: ${({ theme }) => theme.spacing(8)};
`;

const SelectOptions = ({ onRunClick }: SelectOptionsProps) => {
  const [sensorNames, setSensorNames] = useState<string[]>([]);
  const [selectedSensor, setSelectedSensor] = useState<string>('');
  const [selectedDataType, setSelectedDataType] = useState<
    'cached' | 'hourly' | 'raw' | ''
  >('');
  const [isLoading, setIsLoading] = useState<boolean>(false);

  useEffect(() => {
    const fetchSensors = async () => {
      try {
        const response = await api.get<{ sensors: string[] }>('/sensors');
        setSensorNames(response.data.sensors);
      } catch (error) {
        console.error('Error fetching sensors:', error);
        setSensorNames([]);
      }
    };

    fetchSensors();
  }, []);

  const { addRequest } = useRequestHistory();

  const handleRunClick = async () => {
    if (!selectedSensor || !selectedDataType) {
      alert('Please select both a sensor and data type');
      return;
    }

    setIsLoading(true);
    try {
      const response = await api.get<SensorData>(
        `/sensor-data/${selectedSensor}/${selectedDataType}`
      );
      onRunClick(response.data);
      addRequest(
        selectedSensor,
        selectedDataType,
        response.data.execution_time
      );
    } catch (error) {
      console.error('Error fetching sensor data:', error);
      alert('Failed to fetch sensor data. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <StyledDiv>
      <Dropdown
        id="sensor-select"
        label="Sensor"
        options={sensorNames.map((name) => ({ label: name, value: name }))}
        onSelectionChange={(value) => setSelectedSensor(value)}
      />
      <Dropdown
        id="data-type-select"
        label="Data type"
        options={[
          { label: 'Raw', value: 'raw' },
          { label: 'Hourly', value: 'hourly' },
          { label: 'Cached', value: 'cached' },
        ]}
        onSelectionChange={(value) => {
          if (value) {
            setSelectedDataType(value as 'cached' | 'hourly' | 'raw');
          }
        }}
      />
      <Button
        onClick={handleRunClick}
        disabled={isLoading || !selectedSensor || !selectedDataType}
      >
        {isLoading ? 'Loading...' : 'Run'}
      </Button>
    </StyledDiv>
  );
};

export default SelectOptions;
