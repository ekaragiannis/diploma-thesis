// GlobalStyles.tsx
import { Global, css } from '@emotion/react';
import '@fontsource/inter/400.css';

const GlobalStyles = () => (
  <Global
    styles={css`
      *,
      *::before,
      *::after {
        box-sizing: border-box;
      }

      html,
      body,
      #root {
        margin: 0;
        padding: 0;
        width: 100%;
        background-color: #171717;
        color: white;
        height: 100%;
        overflow-x: hidden;
      }

      body {
        font-family: 'Inter', sans-serif;
      }
    `}
  />
);

export default GlobalStyles;
