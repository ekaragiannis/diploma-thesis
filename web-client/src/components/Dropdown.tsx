import {
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  type SelectChangeEvent,
} from '@mui/material';

/**
 * Interface for dropdown option items
 */
interface DropdownOption {
  /** The value that will be passed to the onChange handler */
  value: string;
  /** The text to display for this option */
  label: string;
}

interface DropdownProps {
  /** Unique identifier for the dropdown */
  id: string;
  /** Label text displayed above the dropdown */
  label: string;
  /** Array of options to display in the dropdown */
  options: DropdownOption[];
  /** Callback function called when selection changes */
  onSelectionChange: (value: string) => void;
  /** Current selected value */
  value?: string;
}

/**
 * A Material-UI Select component with consistent theming
 */
const Dropdown = ({
  id,
  label,
  options,
  onSelectionChange,
  value = '',
}: DropdownProps) => {
  const handleChange = (event: SelectChangeEvent<string>) => {
    onSelectionChange(event.target.value);
  };

  return (
    <FormControl variant="outlined" size="small" sx={{ minWidth: 120 }}>
      <InputLabel id={`${id}-label`}>{label}</InputLabel>
      <Select
        labelId={`${id}-label`}
        id={id}
        value={value}
        label={label}
        onChange={handleChange}
        displayEmpty
      >
        {options.map((option) => (
          <MenuItem key={option.value} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};

export default Dropdown;
