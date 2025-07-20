import { createTheme } from '@mui/material/styles';

/**
 * Material-UI theme configuration for the application
 */
const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#b91c1c',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#6b7280',
    },
    background: {
      default: '#171717',
      paper: '#1a1a1a',
    },
    text: {
      primary: '#f3f4f6',
      secondary: '#a3a3a3',
    },
    divider: '#333',
    action: {
      disabled: '#333',
    },
  },
  shape: {
    borderRadius: 6,
  },
  spacing: 8,
  typography: {
    fontFamily: 'Inter, sans-serif',
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          backgroundColor: '#171717',
          color: '#f3f4f6',
        },
      },
    },
  },
});

export default theme;
