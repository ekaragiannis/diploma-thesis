import { AppBar, Box, Drawer, Toolbar, Typography } from '@mui/material';
import { useState } from 'react';
import History from './components/History';
import Results from './components/Results';
import SelectOptions from './components/SelectOptions';
import { useResultsStore } from './stores/resultsStore';

const DRAWER_WIDTH = 360;

function App() {
  const { results } = useResultsStore();
  const [drawerOpen, setDrawerOpen] = useState(true);

  const toggleDrawer = () => {
    setDrawerOpen(!drawerOpen);
  };

  return (
    <Box sx={{ display: 'flex', height: '100vh' }}>
      {/* App Bar */}
      <AppBar
        position="fixed"
        sx={{
          zIndex: (theme) => theme.zIndex.drawer + 1,
          backgroundColor: 'background.paper',
        }}
      >
        <Toolbar>
          <Typography variant="h6" noWrap component="div">
            Sensor Data Dashboard
          </Typography>
        </Toolbar>
      </AppBar>

      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 2,
          p: 3,
          mt: 8, // Account for app bar height
        }}
      >
        <SelectOptions />
        {results && (
          <>
            <Typography variant="body1" color="text.secondary">
              Execution time: {results.execution_time} ms
            </Typography>
            <Results />
          </>
        )}
      </Box>
      {/* History Drawer */}
      <Drawer
        variant="permanent"
        anchor="right"
        open={drawerOpen}
        onClose={toggleDrawer}
        sx={{
          width: DRAWER_WIDTH,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            boxSizing: 'border-box',
          },
        }}
      >
        <Toolbar /> {/* Spacer for app bar */}
        <Box sx={{ p: 2, height: '100%' }}>
          <History />
        </Box>
      </Drawer>
    </Box>
  );
}

export default App;
