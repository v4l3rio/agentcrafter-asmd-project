# AgentCrafter: Advanced Software Modeling and Design Project

Welcome to **AgentCrafter**, a comprehensive framework for Reinforcement Learning (RL) with advanced multi-agent capabilities and experimental Large Language Model (LLM) integration.

## Development Roadmap

This project was developed following a structured, incremental approach that demonstrates methodical progression from basic RL concepts to advanced multi-agent systems:

### Foundation Phase
1. **Grid Q-Learning** - Core reinforcement learning implementation
2. **Visual Q-Learning** - Enhanced visualization and user experience  
3. **First DSL Version** - Domain-specific language foundation

### Advanced Features Phase
1. **MARL Extension** - Multi-agent reinforcement learning capabilities
2. **DSL Adaptation** - Enhanced DSL for multi-agent scenarios
3. **QTable LLM** - AI-powered Q-table generation
4. **Wall LLM** - AI-powered environment design

### Testing and Validation Phase
The following tests were developed, before, during, and after development incrementally:
1. **Unit Tests** - Comprehensive testing framework
2. **Gherkin/Cucumber Tests** - Behavior-driven development testing
3. **ScalaCheck Tests** - Property-based testing framework

This roadmap explicitly shows how each component builds upon previous work, creating a robust foundation for complex multi-agent reinforcement learning scenarios.

## Documentation Structure

The documentation follows the development journey, explaining how each component works:

### [Q-Learning](qlearning/)
**Foundation Analysis and Implementation**
- Analysis of core learning algorithms and grid-based environments
- Grid Q-Learning: basic implementation and environment dynamics
- Visual Q-Learning: enhanced user experience and visualization
- First DSL version: design decisions and syntax foundation

*This section establishes the fundamental concepts and architecture that support all advanced features.*

### [MARL](marl/)
**Multi-Agent Extensions and Coordination**
- What extends beyond basic Q-Learning: coordination mechanisms and shared environments
- Multi-agent implementation: Many QTable, agent coordination and state management
- DSL additions: enhanced syntax for multi-agent scenarios, triggers, and complex configurations

*This section demonstrates how the foundation scales to support multiple coordinated agents in complex environments.*

### [LLM](llm/)
**AI-Powered Enhancement Features**
- LLM Q-Learning: AI-generated Q-tables for intelligent initialization
- Wall LLM: natural language environment design and generation
- Prompt engineering: how prompts are structured and data is processed

*This section covers experimental AI integration, including both successes and limitations.*

### [Grammar](grammar/)
**Complete DSL Specification**
Comprehensive syntax reference and language specification.

### [Conclusions](conclusions/)
**Project Outcomes and Insights**
- **MARL Success**: Why multi-agent coordination works effectively
- **LLM Limitations**: Why AI integration faces significant challenges and what this teaches us

## Key Results Summary

**✅ MARL Works Effectively**
- Reliable multi-agent coordination in complex environments
- Seamless DSL integration with advanced features

**⚠️ LLM Integration Has Limitations**
- Technical integration succeeds but practical benefits are limited
- LLMs struggle to understand information in a spatial environment with multiple entities interacting within it
- Traditional algorithmic approaches often provide better reliability

## Getting Started

To understand the complete development journey:

1. **[Q-Learning](qlearning/)** - Understand the foundational architecture and core concepts
2. **[MARL](marl/)** - See how multi-agent features build naturally on the foundation
3. **[LLM](llm/)** - Explore experimental AI features and their real-world limitations
4. **[Grammar](grammar/)** - Reference the complete DSL specification
5. **[Conclusions](conclusions/)** - Learn from project outcomes and insights
