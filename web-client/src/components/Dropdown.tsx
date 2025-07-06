import styled from '@emotion/styled';

interface DropdownOption {
  value: string;
  label: string;
  description?: string;
}

interface DropdownProps {
  id: string;
  label: string;
  options: DropdownOption[];
  placeholder?: string;
  onSelectionChange: (value: string) => void;
}

const StyledDropdown = styled.select`
  background: ${({ theme }) => theme.colors.surface};
  color: ${({ theme }) => theme.colors.text};
  border: none;
  border-radius: ${({ theme }) => theme.borderRadius};
  padding: ${({ theme }) => theme.spacing(2)} ${({ theme }) => theme.spacing(4)};
  cursor: pointer;
  font-weight: 600;
  &:hover {
    background: ${({ theme }) => theme.colors.primaryHover};
  }
`;

const StyledLabel = styled.label`
  font-weight: 600;
  color: ${({ theme }) => theme.colors.text};
`;

const StyledDiv = styled.div`
  display: flex;
  flex-direction: column;
  gap: ${({ theme }) => theme.spacing(1)};
`;

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
