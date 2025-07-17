import {
  Box,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from '@mui/material';
import { TableChart, ShowChart } from '@mui/icons-material';
import { useState } from 'react';
import { useResultsStore } from '../stores/resultsStore';
import ResultsTable from './ResultsTable';
import ResultsGraph from './ResultsGraph';

type ViewMode = 'table' | 'graph';

/**
 * A results display component that allows switching between table and graph views
 *
 * The component provides a toggle button to switch between table and graph
 * representations of the sensor data, with proper error handling.
 */
const Results = () => {
  const { results, error } = useResultsStore();
  const [viewMode, setViewMode] = useState<ViewMode>('table');

  const handleViewChange = (
    _event: React.MouseEvent<HTMLElement>,
    newView: ViewMode | null,
  ) => {
    if (newView !== null) {
      setViewMode(newView);
    }
  };

  if (error) {
    return <Typography>{error}</Typography>;
  }

  const hasData = results?.data && results.data.length > 0;

  return (
    <Box sx={{ width: '100%', margin: '0 auto' }}>
      {hasData && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
          <ToggleButtonGroup
            value={viewMode}
            exclusive
            onChange={handleViewChange}
            aria-label="view mode"
            size="small"
          >
            <ToggleButton value="table" aria-label="table view">
              <TableChart sx={{ mr: 1 }} />
              Table
            </ToggleButton>
            <ToggleButton value="graph" aria-label="graph view">
              <ShowChart sx={{ mr: 1 }} />
              Graph
            </ToggleButton>
          </ToggleButtonGroup>
        </Box>
      )}
      
      {viewMode === 'table' ? <ResultsTable /> : <ResultsGraph />}
    </Box>
  );
};

export default Results;
