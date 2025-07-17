import { Box, Typography, useTheme } from '@mui/material';
import { BarChart } from '@mui/x-charts/BarChart';
import { useMemo } from 'react';
import { useResultsStore } from '../stores/resultsStore';

/**
 * A graph component for displaying sensor energy data over time
 *
 * Uses Material-UI X Charts BarChart to display energy_total values
 * over time periods, with proper data formatting and responsive design.
 */
const ResultsGraph = () => {
  const { results, error } = useResultsStore();
  const theme = useTheme();

  const chartData = useMemo(() => {
    const data = results?.data || [];

    if (data.length === 0) return { xAxisData: [], seriesData: [] };

    // Sort data by date and period for proper chronological order
    const sortedData = [...data].sort((a, b) => {
      const dateComparison = a.date.localeCompare(b.date);
      if (dateComparison !== 0) {
        return dateComparison;
      }
      return a.period.localeCompare(b.period);
    });

    // Format x-axis labels (combine date and period)
    const xAxisData = sortedData.map((item) => `${item.date} ${item.period}`);

    // Extract energy values for the series
    const seriesData = sortedData.map((item) => item.energy_total);

    return { xAxisData, seriesData };
  }, [results?.data]);

  if (error) {
    return <Typography>{error}</Typography>;
  }

  if (chartData.xAxisData.length === 0) {
    return (
      <Box sx={{ maxWidth: '80%', margin: '0 auto', textAlign: 'center' }}>
        <Typography variant="body1" color="text.secondary">
          No data available to display
        </Typography>
      </Box>
    );
  }

  return (
    <BarChart
      width={800}
      height={600}
      series={[
        {
          data: chartData.seriesData,
          label: 'Energy Total',
          color: theme.palette.primary.main,
        },
      ]}
      xAxis={[
        {
          scaleType: 'band',
          data: chartData.xAxisData,
        },
      ]}
      yAxis={[
        {
          label: 'Energy (kWh)',
        },
      ]}
      margin={{ left: 80, right: 50, top: 50, bottom: 120 }}
      grid={{ vertical: true, horizontal: true }}
    />
  );
};

export default ResultsGraph;
