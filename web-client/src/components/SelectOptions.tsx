import styled from '@emotion/styled';
import { useHistory } from '../hooks/useHistory';
import { useSensorSelectionStore } from '../stores/sensorSelectionStore';
import { useResultsStore } from '../stores/resultsStore';
import { useSensors } from '../queries/useSensors';
import { useSensorData } from '../queries/useSensorData';
import Button from './Button';
import Dropdown from './Dropdown';

const StyledDiv = styled.div`
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  gap: ${({ theme }) => theme.spacing(8)};
`;

const SelectOptions = () => {
  // Zustand stores
  const {
    selectedSensor,
    selectedDataType,
    setSelectedSensor,
    setSelectedDataType,
  } = useSensorSelectionStore();
  const { setResults } = useResultsStore();

  // TanStack Query
  const {
    data: sensorsData,
    isLoading: sensorsLoading,
    error: sensorsError,
  } = useSensors();
  const sensorNames = sensorsData?.sensors || [];
  const {
    isLoading: sensorDataLoading,
    error: sensorDataError,
    refetch: refetchSensorData,
  } = useSensorData(
    selectedSensor,
    selectedDataType as 'cached' | 'hourly' | 'raw'
  );

  const { addRecord } = useHistory();

  const handleRunClick = async () => {
    if (!selectedSensor || !selectedDataType) {
      alert('Please select both a sensor and data type');
      return;
    }
    try {
      const result = await refetchSensorData();
      if (result.data) {
        setResults(result.data);
        addRecord(selectedSensor, selectedDataType, result.data.execution_time);
      }
    } catch (error) {
      console.error('Error fetching sensor data:', error);
      alert('Failed to fetch sensor data. Please try again.');
    }
  };

  if (sensorsError) {
    return (
      <StyledDiv>
        <div>Error loading sensors: {sensorsError.message}</div>
      </StyledDiv>
    );
  }
  if (sensorDataError) {
    return (
      <StyledDiv>
        <div>Error loading sensor data: {sensorDataError.message}</div>
      </StyledDiv>
    );
  }

  return (
    <StyledDiv>
      <Dropdown
        id="sensor-select"
        label="Sensor"
        options={sensorNames.map((name) => ({ label: name, value: name }))}
        onSelectionChange={setSelectedSensor}
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
        disabled={
          !selectedSensor ||
          !selectedDataType ||
          sensorsLoading ||
          sensorDataLoading
        }
      >
        {sensorDataLoading ? 'Loading...' : 'Run'}
      </Button>
    </StyledDiv>
  );
};

export default SelectOptions;
