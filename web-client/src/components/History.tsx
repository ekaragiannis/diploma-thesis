import styled from '@emotion/styled';
import { useRequestHistory } from '../context/RequestHistoryContext';
import Button from './Button';

const HistoryHeader = styled.div`
  font-size: 1rem;
  flex-grow: 0;
  font-weight: 600;
  margin-bottom: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

const HistoryList = styled.ul`
  flex-grow: 1;
  flex-basis: 0;
  list-style: none;
  padding: 0;
  margin: 0;
  overflow-y: auto;
`;

const HistoryContainer = styled.div`
  background-color: ${({ theme }) => theme.colors.surface};
  padding: ${({ theme }) => theme.spacing(2)};
  border-radius: ${({ theme }) => theme.borderRadius};
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  padding: 24px;
`;

const HistoryItem = styled.li`
  padding: 8px 0;
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};
  color: ${({ theme }) => theme.colors.text};
`;

const HistoryItemSecondary = styled.div`
  font-size: 0.8rem;
  color: ${({ theme }) => theme.colors.textSecondary};
`;

const HistoryItemContent = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
`;

const History = () => {
  const { state, clearHistory } = useRequestHistory();

  const formatTimestamp = (timestamp: number) => {
    return new Date(timestamp).toLocaleString();
  };

  return (
    <HistoryContainer>
      <HistoryHeader>
        <h3>History ({state.history.length})</h3>
        {state.history.length > 0 && (
          <Button onClick={clearHistory}>Clear</Button>
        )}
      </HistoryHeader>
      <HistoryList>
        {state.history.length === 0 ? (
          <li style={{ color: '#a3a3a3', fontStyle: 'italic' }}>
            No requests made yet
          </li>
        ) : (
          state.history.map((request: any) => (
            <HistoryItem key={request.id}>
              <HistoryItemContent>
                <div>
                  {request.sensor} - {request.dataType}
                </div>
                <div>{request.execution_time} ms</div>
                <HistoryItemSecondary>
                  {formatTimestamp(request.timestamp)}
                </HistoryItemSecondary>
              </HistoryItemContent>
            </HistoryItem>
          ))
        )}
      </HistoryList>
    </HistoryContainer>
  );
};

export default History;
