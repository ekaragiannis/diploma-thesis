import styled from '@emotion/styled';
import type { PropsWithChildren } from 'react';

interface ButtonProps {
  onClick: () => void;
  disabled?: boolean;
  style?: React.CSSProperties;
}

const StyledButton = styled.button`
  background: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.text};
  border: none;
  border-radius: ${({ theme }) => theme.borderRadius};
  padding: ${({ theme }) => theme.spacing(2)} ${({ theme }) => theme.spacing(4)};
  cursor: pointer;
  font-weight: 600;
  &:hover {
    background: ${({ theme }) => theme.colors.primaryHover};
  }
  &:disabled {
    background: ${({ theme }) => theme.colors.disabled};
    cursor: not-allowed;
  }
`;

const Button = ({
  onClick,
  disabled = false,
  children,
  style,
}: PropsWithChildren<ButtonProps>) => {
  return (
    <StyledButton onClick={onClick} disabled={disabled} style={style}>
      {children}
    </StyledButton>
  );
};

export default Button;
