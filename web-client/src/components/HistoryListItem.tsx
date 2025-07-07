import styled from '@emotion/styled';
import type { RequestHistoryRecord } from '../types';

interface HistoryListItemProps {
  /** The request record data to display */
  request: RequestHistoryRecord;
}

/**
 * Styled list item container for history entries
 */
const HistoryItem = styled.li`
  padding: 12px 0;
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};
  color: ${({ theme }) => theme.colors.text};

  &:last-child {
    border-bottom: none;
  }
`;

/**
 * Container for the main content area of a history item
 */
const HistoryItemContent = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: ${({ theme }) => theme.spacing(2)};
`;

/**
 * Main content area containing title and timestamp
 */
const HistoryItemMain = styled.div`
  display: flex;
  flex-direction: column;
  gap: ${({ theme }) => theme.spacing(1)};
  flex: 1;
`;

/**
 * Title text for the history item (sensor and data type)
 */
const HistoryItemTitle = styled.div`
  font-weight: 600;
  color: ${({ theme }) => theme.colors.text};
`;

/**
 * Secondary text showing the timestamp
 */
const HistoryItemSecondary = styled.div`
  font-size: 0.8rem;
  color: ${({ theme }) => theme.colors.textSecondary};
`;

/**
 * Execution time display with primary color highlighting
 */
const HistoryItemExecutionTime = styled.div`
  font-weight: 500;
  color: ${({ theme }) => theme.colors.primary};
  font-size: 0.9rem;
  white-space: nowrap;
`;

/**
 * A component for displaying individual history items
 * The component formats the timestamp into a human-readable format
 * and highlights the execution time with the primary theme color.
 *
 * @returns A styled list item displaying history information
 */
const HistoryListItem = ({ request }: HistoryListItemProps) => {
  /**
   * Formats a timestamp into a human-readable date string
   *
   * @param timestamp - Unix timestamp in milliseconds
   * @returns Formatted date string
   */
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
