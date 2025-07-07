import { useLocalStorage } from 'usehooks-ts';
import type { RequestHistoryRecord } from '../types';

export const useHistory = () => {
  const [history, setHistory, clearHistory] = useLocalStorage<
    RequestHistoryRecord[]
  >('history', []);

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
