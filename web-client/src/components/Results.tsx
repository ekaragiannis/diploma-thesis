import type { Theme } from '@emotion/react';
import { useTheme } from '@emotion/react';
import DataTable, { type TableStyles } from 'react-data-table-component';
import { useResultsStore } from '../stores/resultsStore';

const customStyles = (theme: Theme): TableStyles => ({
  table: {
    style: {
      maxWidth: '50%',
      minWidth: '500px',
      margin: '0 auto',
    },
  },
  headRow: {
    style: {
      border: 'none',
    },
  },
  rows: {
    style: {
      '&:not(:last-of-type)': {
        border: 'none',
      },
    },
  },
  headCells: {
    style: {
      border: `1px solid ${theme.colors.border}`,
      backgroundColor: theme.colors.surface,
      color: theme.colors.text,
    },
  },
  cells: {
    style: {
      border: `1px solid ${theme.colors.border}`,
      color: theme.colors.text,
      backgroundColor: theme.colors.background,
    },
  },
});

const Results = () => {
  const { results } = useResultsStore();
  const theme = useTheme();

  const rows = results
    ? Object.entries(results.data).map(([hour, energy]) => ({
        hour,
        energy: Number(energy),
      }))
    : [];

  return (
    <DataTable
      customStyles={customStyles(theme)}
      defaultSortFieldId={1}
      columns={[
        { name: 'Hour', selector: (row) => row.hour, sortable: true },
        { name: 'Energy', selector: (row) => row.energy, sortable: true },
      ]}
      data={rows}
    />
  );
};

export default Results;
