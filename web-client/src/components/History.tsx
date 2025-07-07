import styled from '@emotion/styled';
import { useHistory } from '../hooks/useHistory';
import Button from './Button';
import HistoryListItem from './HistoryListItem';

/**
 * Header section containing title and clear button
 */
const HistoryHeader = styled.div`
  font-size: 1rem;
  flex-grow: 0;
  font-weight: 600;
  margin-bottom: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

/**
 * Container for the list of history items
 */
const HistoryList = styled.ul`
  flex-grow: 1;
  flex-basis: 0;
  list-style: none;
  padding: 0;
  margin: 0;
  overflow-y: auto;
`;

/**
 * Main container for the entire history component
 */
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

/**
 * Styled element for the empty state message
 */
const EmptyState = styled.li`
  color: ${({ theme }) => theme.colors.textSecondary};
  font-style: italic;
  padding: 12px 0;
  text-align: center;
`;

/**
 * A component for displaying the history of sensor data requests
 */
const History = () => {
  const { history, clearHistory } = useHistory();

  return (
    <HistoryContainer>
      <HistoryHeader>
        <h3>History ({history.length})</h3>
        {history.length > 0 && <Button onClick={clearHistory}>Clear</Button>}
      </HistoryHeader>
      <HistoryList>
        {history.length === 0 ? (
          <EmptyState>No requests made yet</EmptyState>
        ) : (
          history.map((request) => (
            <HistoryListItem key={request.id} request={request} />
          ))
        )}
      </HistoryList>
    </HistoryContainer>
  );
};

export default History;
