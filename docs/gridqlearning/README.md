# Grid Q-Learning

This section documents the first approach to implementing reinforcement learning in our project, using a grid-based Q-Learning algorithm.

## Overview

Grid Q-Learning represents the foundational implementation of our reinforcement learning system. This approach uses a discrete grid environment where agents learn optimal policies through Q-Learning, a model-free reinforcement learning algorithm.

## Key Concepts

### Q-Learning Algorithm
Q-Learning is a value-based learning algorithm that learns the quality of actions, telling an agent what action to take under what circumstances. It does not require a model of the environment and can handle problems with stochastic transitions and rewards.

### Grid Environment
The environment is represented as a discrete grid where:
- Each cell represents a state
- Agents can move in four directions (up, down, left, right)
- Rewards are associated with specific states or state-action pairs
- The goal is to learn the optimal path from any starting position to the target

## Implementation Details

### State Representation
- States are represented as coordinates (x, y) in the grid
- Each state has an associated Q-value for each possible action
- The Q-table stores the learned values for state-action pairs

### Action Space
- **UP**: Move one cell upward
- **DOWN**: Move one cell downward
- **LEFT**: Move one cell to the left
- **RIGHT**: Move one cell to the right

### Learning Process
1. **Initialization**: Q-values are initialized (typically to zero)
2. **Exploration**: Agent explores the environment using an ε-greedy policy
3. **Learning**: Q-values are updated using the Q-Learning update rule:
   ```
   Q(s,a) ← Q(s,a) + α[r + γ max Q(s',a') - Q(s,a)]
   ```
   Where:
   - α is the learning rate
   - γ is the discount factor
   - r is the immediate reward
   - s' is the next state

## Parameters

- **Learning Rate (α)**: Controls how much new information overrides old information
- **Discount Factor (γ)**: Determines the importance of future rewards
- **Exploration Rate (ε)**: Balances exploration vs exploitation
- **Grid Size**: Dimensions of the environment
- **Episodes**: Number of training iterations

## Advantages

- **Simplicity**: Easy to understand and implement
- **Guaranteed Convergence**: Under certain conditions, Q-Learning converges to optimal policy
- **Model-Free**: No need to know the environment dynamics
- **Foundation**: Provides a solid base for more complex algorithms

## Limitations

- **Scalability**: Q-table grows exponentially with state space size
- **Discrete States**: Limited to discrete state representations
- **Memory Requirements**: Large state spaces require significant memory
- **Slow Convergence**: Can be slow to converge in large environments

## Results and Analysis

This initial implementation provided valuable insights into:
- Basic reinforcement learning principles
- The importance of parameter tuning
- Trade-offs between exploration and exploitation
- Foundation for more advanced approaches

## Next Steps

The lessons learned from this grid-based approach led to the development of the [Visual Q-Learning](../visualqlearning/) implementation, which addresses some of the limitations identified in this phase.

---

*This implementation serves as the cornerstone of our reinforcement learning project, establishing the fundamental concepts that are built upon in subsequent phases.*