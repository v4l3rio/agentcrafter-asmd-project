# Visualizers

The AgentCrafter framework provides comprehensive visualization capabilities for both single-agent Q-Learning and multi-agent reinforcement learning simulations. The visualization system offers real-time monitoring, debugging tools, and interactive environments.

## Overview

The visualization system consists of two main components:

1. **Unified GUI Visualizer** - A Swing-based graphical interface for real-time simulation visualization
2. **Console Visualizer** - An ASCII-based utility for debugging and analysis in console environments

## Key Features

### Real-time Visualization
- Live agent movement and state updates
- Dynamic wall rendering (including openable walls)
- Goal and trigger visualization
- Multi-agent coordination display

### Interactive Environment
- Configurable cell size and update delay
- Comprehensive information display
- Episode and reward tracking
- Q-value debugging for single-agent scenarios

### Multi-layer Support
- Grid world environments with walls
- Agent trajectories and visit patterns
- Switch/trigger mechanisms
- Dynamic environment changes

## Implementation Architecture

### Unified Visualizer (`Visualizer.scala`)

The main visualization component that supports both single-agent and multi-agent scenarios:

```scala
class Visualizer(
  windowTitle: String,
  rows: Int,
  cols: Int,
  cell: Int = 60,
  delayMs: Int = 100
)
```

**Key Features:**
- Swing-based GUI with configurable cell size
- Support for static and dynamic walls
- Multiple agent visualization with distinct colors
- Real-time state updates and episode tracking
- Q-value debugging capabilities

**Supported Elements:**
- Grid environments with customizable dimensions
- Wall rendering (static and openable)
- Agent positions and movements
- Goal states and triggers
- Reward and episode statistics

### Console Visualizer (`ConsoleVisualizer.scala`)

A lightweight ASCII-based visualization utility for debugging:

```scala
object ConsoleVisualizer {
  def visualizeTrajectory(trajectory: List[State], gridWorld: GridWorld): String
  def printGrid(gridWorld: GridWorld, agentPos: State): Unit
}
```

**Key Features:**
- Character-based grid representation
- Visit order tracking
- Start/goal position marking
- Wall and obstacle visualization
- Trajectory analysis

**Character Mapping:**
- `#` - Walls and obstacles
- `S` - Start position
- `G` - Goal position
- `1-9` - Visit order (numbered)
- `.` - Unvisited cells
- `A` - Current agent position

## Usage Examples

### GUI Visualization

```scala
import agentcrafter.marl.dsl.*

simulation:
  grid: 10 x 10
  // ... simulation configuration
  WithGUI >> true
  Delay >> 100
```

### Console Debugging

```scala
import agentcrafter.marl.visualizers.ConsoleVisualizer

// Print current grid state
ConsoleVisualizer.printGrid(gridWorld, agentPosition)

// Visualize agent trajectory
val trajectoryViz = ConsoleVisualizer.visualizeTrajectory(path, gridWorld)
println(trajectoryViz)
```

## Configuration Options

### GUI Visualizer Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `windowTitle` | String | - | Window title for the visualization |
| `rows` | Int | - | Number of grid rows |
| `cols` | Int | - | Number of grid columns |
| `cell` | Int | 60 | Size of each grid cell in pixels |
| `delayMs` | Int | 100 | Delay between updates in milliseconds |

### DSL Integration

| Property | Type | Description |
|----------|------|-------------|
| `WithGUI` | Boolean | Enable/disable graphical interface |
| `Delay` | Int | Update delay in milliseconds |
| `ShowAfter` | Int | Episodes after which to show GUI |

## Advanced Features

### Multi-Agent Coordination
- Distinct colors for different agents
- Simultaneous agent movement tracking
- Trigger activation visualization
- Cooperative behavior monitoring

### Debug Information
- Q-value display for single-agent scenarios
- Exploration mode indicators
- Episode and step counters
- Reward accumulation tracking

### Dynamic Environment Support
- Real-time wall opening/closing
- Trigger state changes
- Environment modification visualization
- Interactive element highlighting

## Integration with Learning Components

The visualization system seamlessly integrates with:

- **Grid Q-Learning**: Basic single-agent visualization
- **MARL Framework**: Multi-agent coordination display
- **LLM Q-Learning**: AI-assisted learning visualization
- **Visual Q-Learning**: Enhanced rendering and analytics

## Best Practices

### Performance Optimization
- Use appropriate delay settings for smooth visualization
- Consider disabling GUI for training phases
- Enable visualization only after initial learning

### Debugging Workflow
1. Start with console visualization for quick debugging
2. Use GUI visualization for detailed analysis
3. Adjust delay and cell size for optimal viewing
4. Monitor Q-values and exploration patterns

### Multi-Agent Scenarios
- Use distinct agent names for clear identification
- Monitor trigger activations and coordination
- Track individual and collective rewards
- Analyze cooperation patterns

## Technical Implementation

### Rendering Pipeline
1. **State Collection**: Gather current simulation state
2. **Element Rendering**: Draw grid, walls, agents, and triggers
3. **Information Display**: Show statistics and debug info
4. **Update Cycle**: Refresh display based on delay settings

### Thread Safety
- Visualization updates are synchronized with simulation steps
- GUI components are properly managed in EDT
- State consistency maintained across updates

### Memory Management
- Efficient rendering with minimal object allocation
- Proper cleanup of visualization resources
- Optimized for long-running simulations