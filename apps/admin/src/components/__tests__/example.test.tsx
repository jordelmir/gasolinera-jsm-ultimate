import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'

// Componente de ejemplo para testing
const ExampleComponent = () => {
  return <div>Hello Testing World</div>
}

describe('ExampleComponent', () => {
  it('renders correctly', () => {
    render(<ExampleComponent />)
    expect(screen.getByText('Hello Testing World')).toBeInTheDocument()
  })
})
