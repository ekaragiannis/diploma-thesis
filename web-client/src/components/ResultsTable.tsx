import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TableSortLabel,
  Typography,
} from '@mui/material';
import { useState, useMemo } from 'react';
import { useResultsStore } from '../stores/resultsStore';
import type { EnergyData } from '../types.d';

type Order = 'asc' | 'desc';
type OrderBy = keyof EnergyData;

/**
 * Generic comparator function for sorting
 */
const getComparator = <T,>(order: Order, orderBy: keyof T) => {
  return (a: T, b: T): number => {
    const aValue = a[orderBy];
    const bValue = b[orderBy];

    let comparison = 0;

    if (typeof aValue === 'string' && typeof bValue === 'string') {
      comparison = aValue.localeCompare(bValue);
    } else if (typeof aValue === 'number' && typeof bValue === 'number') {
      comparison = aValue - bValue;
    }

    return order === 'desc' ? -comparison : comparison;
  };
};

/**
 * Default sort function: by date first, then by period
 */
const getDefaultSortComparator = (data: EnergyData[]): EnergyData[] => {
  return [...data].sort((a, b) => {
    // First sort by date
    const dateComparison = a.date.localeCompare(b.date);
    if (dateComparison !== 0) {
      return dateComparison;
    }

    // If dates are equal, sort by period
    return a.period.localeCompare(b.period);
  });
};

/**
 * A data table component for displaying sensor results using Material-UI Table
 *
 * The component includes sorting functionality and responsive design
 * with proper accessibility features. Default sort is by date, then by period.
 */
const ResultsTable = () => {
  const { results, error } = useResultsStore();
  const [order, setOrder] = useState<Order>('asc');
  const [orderBy, setOrderBy] = useState<OrderBy>('date');

  const handleRequestSort = (property: OrderBy) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
  };

  const sortedRows = useMemo(() => {
    const data = results?.data || [];

    if (data.length === 0) return [];

    // If no specific sort is applied (default state), use default sort
    if (orderBy === 'date' && order === 'asc') {
      return getDefaultSortComparator(data);
    }

    // Otherwise use the generic comparator
    return [...data].sort(getComparator(order, orderBy));
  }, [results?.data, order, orderBy]);

  if (error) {
    return <Typography>{error}</Typography>;
  }

  return (
    <Box sx={{ maxWidth: '50%', minWidth: '500px', margin: '0 auto' }}>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'period'}
                  direction={orderBy === 'period' ? order : 'asc'}
                  onClick={() => handleRequestSort('period')}
                >
                  Hour Period
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'date'}
                  direction={orderBy === 'date' ? order : 'asc'}
                  onClick={() => handleRequestSort('date')}
                >
                  Date
                </TableSortLabel>
              </TableCell>
              <TableCell>
                <TableSortLabel
                  active={orderBy === 'energy_total'}
                  direction={orderBy === 'energy_total' ? order : 'asc'}
                  onClick={() => handleRequestSort('energy_total')}
                >
                  Energy
                </TableSortLabel>
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {sortedRows.map((row, index) => (
              <TableRow key={index}>
                <TableCell>{row.period}</TableCell>
                <TableCell>{row.date}</TableCell>
                <TableCell>{row.energy_total}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default ResultsTable;