import {
  Box,
  Button,
  Card,
  CardContent,
  List,
  ListItem,
  Typography,
} from '@mui/material';
import { useHistory } from '../hooks/useHistory';
import HistoryListItem from './HistoryListItem';

/**
 * A component for displaying the history of sensor data requests
 */
const History = () => {
  const { history, clearHistory } = useHistory();

  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CardContent
        sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}
      >
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            mb: 2,
          }}
        >
          <Typography variant="h6" component="h3">
            History ({history.length})
          </Typography>
          {history.length > 0 && (
            <Button variant="contained" size="small" onClick={clearHistory}>
              Clear
            </Button>
          )}
        </Box>

        <List sx={{ flexGrow: 1, overflow: 'auto', p: 0 }}>
          {history.length === 0 ? (
            <ListItem>
              <Typography
                variant="body2"
                color="text.secondary"
                sx={{ fontStyle: 'italic', textAlign: 'center', width: '100%' }}
              >
                No requests made yet
              </Typography>
            </ListItem>
          ) : (
            history.map((request) => (
              <HistoryListItem key={request.id} request={request} />
            ))
          )}
        </List>
      </CardContent>
    </Card>
  );
};

export default History;
