# Grid Q-Learning Foundation

The **Grid Q-Learning** component represents the foundational layer of the AgentCrafter framework, providing the core reinforcement learning infrastructure that underlies all advanced features. This module establishes the fundamental grid-based environment system, basic Q-learning algorithms, and essential simulation mechanics that power the entire MARL ecosystem.

## Overview

Grid Q-Learning serves as the bedrock of the AgentCrafter project, implementing classical reinforcement learning concepts in a robust, extensible framework. While the project has evolved to encompass advanced multi-agent systems, visual learning, and LLM integration, this foundational layer remains critical for understanding the core principles and providing backward compatibility.

## Architecture Overview

The Grid Q-Learning architecture forms the foundation of all AgentCrafter simulations, providing the core reinforcement learning infrastructure:

@startuml Grid Q-Learning Architecture
!theme plain
title AgentCrafter - Grid Q-Learning Foundation

package "Core Environment" {
  class GridEnvironment {
    +width: Int
    +height: Int
    +walls: Set[Position]
    +agents: Map[String, Agent]
    +triggers: Map[String, Trigger]
    +isValidPosition(pos: Position): Boolean
    +getNeighbors(pos: Position): List[Position]
    +applyAction(agent: Agent, action: Action): EnvironmentState
  }
  
  class Position {
    +x: Int
    +y: Int
    +distance(other: Position): Double
    +neighbors(): List[Position]
  }
  
  enum Action {
    Up
    Down
    Left
    Right
    Stay
  }
}

package "Q-Learning Core" {
  class QLearningAgent {
    +learningRate: Double
    +discountFactor: Double
    +explorationRate: Double
    -qTable: Map[(State, Action), Double]
    +selectAction(state: State): Action
    +updateQValue(state: State, action: Action, reward: Double, nextState: State): Unit
    +exploreAction(state: State): Action
    +exploitAction(state: State): Action
  }
  
  class QTable {
    -values: Map[(State, Action), Double]
    +getValue(state: State, action: Action): Double
    +setValue(state: State, action: Action, value: Double): Unit
    +getBestAction(state: State): Action
    +export(): String
    +import(data: String): Unit
  }
  
  class State {
    +position: Position
    +agentId: String
    +environmentState: Map[String, Any]
    +equals(other: State): Boolean
    +hashCode(): Int
  }
}

package "Learning Process" {
  class Episode {
    +steps: List[Step]
    +totalReward: Double
    +isComplete: Boolean
    +addStep(step: Step): Unit
  }
  
  class Step {
    +state: State
    +action: Action
    +reward: Double
    +nextState: State
    +timestamp: Long
  }
  
  class RewardFunction {
    +calculateReward(state: State, action: Action, nextState: State): Double
    +goalReward: Double
    +stepPenalty: Double
    +wallPenalty: Double
  }
}

package "Simulation Control" {
  class Simulation {
    +environment: GridEnvironment
    +agents: List[QLearningAgent]
    +maxEpisodes: Int
    +maxStepsPerEpisode: Int
    +run(): List[Episode]
    +step(): SimulationState
  }
  
  class SimulationState {
    +currentEpisode: Int
    +currentStep: Int
    +agentPositions: Map[String, Position]
    +isTerminal: Boolean
  }
}

' Relationships
GridEnvironment --> Position
GridEnvironment --> Action
QLearningAgent --> QTable
QLearningAgent --> State
QLearningAgent --> Action
State --> Position
Episode --> Step
Step --> State
Step --> Action
Simulation --> GridEnvironment
Simulation --> QLearningAgent
Simulation --> Episode
Simulation --> RewardFunction

note right of QLearningAgent : "Implements epsilon-greedy\nexploration strategy"
note bottom of QTable : "Stores state-action\nvalue estimates"
note left of RewardFunction : "Defines learning\nobjectives and penalties"

@enduml

## Core Concepts

### Grid-Based Environment

The foundation of all AgentCrafter simulations is the discrete grid world:

