import styled from '@emotion/styled';

interface ButtonProps {
  /** The text content to display on the button */
  children: React.ReactNode;
  /** Click handler function */
  onClick: () => void;
  /** Whether the button is disabled */
  disabled?: boolean;
  /** Additional CSS class name */
  className?: string;
}

/**
 * Styled button component with consistent theming
 */
const StyledButton = styled.button<{ disabled?: boolean }>`
  background: ${({ theme, disabled }) =>
    disabled ? theme.colors.disabled : theme.colors.primary};
  color: ${({ theme }) => theme.colors.text};
  border: none;
  border-radius: ${({ theme }) => theme.borderRadius};
  padding: ${({ theme }) => theme.spacing(2)} ${({ theme }) => theme.spacing(4)};
  cursor: ${({ disabled }) => (disabled ? 'not-allowed' : 'pointer')};
  font-weight: 600;
  transition: background-color 0.2s ease;

  &:hover {
    background: ${({ theme, disabled }) =>
      disabled ? theme.colors.disabled : theme.colors.primaryHover};
  }

  &:active {
    transform: ${({ disabled }) => (disabled ? 'none' : 'translateY(1px)')};
  }
`;

/**
 * A reusable button component with consistent styling and behavior
 */
const Button = ({
  children,
  onClick,
  disabled = false,
  className,
}: ButtonProps) => {
  return (
    <StyledButton onClick={onClick} disabled={disabled} className={className}>
      {children}
    </StyledButton>
  );
};

export default Button;
