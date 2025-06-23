<p align="center"><img src="assets/logo.svg" alt="AgentCrafter"></p>

<p align="center">
<a href="https://www.scala-lang.org/"><img src="https://img.shields.io/badge/scala-%23DC322F.svg?style=for-the-badge&logo=scala&logoColor=white"></a>
<a href="https://cucumber.io/"><img src="https://img.shields.io/badge/Cucumber-43B02A?style=for-the-badge&logo=cucumber&logoColor=white"></a>
<a href="https://en.wikipedia.org/wiki/Reinforcement_learning"><img src="https://img.shields.io/badge/Method-Reinforcement--Learning-red?style=for-the-badge"></a>
<a href="https://en.wikipedia.org/wiki/Large_language_model"><img src="https://img.shields.io/badge/Method-LLM-red?style=for-the-badge"></a>
</p>


# AgentCrafter

**AgentCrafter** is a comprehensive multi-agent reinforcement learning framework that explores the intersection of traditional RL algorithms with modern Large Language Model (LLM) integration. Built as part of an Advanced Software Modeling and Design project, it provides a declarative Scala 3 DSL for creating sophisticated multi-agent simulations with real-time visualization and AI-enhanced learning.

## What is AgentCrafter?

AgentCrafter enables researchers and developers to:
- **Experiment with Multi-Agent RL**: Create complex scenarios where multiple agents learn and coordinate in shared environments
- **Integrate LLMs with RL**: Use AI models to generate optimal Q-tables and dynamic environments from natural language descriptions
- **Visualize Learning**: Real-time visualization of agent behavior, Q-values, and learning progress
- **Rapid Prototyping**: Declarative DSL for quick simulation setup without boilerplate code

## Technologies Used

- **Core**: Scala 3.7+ with advanced type system features
- **AI Integration**: OpenAI GPT models for Q-table generation and environment creation
- **Visualization**: Swing-based GUI with real-time rendering
- **Testing**: Cucumber BDD framework for behavior verification
- **Build System**: SBT with modular architecture
- **Algorithms**: Q-Learning, Multi-Agent Reinforcement Learning (MARL)

## Key Features

- 🤖 **Multi-Agent Coordination**: Sophisticated agent interactions with triggers and dependencies
- 🧠 **LLM-Enhanced Learning**: AI-generated Q-tables and environments from natural language
- 🎨 **Real-time Visualization**: Interactive GUI with agent tracking and analytics
- 🏗️ **Declarative DSL**: Clean, type-safe configuration syntax
- 🧪 **BDD Testing**: Comprehensive behavior-driven testing with Cucumber

## Requirements

- Scala 3.7.0+
- SBT 1.9.0+
- Java 11+
- OpenAI API key (for LLM features)

## Quick Start

```scala
import agentcrafter.marl.dsl.SimulationDSL

object BasicExample extends App with SimulationDSL:
  simulation:
    grid:
      10 x 8
    agent:
      Name >> "Explorer"
      Start >> (1, 1)
      Goal >> (6, 8)
      withLearner:
        Alpha >> 0.1
        Gamma >> 0.9
        Eps0 >> 0.3
    Episodes >> 1000
    WithGUI >> true
```

For comprehensive examples including LLM integration and multi-agent scenarios, see the examples in `src/main/scala/agentcrafter/examples/`.

## Documentation

Comprehensive documentation is available in the [`docs`](docs/) directory:

- **[Framework Overview](docs/index.md)** - Architecture and core concepts
- **[DSL Grammar](docs/grammar/README.md)** - Complete syntax reference
- **[Q-Learning Foundation](docs/qlearning/README.md)** - Basic reinforcement learning implementation
- **[Multi-Agent RL](docs/marl/README.md)** - Multi-agent coordination and learning
- **[LLM Integration](docs/llm/README.md)** - AI-enhanced Q-table generation and environment creation
- **[Project Conclusions](docs/conclusions/README.md)** - Insights and lessons learned

## Project Structure

```
src/main/scala/agentcrafter/
├── common/         # Core RL components (QLearner, GridWorld, etc.)
├── marl/           # Multi-agent RL framework
│   ├── dsl/        # Domain-specific language
│   ├── builders/   # Simulation builders
│   └── managers/   # Agent, environment, and episode managers
├── llmqlearning/   # LLM integration services
├── visualizers/    # Real-time visualization components
└── examples/       # Usage examples
    ├── basic/      # Simple demonstrations
    └── advanced/   # Complex scenarios including LLM integration
```

## Key Components

### Core Framework (`agentcrafter.common`)
- **QLearner**: Advanced Q-Learning implementation with configurable exploration strategies
- **GridWorld**: Environment simulation with wall support and dynamic elements
- **State & Action**: Type-safe state and action representations
- **LearningConfig**: Flexible configuration for learning parameters

### Multi-Agent Framework (`agentcrafter.marl`)
- **SimulationDSL**: Declarative syntax for defining complex simulations
- **AgentManager**: Coordinates multiple learning agents
- **EnvironmentManager**: Handles shared environment state and interactions
- **EpisodeManager**: Manages simulation episodes and learning cycles

### LLM Integration (`agentcrafter.llmqlearning`)
- **LLMQTableService**: AI-powered Q-table generation
- **LLMWallService**: Natural language environment creation
- **QTableLoader**: Intelligent Q-table initialization from LLM outputs
- **Prompts**: Curated prompt templates for optimal LLM interaction

### Visualization (`agentcrafter.visualizers`)
- **Visualizer**: Real-time simulation rendering with agent tracking
- **QTableVisualizer**: Interactive Q-value inspection and debugging
- **ConsoleVisualizer**: Text-based output for headless environments