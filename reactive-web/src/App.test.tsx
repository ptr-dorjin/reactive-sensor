import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';
import 'jest-canvas-mock';

test('renders title', () => {
  render(<App />);
  const linkElement = screen.getByText(/Reactive web app sample/i);
  expect(linkElement).toBeInTheDocument();
});
