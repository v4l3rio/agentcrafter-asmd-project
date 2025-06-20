<p align="center"><img src="assets/logo.svg" alt="AgentCrafter"></p>

<p align="center">
<a href="https://www.scala-lang.org/"><img src="https://img.shields.io/badge/scala-%23DC322F.svg?style=for-the-badge&logo=scala&logoColor=white"></a>
<a href="https://cucumber.io/"><img src="https://img.shields.io/badge/Cucumber-43B02A?style=for-the-badge&logo=cucumber&logoColor=white"></a>
<a href="https://en.wikipedia.org/wiki/Reinforcement_learning"><img src="https://img.shields.io/badge/Method-Reinforcement--Learning-red?style=for-the-badge"></a>
<a href="https://en.wikipedia.org/wiki/Large_language_model"><img src="https://img.shields.io/badge/Method-LLM-red?style=for-the-badge"></a>
</p>


# AgentCrafter: Advanced Software Modeling and Design Project

## Overview

This project is part of the **Advanced Software Modeling and Design** course, exploring different approaches to Reinforcement Learning (RL) implementation and optimization. The repository contains a comprehensive multi-agent reinforcement learning system with advanced features including LLM integration and dynamic environment generation.

## Project Architecture

The project is built around a modular architecture with the following key components:

### Core Components

1. **Common Framework** (`agentcrafter.common`): Shared abstractions including:
   - `Environment` trait for defining RL environments
   - `GridWorld` implementation for grid-based environments
   - `QLearner` for Q-learning algorithm implementation
   - `State`, `Action`, and `StepResult` core data types

2. **Multi-Agent Reinforcement Learning (MARL)** (`agentcrafter.marl`):
   - Advanced multi-agent coordination and learning
   - `EpisodeManager` for orchestrating agent interactions
   - `Runner` for simulation execution
   - Sophisticated trigger system for environment dynamics

3. **Domain-Specific Language (DSL)** (`agentcrafter.MARL.DSL`):
   - Intuitive Scala 3 DSL for simulation configuration
   - Property-based configuration system
   - Fluent API for agent and environment setup

4. **LLM Integration** (`agentcrafter.llmqlearning`):
   - Q-Table generation using Large Language Models
   - Dynamic wall generation from natural language prompts
   - Support for multiple LLM providers (GPT-4, etc.)

5. **Visualization System** (`agentcrafter.visualizers`):
   - Real-time simulation visualization
   - Q-Table value visualization
   - Multi-agent tracking and statistics

## Key Features

- **🤖 Multi-Agent Learning**: Sophisticated coordination between multiple learning agents
- **🧠 LLM Integration**: Generate optimal Q-Tables and environments using AI
- **🎨 Rich Visualization**: Real-time visualization of learning processes and agent behavior
- **🏗️ Declarative DSL**: Clean, readable configuration syntax using Scala 3
- **🔧 Modular Design**: Extensible architecture with pluggable components
- **🧪 Comprehensive Testing**: BDD-style tests using Cucumber for behavior verification
- **🌍 Dynamic Environments**: Support for triggers, switches, and environment modifications

## Requirements

- Scala 3.7.0+
- SBT 1.9.0+
- Java 11+
- OpenAI API key (for LLM features)

## Usage

### Basic Multi-Agent Simulation

```scala
import agentcrafter.MARL.DSL.{SimulationDSL, *}

object BasicSimulation extends App with SimulationDSL:
  import AgentProperty.*
  import LearnerProperty.*
  import TriggerProperty.*
  import SimulationProperty.*
  
  simulation:
    grid:
      8 x 10
    
    // Define walls using ASCII art
    asciiWalls:
      """##########
        |#........#
        |#.####...#
        |#....#...#
        |#....#...#
        |#........#
        |##########"""
    
    agent:
      Name >> "Explorer"
      Start >> (1, 1)
      Goal >> (6, 8)
      withLearner:
        Alpha >> 0.1
        Gamma >> 0.95
        Eps0 >> 0.9
        EpsMin >> 0.1
        Warm >> 1_000
        Optimistic >> 0.2
      onGoal:
        Give >> 100.0
        EndEpisode >> true
        
    Episodes >> 5_000
    Steps >> 400
    ShowAfter >> 4_000
    Delay >> 100
    WithGUI >> true
```

### LLM-Enhanced Q-Learning

