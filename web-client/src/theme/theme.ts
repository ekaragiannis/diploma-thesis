// src/theme.ts
const theme = {
  colors: {
    background: '#171717', // dark background
    surface: '#232323', // slightly lighter for cards/sections
    primary: '#b91c1c', // red accent
    primaryHover: '#6b7280', // light gray for hover
    text: '#f3f4f6', // light gray text
    textSecondary: '#a3a3a3', // muted text
    border: '#333',
    disabled: '#333',
  },
  borderRadius: '6px',
  spacing: (factor: number) => `${0.5 * factor}rem`,
};

export interface Theme {
  colors: {
    background: string;
    surface: string;
    primary: string;
    primaryHover: string;
    text: string;
    textSecondary: string;
    border: string;
    disabled: string;
  };
  borderRadius: string;
  spacing: (factor: number) => string;
}

export default theme;
