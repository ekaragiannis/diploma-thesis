// GlobalStyles.tsx
import { Global, css } from '@emotion/react';

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
    `}
  />
);

export default GlobalStyles;
