# AgentCrafter Architecture Documentation

This document provides an overview of the PlantUML architecture diagrams that illustrate the high-level design and component relationships in the AgentCrafter system.

## Viewing the Diagrams

The diagrams are written in PlantUML format (`.puml` files). To view them, you can:

1. **Online PlantUML Server**: Copy the content of any `.puml` file to [PlantUML Online Server](http://www.plantuml.com/plantuml/uml/)
2. **VS Code Extension**: Install the "PlantUML" extension for Visual Studio Code
3. **IntelliJ Plugin**: Use the PlantUML integration plugin for IntelliJ IDEA
4. **Local PlantUML**: Install PlantUML locally and generate PNG/SVG files

## Architecture Diagrams Overview

### 1. System Overview (`architecture-overview.puml`)

**Purpose**: Provides a complete bird's-eye view of the entire AgentCrafter system.

**Key Components Shown**:
- DSL Layer (SimulationDSL, LLMQLearning, Properties)
- Builder Pattern (SimulationBuilder, AgentBuilder, etc.)
- Domain Model (AgentSpec, WorldSpec, Effects, Triggers)
- Learning Core (Environment, QLearner, GridWorld)
- LLM Integration (API clients, services, configuration)
- Visualization (Visualizer, QTableVisualizer)
- Execution (Runner, EpisodeManager, Simulation)

**Use Case**: Understanding the overall system architecture and how major components interact.

### 2. DSL Flow (`dsl-flow.puml`)

**Purpose**: Illustrates the sequence of operations when using the DSL to configure a simulation.

**Key Flow Shown**:
- User writes DSL configuration
- SimulationDSL processes the configuration
- Builder pattern constructs the simulation
- Properties are applied using the `>>` operator
- Final simulation is built and executed

**Use Case**: Understanding how the DSL syntax translates to actual object construction.

### 3. Learning Architecture (`learning-architecture.puml`)

**Purpose**: Focuses on the reinforcement learning components and their relationships.

**Key Components Shown**:
- Core learning interfaces (Learner, Environment)
- Q-Learning implementation (QLearner, LearningParameters)
- Environment management (GridWorld, State, Action)
- LLM enhancement services
- Execution and visualization components

**Use Case**: Understanding the Q-Learning implementation and how LLM integration enhances it.

### 4. Multi-Agent Coordination (`multi-agent-coordination.puml`)

**Purpose**: Shows how multiple agents coordinate through triggers and effects.

**Key Components Shown**:
- Agent system (AgentSpec, Agent)
- Coordination mechanisms (Trigger, Effect types)
- World management (WorldSpec, GridWorld)
- Execution control (EpisodeManager, Simulation)
- Visualization of multi-agent scenarios

**Use Case**: Understanding how agents interact and coordinate in multi-agent scenarios.

### 5. LLM Integration (`llm-integration.puml`)

**Purpose**: Details the Large Language Model integration architecture.

**Key Components Shown**:
- DSL integration (LLMQLearning, LLMConfig, LLMProperty)
- LLM services (LLMApiClient, LLMQTableService, LLMWallService)
- Prompt management and API communication
- Integration points with the main system

**Use Case**: Understanding how LLM capabilities are integrated into the reinforcement learning system.

## Component Relationships

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

## Design Principles

### Separation of Concerns
- DSL syntax is separate from implementation
- Learning algorithms are independent of visualization
- LLM integration is optional and modular

### Extensibility
- New learner types can be added easily
- Additional effect types can be implemented
- New LLM services can be integrated

### Type Safety
- Scala 3's type system ensures compile-time safety
- DSL properties are type-checked
- Builder pattern prevents invalid configurations

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