```scala
case class GridEnvironment(
  width: Int,
  height: Int,
  walls: Set[Position],
  agents: Map[String, Agent],
  triggers: Map[String, Trigger]
) {
  def isValidPosition(pos: Position): Boolean
  def getNeighbors(pos: Position): List[Position]
  def applyAction(agent: Agent, action: Action): EnvironmentState
}
```

### Q-Learning Algorithm

Classical Q-learning implementation with configurable parameters:

```scala
class QLearningAgent(
  val learningRate: Double = 0.1,
  val discountFactor: Double = 0.9,
  val explorationRate: Double = 0.1
) {
  private var qTable: Map[(State, Action), Double] = Map()
  
  def selectAction(state: State): Action = {
    if (Random.nextDouble() < explorationRate) {
      exploreAction(state)
    } else {
      exploitAction(state)
    }
  }
  
  def updateQValue(state: State, action: Action, reward: Double, nextState: State): Unit = {
    val currentQ = qTable.getOrElse((state, action), 0.0)
    val maxNextQ = getMaxQValue(nextState)
    val newQ = currentQ + learningRate * (reward + discountFactor * maxNextQ - currentQ)
    qTable = qTable.updated((state, action), newQ)
  }
}
```

### State Representation

Simple but effective state encoding for grid environments:

```scala
case class GridState(
  agentPosition: Position,
  goalPosition: Position,
  wallConfiguration: Set[Position]
) {
  def toVector: Vector[Double] = {
    // Convert to numerical representation for learning
  }
  
  def getFeatures: Map[String, Double] = {
    Map(
      "distanceToGoal" -> calculateDistance(agentPosition, goalPosition),
      "wallDensity" -> calculateWallDensity(agentPosition),
      "explorationLevel" -> calculateExplorationMetric()
    )
  }
}
```

### Action Space

Fundamental movement actions in grid environments:

```scala
enum GridAction {
  case MoveUp
  case MoveDown
  case MoveLeft
  case MoveRight
  case Stay
  
  def toVector: Position = this match {
    case MoveUp => (0, -1)
    case MoveDown => (0, 1)
    case MoveLeft => (-1, 0)
    case MoveRight => (1, 0)
    case Stay => (0, 0)
  }
}
```

## Implementation Architecture

### Core Framework Components

#### GridWorld Engine

```scala
class GridWorldEngine {
  def initializeEnvironment(config: GridConfig): GridEnvironment
  def stepSimulation(environment: GridEnvironment, actions: Map[String, Action]): SimulationResult
  def checkTerminalConditions(environment: GridEnvironment): Boolean
  def calculateRewards(environment: GridEnvironment, actions: Map[String, Action]): Map[String, Double]
}
```

#### Learning Coordinator

```scala
class LearningCoordinator {
  def coordinateAgentUpdates(agents: List[QLearningAgent], experiences: List[Experience]): Unit
  def manageExploration(agents: List[QLearningAgent], episode: Int): Unit
  def trackLearningProgress(agents: List[QLearningAgent]): LearningMetrics
  def optimizeHyperparameters(performance: PerformanceMetrics): HyperparameterSet
}
```

#### Simulation Manager

```scala
class SimulationManager {
  def runEpisode(environment: GridEnvironment, agents: List[QLearningAgent]): EpisodeResult
  def runSimulation(config: SimulationConfig): SimulationResults
  def collectStatistics(results: List[EpisodeResult]): SimulationStatistics
  def exportResults(statistics: SimulationStatistics): Unit
}
```

### DSL Foundation

The grid Q-learning module provides the foundational DSL elements:

```scala
// Basic simulation structure
simulation(
  name = "BasicGridLearning",
  width = 10,
  height = 10,
  episodes = 1000
) {
  // Single agent configuration
  agent("Learner") {
    position = (0, 0)
    learningRate = 0.1
    discountFactor = 0.9
    explorationRate = 0.3
  }
  
  // Simple wall configuration
  walls {
    line(from = (2, 2), to = (2, 7))
    line(from = (7, 1), to = (7, 5))
    block(at = (5, 5))
  }
  
  // Goal definition
  trigger("Goal") {
    position = (9, 9)
    reward = 100
    terminal = true
  }
}
```

