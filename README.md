<p align="center"><img src="assets/logo.svg" alt="AgentCrafter"></p>

<p align="center">
<a href="https://www.scala-lang.org/"><img src="https://img.shields.io/badge/scala-%23DC322F.svg?style=for-the-badge&logo=scala&logoColor=white"></a>
<a href="https://cucumber.io/"><img src="https://img.shields.io/badge/Cucumber-43B02A?style=for-the-badge&logo=cucumber&logoColor=white"></a>
<a href="https://en.wikipedia.org/wiki/Reinforcement_learning"><img src="https://img.shields.io/badge/Method-Reinforcement--Learning-red?style=for-the-badge"></a>
<a href="https://en.wikipedia.org/wiki/Large_language_model"><img src="https://img.shields.io/badge/Method-LLM-red?style=for-the-badge"></a>
</p>


# AgentCrafter: Advanced Software Modeling and Design Project

## Overview

This project is part of the **Advanced Software Modeling and Design** course, exploring different approaches to Reinforcement Learning (RL) implementation and optimization. The repository contains a comprehensive multi-agent reinforcement learning system, developed through several distinct phases of refinement and improvement.

## Project Structure

The project is organized into four main components, each representing a different approach to reinforcement learning:

1. **Grid Q-Learning**: The initial approach implementing a basic Q-Learning algorithm on a grid-based environment.
2. **Visual Q-Learning**: A refined approach that introduces visual elements and improved learning mechanisms.
3. **Multi-Agent Reinforcement Learning (MARL)**: The most advanced implementation featuring multi-agent systems and sophisticated coordination mechanisms.
4. **LLM Q-Learning Extensions**: An innovative extension that integrates Large Language Models (LLMs) for intelligent Q-Table generation.

## Features

- **Modular Architecture**: Clean separation of concerns with common components shared across implementations
- **Domain-Specific Language (DSL)**: Intuitive Scala 3 DSL for defining simulations and agent configurations
- **Visualization Tools**: Real-time visualization of learning processes and Q-Table values
- **LLM Integration**: Capability to generate optimal Q-Tables using Large Language Models
- **Comprehensive Testing**: BDD-style tests using Cucumber for behavior verification

## Requirements

- Scala 3.7.0+
- SBT 1.9.0+
- Java 11+

## Usage

### Running a Basic Simulation

```scala
import MARL.DSL.SimulationDSL

object BasicSimulation extends SimulationDSL with App:
  simulation:
    grid: 5 x 5
    
    agent:
      Name >> "Agent1"
      Start >> (0, 0)
      Goal >> (4, 4)
      Reward >> 100.0
      
      withLearner:
        Alpha >> 0.1
        Gamma >> 0.9
        Epsilon >> 0.8
        
    episodes(5_000)
    steps(400)
    showAfter(4_000)
    delay(100)
    withGUI(true)
```

### Using LLM Integration

```scala
import MARL.DSL.SimulationDSL
import llmqlearning.LLMQLearning

object LLMSimulation extends SimulationDSL with LLMQLearning with App:
  simulation:
    useLLM:
      Enabled >> true
      Model >> "gpt-4o"
      
    grid: 10 x 10
    
    agent:
      Name >> "SmartAgent"
      Start >> (0, 0)
      Goal >> (9, 9)
      Reward >> 100.0
      withLearner:
        Alpha >> 0.1
        Gamma >> 0.99

    episodes(5_000)
    steps(400)
    showAfter(4_000)
    delay(100)
    withGUI(true)
```

## Documentation

Detailed documentation for each component is available in the `docs` directory:

- [Grid Q-Learning Documentation](docs/gridqlearning/README.md)
- [Visual Q-Learning Documentation](docs/visualqlearning/README.md)
- [Multi-Agent RL Documentation](docs/marl/README.md)
- [LLM Q-Learning Documentation](docs/llmqlearning/README.md)

## Testing

The project uses Cucumber for behavior-driven development (BDD) testing. Feature files are located in `src/test/resources/`.

To run the tests:

```bash
sbt test
```