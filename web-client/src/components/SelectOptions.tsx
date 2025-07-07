import styled from '@emotion/styled';
import { useHistory } from '../hooks/useHistory';
import { useSensorData } from '../queries/useSensorData';
import { useSensors } from '../queries/useSensors';
import { useResultsStore } from '../stores/resultsStore';
import { useSensorSelectionStore } from '../stores/sensorSelectionStore';
import Button from './Button';
import Dropdown from './Dropdown';

/**
 * Container for the selection controls layout
 */
const StyledDiv = styled.div`
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  gap: ${({ theme }) => theme.spacing(8)};
`;

/**
 * A component for selecting sensor parameters and triggering data fetching
 *
 * The component manages the selection of sensor and data type through
 * the sensorSelectionStore, fetches available sensors using TanStack Query,
 * and triggers data fetching when the Run button is clicked. Results are
 * stored in the global resultsStore and history is automatically updated.
 *
 * @example
 * ```tsx
 * // The component automatically handles all state management
 * <SelectOptions />
 * ```
 *
 * @returns A form-like component with sensor selection controls
 */
const SelectOptions = () => {
  // Zustand stores for global state management
  const {
    selectedSensor,
    selectedDataType,
    setSelectedSensor,
    setSelectedDataType,
  } = useSensorSelectionStore();
  const { setResults } = useResultsStore();

  // TanStack Query hooks for data fetching
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

  /**
   * Handles the Run button click event
   *
   * Validates that both sensor and data type are selected,
   * triggers the data fetch, and updates the global state
   * with the results. Also adds the request to history.
   */
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