## Key Features

### Robust Learning Algorithms

- **Classical Q-Learning**: Well-tested implementation of the fundamental algorithm
- **Epsilon-Greedy Exploration**: Balanced exploration-exploitation strategy
- **Experience Replay**: Optional experience replay for improved learning stability
- **Adaptive Learning Rates**: Dynamic adjustment of learning parameters

### Flexible Environment Configuration

- **Variable Grid Sizes**: Support for environments from 5x5 to 100x100+
- **Wall Configurations**: Flexible wall placement and obstacle definition
- **Multiple Goals**: Support for multiple objectives and reward structures
- **Dynamic Environments**: Ability to modify environments during learning

### Performance Optimization

- **Efficient State Representation**: Optimized state encoding for fast lookup
- **Vectorized Operations**: Numpy-style operations for batch processing
- **Memory Management**: Intelligent Q-table pruning and compression
- **Parallel Processing**: Multi-threaded episode execution

### Comprehensive Monitoring

- **Learning Curves**: Real-time tracking of agent performance
- **Convergence Analysis**: Detection of learning convergence
- **Exploration Metrics**: Monitoring of exploration vs exploitation balance
- **Statistical Analysis**: Comprehensive performance statistics

## Example Scenarios

### Basic Navigation Task

```scala
// Simple navigation from start to goal
val basicNavigation = simulation(
  name = "BasicNavigation",
  width = 8,
  height = 8,
  episodes = 500
) {
  agent("Navigator") {
    position = (0, 0)
    learningRate = 0.1
    explorationRate = 0.2
  }
  
  trigger("Goal") {
    position = (7, 7)
    reward = 100
  }
}
```

### Maze Solving

```scala
// Classic maze-solving scenario
val mazeSolver = simulation(
  name = "MazeSolver",
  width = 12,
  height = 12,
  episodes = 1000
) {
  agent("MazeSolver") {
    position = (1, 1)
    learningRate = 0.15
    explorationRate = 0.3
    discountFactor = 0.95
  }
  
  // Maze walls
  walls {
    // Outer boundary
    line(from = (0, 0), to = (11, 0))
    line(from = (11, 0), to = (11, 11))
    line(from = (11, 11), to = (0, 11))
    line(from = (0, 11), to = (0, 0))
    
    // Internal maze structure
    line(from = (2, 1), to = (2, 5))
    line(from = (4, 3), to = (8, 3))
    line(from = (6, 5), to = (6, 9))
    line(from = (8, 7), to = (10, 7))
  }
  
  trigger("Exit") {
    position = (10, 10)
    reward = 200
  }
}
```

### Multi-Objective Learning

```scala
// Learning with multiple objectives
val multiObjective = simulation(
  name = "MultiObjective",
  width = 15,
  height = 15,
  episodes = 1500
) {
  agent("Collector") {
    position = (0, 0)
    learningRate = 0.12
    explorationRate = 0.25
  }
  
  // Multiple reward locations
  trigger("Treasure1") {
    position = (5, 5)
    reward = 50
    repeatable = true
  }
  
  trigger("Treasure2") {
    position = (10, 3)
    reward = 75
    repeatable = true
  }
  
  trigger("FinalGoal") {
    position = (14, 14)
    reward = 200
    terminal = true
  }
  
  // Obstacles
  walls {
    line(from = (3, 0), to = (3, 8))
    line(from = (8, 7), to = (8, 15))
    line(from = (12, 2), to = (12, 10))
  }
}
```

## Learning Parameters

### Core Hyperparameters

```scala
case class QLearningConfig(
  learningRate: Double = 0.1,        // Alpha: how much to update Q-values
  discountFactor: Double = 0.9,      // Gamma: importance of future rewards
  explorationRate: Double = 0.1,     // Epsilon: exploration vs exploitation
  explorationDecay: Double = 0.995,  // Decay rate for exploration
  minExploration: Double = 0.01      // Minimum exploration rate
) {
  def updateExploration(episode: Int): QLearningConfig = {
    val newRate = math.max(
      minExploration,
      explorationRate * math.pow(explorationDecay, episode)
    )
    copy(explorationRate = newRate)
  }
}
```

