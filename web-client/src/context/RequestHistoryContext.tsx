import type { ReactNode } from 'react';
import React, { createContext, useContext, useReducer } from 'react';

interface RequestRecord {
  id: string;
  sensor: string;
  dataType: string;
  timestamp: number;
  execution_time: number;
}

interface RequestHistoryState {
  history: RequestRecord[];
}

type RequestHistoryAction =
  | { type: 'ADD_REQUEST'; payload: RequestRecord }
  | { type: 'CLEAR_HISTORY' };

const initialState: RequestHistoryState = {
  history: [],
};

const requestHistoryReducer = (
  state: RequestHistoryState,
  action: RequestHistoryAction
): RequestHistoryState => {
  switch (action.type) {
    case 'ADD_REQUEST':
      return {
        ...state,
        history: [action.payload, ...state.history].slice(0, 50), // Keep last 50 requests
      };
    case 'CLEAR_HISTORY':
      return {
        ...state,
        history: [],
      };
    default:
      return state;
  }
};

interface RequestHistoryContextType {
  state: RequestHistoryState;
  addRequest: (sensor: string, dataType: string, responseData?: any) => void;
  clearHistory: () => void;
}

const RequestHistoryContext = createContext<
  RequestHistoryContextType | undefined
>(undefined);

export const RequestHistoryProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const [state, dispatch] = useReducer(requestHistoryReducer, initialState);

  const addRequest = (
    sensor: string,
    dataType: string,
    execution_time: number
  ) => {
    const newRecord: RequestRecord = {
      id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      sensor,
      dataType,
      timestamp: Date.now(),
      execution_time,
    };
    dispatch({ type: 'ADD_REQUEST', payload: newRecord });
  };

  const clearHistory = () => {
    dispatch({ type: 'CLEAR_HISTORY' });
  };

  const value: RequestHistoryContextType = {
    state,
    addRequest,
    clearHistory,
  };

  return (
    <RequestHistoryContext.Provider value={value}>
      {children}
    </RequestHistoryContext.Provider>
  );
};

export const useRequestHistory = (): RequestHistoryContextType => {
  const context = useContext(RequestHistoryContext);
  if (context === undefined) {
    throw new Error(
      'useRequestHistory must be used within a RequestHistoryProvider'
    );
  }
  return context;
};
