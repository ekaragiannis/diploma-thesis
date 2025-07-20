import { Box, Chip, ListItem, ListItemText, Typography } from '@mui/material';
import type { RequestHistoryRecord } from '../types';

interface HistoryListItemProps {
  /** The request record data to display */
  request: RequestHistoryRecord;
}

/**
 * A component for displaying individual history items
 * The component formats the timestamp into a human-readable format
 * and highlights the execution time with the primary theme color.
 *
 * @returns A Material-UI list item displaying history information
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
    <ListItem
      divider
      sx={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        py: 1.5,
      }}
    >
      <ListItemText
        primary={
          <Typography variant="body1" fontWeight={600}>
            {request.sensor} / {request.dataType}
          </Typography>
        }
        secondary={
          <Typography variant="body2" color="text.secondary">
            {formatTimestamp(request.timestamp)}
          </Typography>
        }
      />
      <Box sx={{ ml: 2 }}>
        <Chip
          label={`${request.execution_time} ms`}
          color="primary"
          size="small"
          variant="outlined"
        />
      </Box>
    </ListItem>
  );
};

export default HistoryListItem;