### Advanced Configuration

```scala
case class AdvancedQLearningConfig(
  baseConfig: QLearningConfig,
  experienceReplay: Boolean = false,
  replayBufferSize: Int = 10000,
  batchSize: Int = 32,
  targetNetworkUpdate: Int = 100,
  prioritizedReplay: Boolean = false,
  doubleQLearning: Boolean = false
) {
  def createAgent(): AdvancedQLearningAgent = {
    new AdvancedQLearningAgent(this)
  }
}
```

## Performance Metrics

### Learning Progress Tracking

```scala
case class LearningMetrics(
  episode: Int,
  cumulativeReward: Double,
  episodeLength: Int,
  explorationRate: Double,
  qTableSize: Int,
  convergenceMetric: Double
) {
  def learningEfficiency: Double = cumulativeReward / episodeLength
  def explorationBalance: Double = explorationRate * qTableSize
}

class MetricsCollector {
  def trackEpisode(agent: QLearningAgent, result: EpisodeResult): LearningMetrics
  def calculateMovingAverage(metrics: List[LearningMetrics], window: Int): Double
  def detectConvergence(metrics: List[LearningMetrics]): Boolean
  def generateLearningCurve(metrics: List[LearningMetrics]): Chart
}
```

### Performance Analysis

```scala
class PerformanceAnalyzer {
  def analyzeConvergence(metrics: List[LearningMetrics]): ConvergenceReport
  def compareConfigurations(results: Map[String, List[LearningMetrics]]): ComparisonReport
  def optimizeHyperparameters(searchSpace: HyperparameterSpace): OptimalConfig
  def generatePerformanceReport(simulation: SimulationResults): PerformanceReport
}
```

## Integration with Advanced Features

### Foundation for MARL

The grid Q-learning foundation provides essential infrastructure for multi-agent systems:

```scala
// Single-agent foundation
class SingleAgentQLearning extends GridQLearning {
  def learn(environment: GridEnvironment): QLearningAgent
}

// Extended for multi-agent scenarios
class MultiAgentQLearning extends SingleAgentQLearning {
  def coordinateAgents(agents: List[QLearningAgent]): CoordinationStrategy
  def handleAgentInteractions(agents: List[QLearningAgent]): InteractionResult
}
```

### Visual Learning Extension

Grid Q-learning provides the algorithmic foundation for visual enhancements:

```scala
// Core learning algorithm
class GridQLearningCore {
  def updateQTable(state: State, action: Action, reward: Double): Unit
}

// Extended with visual capabilities
class VisualQLearning extends GridQLearningCore {
  def renderEnvironment(state: State): VisualRepresentation
  def trackAgentPath(agent: Agent): PathVisualization
}
```

### LLM Integration Support

The foundational Q-table structure supports LLM-generated enhancements:

```scala
// Standard Q-table
class StandardQTable {
  private var table: Map[(State, Action), Double] = Map()
}

// LLM-enhanced Q-table
class LLMEnhancedQTable extends StandardQTable {
  def loadFromLLM(llmData: LLMQTableData): Unit
  def mergeWithGenerated(generated: Map[(State, Action), Double]): Unit
}
```

## Testing and Validation

### Unit Testing

```scala
class GridQLearningTests {
  @Test
  def testQValueUpdates(): Unit = {
    val agent = new QLearningAgent()
    val initialQ = agent.getQValue(state, action)
    agent.updateQValue(state, action, reward, nextState)
    val updatedQ = agent.getQValue(state, action)
    assert(updatedQ != initialQ)
  }
  
  @Test
  def testExplorationDecay(): Unit = {
    val config = QLearningConfig(explorationRate = 0.5)
    val updated = config.updateExploration(100)
    assert(updated.explorationRate < config.explorationRate)
  }
}
```

