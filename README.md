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
import agentcrafter.MARL.DSL.{SimulationDSL, *}

object BasicExample extends App with SimulationDSL:
  simulation:
    grid: 8 x 10
    agent:
      Name >> "Explorer"
      Start >> (1, 1)
      Goal >> (6, 8)
    Episodes >> 1000
    WithGUI >> true
```

For comprehensive examples including LLM integration and multi-agent scenarios, see the [examples documentation](docs/examples/comprehensive-example.md).

## Documentation

Comprehensive documentation is available in the [`docs`](docs/) directory:

- **[Framework Overview](docs/index.md)** - Architecture and core concepts
- **[DSL Grammar](docs/grammar.md)** - Complete syntax reference
- **[Grid Q-Learning](docs/gridqlearning/README.md)** - Basic reinforcement learning
- **[Multi-Agent RL](docs/marl/README.md)** - Multi-agent coordination and learning
- **[LLM Q-Learning](docs/llmqlearning/README.md)** - AI-enhanced Q-table generation
- **[LLM Wall Generation](docs/wallsfromllm/README.md)** - Dynamic environment creation
- **[Visual Q-Learning](docs/visualqlearning/README.md)** - Enhanced learning with visualization
- **[Visualizers](docs/visualizers/README.md)** - Real-time simulation visualization
- **[Comprehensive Examples](docs/examples/comprehensive-example.md)** - Complete usage scenarios