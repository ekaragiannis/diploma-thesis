import { useLocalStorage } from 'usehooks-ts';

export interface RequestRecord {
  id: string;
  sensor: string;
  dataType: string;
  timestamp: number;
  execution_time: number;
}

export const useHistory = () => {
  const [history, setHistory, clearHistory] = useLocalStorage<RequestRecord[]>(
    'history',
    []
  );

  const createRequestRecord = (
    sensor: string,
    dataType: string,
    execution_time: number
  ) => {
    return {
      id: crypto.randomUUID(),
      sensor,
      dataType,
      timestamp: Date.now(),
      execution_time,
    };
  };

  const addRecord = (
    sensor: string,
    dataType: string,
    execution_time: number
  ) => {
    setHistory((prev) => [
      createRequestRecord(sensor, dataType, execution_time),
      ...prev,
    ]);
  };

  return { history, addRecord, clearHistory };
};
