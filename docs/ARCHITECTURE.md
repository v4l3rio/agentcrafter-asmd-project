# AgentCrafter System Architecture

This document provides a comprehensive overview of the AgentCrafter system architecture, illustrating the key components and their relationships through PlantUML diagrams.

## System Overview

AgentCrafter is built on a layered architecture that separates concerns and provides flexibility for different types of reinforcement learning scenarios.

![AgentCrafter System Architecture](schemas/system-overview.svg)

## DSL Configuration Flow

The following diagram shows how user configurations flow through the system:

![AgentCrafter DSL Configuration Flow](schemas/dsl-flow.svg)

## LLM Integration Architecture

The system provides comprehensive LLM integration capabilities:

![AgentCrafter LLM Integration Architecture](schemas/llm-integration.svg)

## Design Principles

### Separation of Concerns
- DSL syntax is separate from implementation
- Learning algorithms are independent of visualization
- LLM integration is optional and modular

### Layered Architecture
The system follows a layered architecture:
1. **DSL Layer**: User-facing domain-specific language
2. **Builder Layer**: Fluent API for constructing simulations
3. **Domain Layer**: Core business objects and logic
4. **Service Layer**: LLM integration and external services
5. **Execution Layer**: Runtime simulation management
6. **Visualization Layer**: GUI and rendering components

### Key Patterns
- **Builder Pattern**: Used extensively for fluent DSL construction
- **Strategy Pattern**: Different learner implementations
- **Observer Pattern**: Visualization updates
- **Factory Pattern**: Agent and simulation creation
- **Service Layer**: LLM integration abstraction

## Usage Guidelines

### For Developers
1. Start with the **System Overview** to understand the big picture
2. Study the **DSL Flow** to understand how user configurations are processed
3. Examine the **Learning Architecture** for reinforcement learning details
4. Review **Multi-Agent Coordination** for multi-agent scenarios
5. Explore **LLM Integration** for AI enhancement features

### For Contributors
- Use these diagrams as reference when adding new features
- Update diagrams when making architectural changes
- Ensure new components fit within the existing layered structure

### For Users
- The **DSL Flow** diagram helps understand the configuration syntax
- **Multi-Agent Coordination** explains how agents interact
- **LLM Integration** shows available AI enhancement features

---

*These diagrams are living documentation and should be updated as the system evolves.*