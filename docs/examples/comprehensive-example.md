# Comprehensive AgentCrafter Example

This example demonstrates the complete AgentCrafter DSL syntax with all current features, including multi-agent coordination, LLM integration, and visualization.

## Complete Multi-Agent Scenario with LLM Integration

```scala
package agentcrafter.examples.comprehensive

import agentcrafter.marl.dsl.*
import agentcrafter.llmqlearning.LLMQLearning

/**
 * Comprehensive example showcasing all AgentCrafter features:
 * - Multi-agent coordination
 * - LLM Q-table generation
 * - LLM wall generation
 * - Complex trigger systems
 * - Advanced visualization
 */
object ComprehensiveExample extends App with LLMQLearning:

  import AgentProperty.*
  import LearnerProperty.*
  import SimulationProperty.*
  import TriggerProperty.*
  import WallLLMProperty.*
  import LineProperty.*

  simulation:
    // LLM Configuration for Q-table generation
    useLLM:
      Enabled >> true
      Model >> "gpt-4o"
    
    // Grid setup
    grid:
      15 x 20
    
    // LLM-generated environment
    wallsFromLLM:
      Model >> "gpt-4o"
      Prompt >>
        """
        Create a complex multi-room environment with:
        - A central hub connected to 4 different rooms
        - Each room should have a different layout (maze, open space, narrow corridors, chamber)
        - Include strategic chokepoints between rooms
        - Design for 3 agents with different starting positions
        - Ensure multiple paths exist between start and goal areas
        """
    
    // Additional manual walls for specific game mechanics
    walls:
      line:
        Direction >> "horizontal"
        From >> (7, 8)
        To >> (7, 12)
      
      block >> (5, 15)
      block >> (9, 15)
    
    // Agent 1: Key Collector
    agent:
      Name >> "KeyCollector"
      Start >> (1, 1)
      withLearner:
        Alpha >> 0.12
        Gamma >> 0.95
        Eps0 >> 0.8
        EpsMin >> 0.05
        Warm >> 2_000
        Optimistic >> 0.6
      Goal >> (7, 5)
      onGoal:
        Give >> 50.0
        OpenWall >> (7, 10)  // Opens passage for DoorOpener
        EndEpisode >> false
    
    // Agent 2: Door Opener
    agent:
      Name >> "DoorOpener"
      Start >> (1, 18)
      withLearner:
        Alpha >> 0.15
        Gamma >> 0.9
        Eps0 >> 0.7
        EpsMin >> 0.1
        Warm >> 1_500
        Optimistic >> 0.4
      Goal >> (7, 10)
      onGoal:
        Give >> 75.0
        OpenWall >> (9, 15)  // Opens final passage for TreasureHunter
        EndEpisode >> false
    
    // Agent 3: Treasure Hunter (final goal)
    agent:
      Name >> "TreasureHunter"
      Start >> (13, 10)
      withLearner:
        Alpha >> 0.1
        Gamma >> 0.99
        Eps0 >> 0.6
        EpsMin >> 0.05
        Warm >> 3_000
        Optimistic >> 0.8
      Goal >> (13, 18)
      onGoal:
        Give >> 200.0
        EndEpisode >> true  // Mission complete!
    
    // Simulation parameters
    Penalty >> -2.0
    Episodes >> 25_000
    Steps >> 800
    ShowAfter >> 20_000
    Delay >> 80
    WithGUI >> true
```

## Key Features Demonstrated

### 1. LLM Integration
- **Q-table Generation**: Uses GPT-4o to bootstrap intelligent initial policies
- **Environment Design**: Natural language description generates complex maze layouts
- **Seamless Integration**: LLM features work transparently with traditional RL

### 2. Multi-Agent Coordination
- **Sequential Dependencies**: Agents must cooperate in a specific order
- **Trigger Chains**: Each agent's success enables the next agent's progress
- **Shared Environment**: All agents operate in the same dynamic world

### 3. Advanced DSL Features
- **Property Syntax**: All properties use the `>>` operator for type-safe assignment
- **Flexible Configuration**: Mix of LLM-generated and manually defined elements
- **Comprehensive Parameters**: Full control over learning and simulation behavior

### 4. Visualization System
- **Real-time Monitoring**: Live updates of agent positions and environment changes
- **Performance Tracking**: Episode progress and reward accumulation
- **Interactive Display**: GUI with configurable update rates

## DSL Syntax Reference

### Simulation Properties
```scala
Penalty >> <Double>      // Step penalty
Episodes >> <Int>        // Number of training episodes
Steps >> <Int>           // Maximum steps per episode
ShowAfter >> <Int>       // Episodes before showing GUI
Delay >> <Int>           // Milliseconds between updates
WithGUI >> <Boolean>     // Enable graphical interface
```

### Agent Properties
```scala
Name >> <String>         // Agent identifier
Start >> (<Int>, <Int>)  // Starting coordinates
Goal >> (<Int>, <Int>)   // Target coordinates
```

### Learner Properties
```scala
Alpha >> <Double>        // Learning rate
Gamma >> <Double>        // Discount factor
Eps0 >> <Double>         // Initial exploration rate
EpsMin >> <Double>       // Minimum exploration rate
Warm >> <Int>            // Warmup episodes
Optimistic >> <Double>   // Optimistic initialization
```

### Trigger Properties
```scala
Give >> <Double>         // Bonus reward
OpenWall >> (<Int>, <Int>) // Wall to remove
EndEpisode >> <Boolean>  // End episode flag
```

### Wall Properties
```scala
// Line walls
Direction >> "horizontal" | "vertical"
From >> (<Int>, <Int>)
To >> (<Int>, <Int>)

// Block walls
block >> (<Int>, <Int>)
```

### LLM Properties
```scala
// Q-table generation
Enabled >> <Boolean>
Model >> <String>

// Wall generation
Model >> <String>
Prompt >> <String>
```

## Expected Behavior

1. **Initialization**: LLM generates both Q-tables and environment layout
2. **Phase 1**: KeyCollector learns to reach the key location
3. **Phase 2**: DoorOpener waits for passage to open, then proceeds to switch
4. **Phase 3**: TreasureHunter waits for final passage, then reaches treasure
5. **Completion**: Episode ends when TreasureHunter reaches the goal

This example demonstrates the full power of AgentCrafter's declarative DSL, combining traditional reinforcement learning with cutting-edge LLM integration for both intelligent initialization and creative environment design.