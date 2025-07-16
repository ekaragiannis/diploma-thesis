// GlobalStyles.tsx
import { GlobalStyles as MuiGlobalStyles } from '@mui/material';
import '@fontsource/inter/400.css';

/**
 * Global styles for the application using Material-UI GlobalStyles
 */
const GlobalStyles = () => (
  <MuiGlobalStyles
    styles={{
      html: {
        height: '100%',
      },
      body: {
        height: '100%',
        fontFamily: 'Inter, sans-serif',
      },
      '#root': {
        height: '100%',
        overflowX: 'hidden',
      },
    }}
  />
);

export default GlobalStyles;
