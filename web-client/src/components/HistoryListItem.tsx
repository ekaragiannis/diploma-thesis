import styled from '@emotion/styled';
import type { RequestRecord } from '../hooks/useHistory';

interface HistoryListItemProps {
  request: RequestRecord;
}

const HistoryItem = styled.li`
  padding: 12px 0;
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};
  color: ${({ theme }) => theme.colors.text};

  &:last-child {
    border-bottom: none;
  }
`;

const HistoryItemContent = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: ${({ theme }) => theme.spacing(2)};
`;

const HistoryItemMain = styled.div`
  display: flex;
  flex-direction: column;
  gap: ${({ theme }) => theme.spacing(1)};
  flex: 1;
`;

const HistoryItemTitle = styled.div`
  font-weight: 600;
  color: ${({ theme }) => theme.colors.text};
`;

const HistoryItemSecondary = styled.div`
  font-size: 0.8rem;
  color: ${({ theme }) => theme.colors.textSecondary};
`;

const HistoryItemExecutionTime = styled.div`
  font-weight: 500;
  color: ${({ theme }) => theme.colors.primary};
  font-size: 0.9rem;
  white-space: nowrap;
`;

const HistoryListItem = ({ request }: HistoryListItemProps) => {
  const formatTimestamp = (timestamp: number) => {
    return new Date(timestamp).toLocaleString();
  };

  return (
    <HistoryItem>
      <HistoryItemContent>
        <HistoryItemMain>
          <HistoryItemTitle>
            {request.sensor} / {request.dataType}
          </HistoryItemTitle>
          <HistoryItemSecondary>
            {formatTimestamp(request.timestamp)}
          </HistoryItemSecondary>
        </HistoryItemMain>
        <HistoryItemExecutionTime>
          {request.execution_time} ms
        </HistoryItemExecutionTime>
      </HistoryItemContent>
    </HistoryItem>
  );
};

export default HistoryListItem;