```scala
import agentcrafter.MARL.DSL.*
import agentcrafter.llmqlearning.LLMQLearning

object LLMSimulation extends App with LLMQLearning:
  import AgentProperty.*
  import LearnerProperty.*
  import SimulationProperty.*
  
  simulation:
    useLLM:
      Enabled >> true
      Model >> "gpt-4o"
      
    grid:
      10 x 10
    
    agent:
      Name >> "SmartAgent"
      Start >> (0, 0)
      Goal >> (9, 9)
      withLearner:
        Alpha >> 0.05
        Gamma >> 0.99
        Eps0 >> 0  // Lower exploration with LLM guidance
        EpsMin >> 0
        Warm >> 500
        Optimistic >> 0.2
      onGoal:
        Give >> 100.0
        EndEpisode >> true

    Episodes >> 1
    Steps >> 500
    Delay >> 50
    WithGUI >> true
```

### Dynamic Environment Generation

```scala
import agentcrafter.MARL.DSL.*
import agentcrafter.llmqlearning.LLMQLearning

object DynamicEnvironment extends App with LLMQLearning:
  import AgentProperty.*
  import LearnerProperty.*
  import SimulationProperty.*
  import WallLLMProperty.*
  
  simulation:
    grid:
      12 x 15
    
    // Generate walls using LLM
    wallsFromLLM:
      Model >> "gpt-4o"
      Prompt >> """
        Create a challenging maze with multiple paths,
        dead ends, and interesting chokepoints.
        """
    
    agent:
      Name >> "MazeRunner"
      Start >> (1, 1)
      Goal >> (10, 13)
      withLearner:
        Alpha >> 0.12
        Gamma >> 0.95
        Eps0 >> 0.8
        EpsMin >> 0.05
        Warm >> 2_000
        Optimistic >> 0.4
      onGoal:
        Give >> 100.0
        EndEpisode >> true
        
    Episodes >> 5_000
    Steps >> 300
    ShowAfter >> 4_000
    WithGUI >> true
```

### Cooperative Multi-Agent Scenario

```scala
import agentcrafter.MARL.DSL.{SimulationDSL, *}

object CooperativeScenario extends App with SimulationDSL:
  import AgentProperty.*
  import LearnerProperty.*
  import TriggerProperty.*
  import WallProperty.*
  import LineProperty.*
  import SimulationProperty.*
  
  simulation:
    grid:
      8 x 12
    
    walls:
      // Create a wall that can be opened
      line:
        Direction >> "vertical"
        From >> (1, 6)
        To >> (6, 6)
    
    // Agent that opens the wall
    agent:
      Name >> "Opener"
      Start >> (1, 1)
      Goal >> (6, 2)  // Switch location
      withLearner:
        Alpha >> 0.15
        Gamma >> 0.9
        Eps0 >> 0.8
        EpsMin >> 0.1
        Warm >> 1_500
        Optimistic >> 0.5
      onGoal:
        Give >> 25.0
        OpenWall >> (4, 6)  // Open the wall
        EndEpisode >> false
    
    // Agent that needs to cross
    agent:
      Name >> "Runner"
      Start >> (1, 10)
      Goal >> (6, 2)
      withLearner:
        Alpha >> 0.15
        Gamma >> 0.9
        Eps0 >> 0.8
        EpsMin >> 0.1
        Warm >> 1_500
        Optimistic >> 0.5
      onGoal:
        Give >> 50.0
        EndEpisode >> true
        
    Episodes >> 10_000
    Steps >> 500
    ShowAfter >> 8_000
    WithGUI >> true
```

## Documentation

Detailed documentation for each component is available in the `docs` directory:

- [Grid Q-Learning Documentation](docs/gridqlearning/README.md)
- [Visual Q-Learning Documentation](docs/visualqlearning/README.md)
- [Multi-Agent RL Documentation](docs/marl/README.md)
- [LLM Q-Learning Documentation](docs/llmqlearning/README.md)

## Getting Started

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd asmd-project
```

2. Set up environment variables (for LLM features):
```bash
echo "OPENAI_API_KEY=your_api_key_here" > .env
```

3. Run the project:
```bash
sbt run
```

### Running Examples

The project includes several example scenarios:

- **Basic Examples**: `src/main/scala/agentcrafter/examples/basic/`
- **MARL Scenarios**: `src/main/scala/agentcrafter/examples/advanced/marl/`
- **LLM Integration**: `src/main/scala/agentcrafter/examples/advanced/llm/`

## Testing

The project uses Cucumber for behavior-driven development (BDD) testing. Feature files are located in `src/test/resources/`.

To run the tests:

```bash
sbt test
```