### Integration Testing

```scala
class GridQLearningIntegrationTests {
  @Test
  def testSimulationExecution(): Unit = {
    val simulation = createBasicSimulation()
    val results = simulation.run()
    assert(results.episodes.nonEmpty)
    assert(results.finalPerformance > 0)
  }
  
  @Test
  def testLearningConvergence(): Unit = {
    val simulation = createConvergenceTestSimulation()
    val results = simulation.run()
    assert(results.hasConverged)
  }
}
```

## Advantages and Strengths

### Algorithmic Robustness
- **Proven Algorithm**: Based on well-established Q-learning theory
- **Convergence Guarantees**: Mathematical guarantees under certain conditions
- **Stability**: Robust performance across different environments
- **Interpretability**: Clear understanding of learning process

### Implementation Quality
- **Clean Architecture**: Well-structured, maintainable codebase
- **Performance Optimized**: Efficient algorithms and data structures
- **Extensible Design**: Easy to extend and modify
- **Comprehensive Testing**: Thorough test coverage

### Educational Value
- **Clear Examples**: Easy-to-understand demonstration scenarios
- **Progressive Complexity**: From simple to advanced configurations
- **Detailed Documentation**: Comprehensive guides and explanations
- **Research Foundation**: Solid base for research and experimentation

## Limitations and Considerations

### Scalability Constraints
- **State Space Explosion**: Exponential growth with environment complexity
- **Memory Requirements**: Large Q-tables for complex environments
- **Convergence Time**: Slow learning in large state spaces
- **Discrete Actions**: Limited to discrete action spaces

### Environmental Limitations
- **Grid-Based Only**: Restricted to discrete grid environments
- **Static Environments**: Best suited for unchanging environments
- **Simple Observations**: Limited observation capabilities
- **Basic Rewards**: Simple reward structures

### Algorithmic Constraints
- **Tabular Method**: Requires explicit state enumeration
- **No Generalization**: Limited ability to generalize across states
- **Exploration Challenges**: Difficulty in sparse reward environments
- **Single-Agent Focus**: Originally designed for single-agent scenarios

## Migration to Advanced Features

### Upgrading to MARL

```scala
// Grid Q-Learning foundation
val basicAgent = agent("Learner") {
  position = (0, 0)
  learningRate = 0.1
}

// Upgraded to MARL
val marlAgent = agent("Learner") {
  position = (0, 0)
  learningRate = 0.1
  coordinationStrategy = "cooperative"
  communicationRange = 3
}
```

### Adding Visual Features

```scala
// Basic simulation
val basic = simulation(name = "Basic", episodes = 1000) { /* ... */ }

// With visual enhancements
val visual = simulation(name = "Visual", episodes = 1000) {
  visualization = true
  renderingMode = "realtime"
  pathTracking = true
  /* ... */
}
```

### LLM Integration

```scala
// Standard Q-learning
val standard = simulation(name = "Standard") {
  agent("Learner") { /* standard config */ }
}

// With LLM enhancements
val enhanced = simulation(name = "Enhanced") {
  agent("Learner") {
    qTableFromLLM {
      model = "gpt-4o"
      prompt = "Generate optimal Q-values for maze navigation"
    }
  }
}
```

## Future Evolution

### Planned Enhancements
- **Function Approximation**: Neural network-based Q-learning
- **Continuous Actions**: Support for continuous action spaces
- **Hierarchical Learning**: Multi-level learning architectures
- **Transfer Learning**: Knowledge transfer between environments

### Research Directions
- **Meta-Learning**: Learning to learn across different environments
- **Curriculum Learning**: Progressive difficulty in training
- **Multi-Task Learning**: Simultaneous learning of multiple objectives
- **Explainable AI**: Better interpretability of learned policies

---

*The Grid Q-Learning foundation provides the essential building blocks for all advanced features in AgentCrafter, ensuring a solid theoretical and practical foundation for reinforcement learning research and experimentation.*