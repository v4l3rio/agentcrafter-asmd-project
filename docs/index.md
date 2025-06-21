# AgentCrafter: Advanced Software Modeling and Design Project

This project is part of the **Advanced Software Modeling and Design** exam, exploring different approaches to Reinforcement Learning (RL) implementation and optimization with innovative LLM integration.

## Project Overview

This repository contains the implementation and documentation of a multi-agent reinforcement learning system with cutting-edge Large Language Model (LLM) integration capabilities. The project demonstrates the evolution from basic Q-Learning to advanced AI-assisted reinforcement learning.

## Current Architecture

The project is built around a sophisticated **Multi-Agent Reinforcement Learning (MARL)** framework with the following key components:

### Core Framework
- **MARL DSL**: A powerful Domain-Specific Language for defining simulations using Scala 3
- **Multi-Agent Support**: Sophisticated coordination mechanisms between multiple learning agents
- **Flexible Environment**: Grid-based environments with walls, triggers, and dynamic interactions
- **Advanced Learning**: Q-Learning with configurable parameters and optimization strategies

### Advanced Learning
- **Multi-Agent Coordination**: Sophisticated agent interaction patterns
- **Dynamic Environment**: Real-time environment modifications
- **Trigger Systems**: Event-driven behaviors and rewards
- **Performance Analytics**: Comprehensive learning metrics
- **Real-time Visualization**: GUI and console-based monitoring

### LLM Integration Extensions

#### Q-Table Generation
- **AI-Powered Bootstrapping**: Use LLMs like GPT-4o to generate initial Q-tables
- **Strategic Initialization**: Leverage AI understanding for better starting policies
- **Seamless Integration**: Drop-in replacement for traditional initialization

#### Wall Generation
- **Natural Language Design**: Describe environments in plain English
- **Creative Layouts**: AI-generated mazes and obstacle patterns
- **Dynamic Content**: Procedural environment generation

#### Visualization System
- **Unified Interface**: Single component for both single and multi-agent scenarios
- **Real-time Monitoring**: Live agent movement and state updates
- **Debug Capabilities**: Q-value inspection and trajectory analysis
- **Console Support**: ASCII-based visualization for headless environments



## Documentation Sections

- **[MARL Framework](marl/)** - Multi-Agent Reinforcement Learning core
- **[Grid Q-Learning](gridqlearning/)** - Foundation grid-based learning
- **[LLM Q-Learning](llmqlearning/)** - AI-powered Q-Table generation
- **[Visual Q-Learning](visualqlearning/)** - Enhanced visualization and monitoring
- **[Visualizers](visualizers/)** - Comprehensive visualization system
- **[LLM Wall Generation](wallsfromllm/)** - AI-powered environment design

## Key Features

- **Scala 3 DSL**: Clean, type-safe domain-specific language for simulation configuration
- **LLM Integration**: First-class support for GPT-4o and other OpenAI models
- **Real-time Visualization**: Live rendering of learning processes and agent behaviors
- **Cooperative Scenarios**: Multi-agent coordination with switches, triggers, and shared objectives
- **Flexible Architecture**: Modular design supporting various learning algorithms and environments
- **Comprehensive Testing**: BDD-style testing with Cucumber for behavior verification

## Getting Started

To understand the project:

1. Start with [MARL](marl/) to understand the core framework
2. Explore [LLM Q-Learning](llmqlearning/) for AI-enhanced learning
3. Try [LLM Wall Generation](wallsfromllm/) for automatic environment creation
4. Review the foundational concepts in [Grid Q-Learning](gridqlearning/)

## Navigation

Use the links above to explore each section of the project documentation. Each section contains detailed explanations, implementation details, and practical examples.

---

*This project demonstrates the cutting-edge integration of Large Language Models with reinforcement learning, showcasing how AI can enhance traditional RL approaches through intelligent initialization and environment generation.*