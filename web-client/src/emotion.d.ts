// emotion.d.ts
import '@emotion/react';
import { Theme as MyTheme } from './theme/theme';

declare module '@emotion/react' {
  export interface Theme extends MyTheme {}
}
