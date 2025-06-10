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

### LLM Integration Extensions
- **LLM Q-Table Generation**: AI-powered Q-Table initialization using GPT models
- **LLM Wall Generation**: Automatic maze and environment generation from natural language descriptions
- **Intelligent Bootstrapping**: Pre-computed optimal policies to accelerate learning

## Architecture Documentation

For a comprehensive understanding of the system architecture, see the **[Architecture Documentation](architecture.md)** which includes detailed PlantUML diagrams showing:

- **System Overview**: Complete architecture with all major components and relationships
- **DSL Configuration Flow**: How the Domain-Specific Language processes user configurations
- **LLM Integration**: Large Language Model services and API integration architecture

Each documentation section also includes relevant architecture diagrams specific to that component.

## Documentation Sections

### 1. [Multi-Agent Reinforcement Learning (MARL)](marl/)
The core framework implementing sophisticated multi-agent systems with coordination mechanisms, triggers, and complex environment interactions. This is the foundation of the entire project.

### 2. [LLM Q-Learning Extensions](llmqlearning/)
Innovative extensions that integrate Large Language Models for intelligent Q-Table generation and environment bootstrapping. This demonstrates how AI can enhance traditional reinforcement learning.

### 3. [LLM Wall Generation](wallsfromllm/)
A unique feature that allows automatic generation of maze layouts and wall configurations using LLMs directly within the simulation DSL, enabling endless variety in environment design.

### 4. [Grid Q-Learning](gridqlearning/)
The foundational implementation showcasing basic Q-Learning concepts. This serves as a reference for understanding the evolution to more advanced approaches.

### 5. [Visual Q-Learning](visualqlearning/)
Enhanced implementation with visualization capabilities and improved learning mechanisms, bridging basic and advanced approaches.

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