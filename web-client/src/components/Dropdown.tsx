import styled from '@emotion/styled';

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
  /** Placeholder text shown when no option is selected */
  placeholder?: string;
  /** Callback function called when selection changes */
  onSelectionChange: (value: string) => void;
}

/**
 * Styled select element with consistent theming
 */
const StyledDropdown = styled.select`
  background: ${({ theme }) => theme.colors.surface};
  color: ${({ theme }) => theme.colors.text};
  border: none;
  border-radius: ${({ theme }) => theme.borderRadius};
  padding: ${({ theme }) => theme.spacing(2)} ${({ theme }) => theme.spacing(4)};
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s ease;

  &:hover {
    background: ${({ theme }) => theme.colors.primaryHover};
  }

  &:focus {
    outline: 2px solid ${({ theme }) => theme.colors.primary};
    outline-offset: 2px;
  }
`;

/**
 * Styled label element for the dropdown
 */
const StyledLabel = styled.label`
  font-weight: 600;
  color: ${({ theme }) => theme.colors.text};
  margin-bottom: ${({ theme }) => theme.spacing(1)};
`;

/**
 * Container div for the dropdown and label
 */
const StyledDiv = styled.div`
  display: flex;
  flex-direction: column;
  gap: ${({ theme }) => theme.spacing(1)};
`;

/**
 * A styled dropdown/select component with consistent theming
 */
const Dropdown = ({
  id,
  label,
  options,
  placeholder = '-- Select an option --',
  onSelectionChange,
}: DropdownProps) => {
  return (
    <StyledDiv>
      <StyledLabel htmlFor={id}>{label}</StyledLabel>
      <StyledDropdown
        id={id}
        onChange={(e) => onSelectionChange(e.target.value)}
      >
        <option value="">{placeholder}</option>
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </StyledDropdown>
    </StyledDiv>
  );
};

export default Dropdown;
