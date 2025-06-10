# AgentCrafter System Architecture

This document provides a comprehensive overview of the AgentCrafter system architecture, illustrating the key components and their relationships through PlantUML diagrams.

## System Overview

AgentCrafter is built on a layered architecture that separates concerns and provides flexibility for different types of reinforcement learning scenarios.

@startuml AgentCrafter System Architecture
!theme plain
title AgentCrafter - High-Level System Architecture

package "DSL Layer" {
  interface SimulationDSL {
    +simulation()
    +grid()
    +agent()
    +walls()
    +asciiWalls()
    +wallsFromLLM()
  }
  
  class LLMQLearning {
    +useLLM()
    -llmConfig: LLMConfig
  }
  
  class Properties {
    +SimulationProperty
    +AgentProperty
    +LearnerProperty
    +WallProperty
  }
}

package "Builder Pattern" {
  class SimulationBuilder {
    -rows, cols: Int
    -walls: Set[State]
    -agents: Map[String, AgentSpec]
    -triggers: Buffer[Trigger]
    +build(): Unit
  }
  
  class AgentBuilder {
    +withLearner()
    +onGoal()
    +build(): AgentSpec
  }
  
  class TriggerBuilder {
    +openWall()
    +endEpisode()
    +give()
  }
  
  class WallLineBuilder {
    +direction()
    +from()
    +to()
  }
}

package "Domain Model" {
  class AgentSpec {
    +id: String
    +start: State
    +goal: State
    +learner: Learner
    +triggers: List[Trigger]
  }
  
  class WorldSpec {
    +rows, cols: Int
    +walls: Set[State]
    +agents: Map[String, AgentSpec]
    +triggers: List[Trigger]
  }
  
  abstract class Effect
  class OpenWall extends Effect
  class EndEpisode extends Effect
  class Reward extends Effect
  
  class Trigger {
    +who: String
    +at: State
    +effects: List[Effect]
  }
}

package "Learning Core" {
  interface Environment {
    +rows, cols: Int
    +step(state, action): StepResult
  }
  
  abstract class Learner {
    +learn(episodes: Int): Unit
    +greedyEpisode(): EpisodeOutcome
  }
  
  class QLearner {
    -qTable: Map[(State, Action), Double]
    -learningParameters: LearningParameters
    +learn(episodes: Int): Unit
    +chooseAction(state: State, epsilon: Double): Action
  }
  
  class GridWorld {
    +walls: Set[State]
    +stepPenalty: Double
    +step(state: State, action: Action): StepResult
  }
}

package "LLM Integration" {
  class LLMApiClient {
    +generateQTable(prompt: String): Option[String]
    +generateWalls(prompt: String): Option[String]
  }
  
  class LLMQTableService {
    +loadQTableFromLLM(builder: SimulationBuilder): Unit
  }
  
  class LLMWallService {
    +generateWallsFromPrompt(prompt: String): Set[State]
  }
}

package "Visualization" {
  class Visualizer {
    +render(world: WorldSpec, agents: Map[String, Agent]): Unit
    +showQValues(qTable: Map[(State, Action), Double]): Unit
  }
  
  class QTableVisualizer {
    +displayQTable(qTable: Map[(State, Action), Double]): Unit
    +highlightOptimalPath(start: State, goal: State): Unit
  }
}

package "Execution" {
  class Runner {
    +run(simulation: WorldSpec): Unit
    +runEpisode(): EpisodeOutcome
  }
  
  class EpisodeManager {
    +executeEpisode(agents: List[Agent]): EpisodeOutcome
    +handleTriggers(agent: Agent, state: State): Unit
  }
  
  class Simulation {
    +world: WorldSpec
    +agents: Map[String, Agent]
    +currentEpisode: Int
    +step(): Unit
  }
}

' Relationships
SimulationDSL --> SimulationBuilder
SimulationBuilder --> AgentBuilder
SimulationBuilder --> TriggerBuilder
SimulationBuilder --> WallLineBuilder
SimulationBuilder --> WorldSpec
AgentBuilder --> AgentSpec
TriggerBuilder --> Trigger
AgentSpec --> Learner
QLearner --> Environment
GridWorld --> Environment
LLMQLearning --> LLMApiClient
LLMQTableService --> LLMApiClient
LLMWallService --> LLMApiClient
Runner --> Simulation
Simulation --> WorldSpec
Simulation --> Agent
Visualizer --> WorldSpec
QTableVisualizer --> QLearner

@enduml

## DSL Configuration Flow

The following diagram shows how user configurations flow through the system:

@startuml DSL Flow and Builder Pattern
!theme plain
title AgentCrafter DSL - Configuration Flow

actor User
participant "SimulationDSL" as DSL
participant "SimulationWrapper" as Wrapper
participant "SimulationBuilder" as SB
participant "AgentBuilder" as AB
participant "Properties" as Props
participant "WorldSpec" as World
participant "Runner" as Runner

User -> DSL : simulation { ... }
activate DSL

DSL -> Wrapper : create SimulationWrapper
activate Wrapper
DSL -> SB : new SimulationBuilder
activate SB

User -> DSL : grid: 10 x 10
DSL -> SB : grid(10, 10)

User -> DSL : agent: { ... }
DSL -> AB : new AgentBuilder
activate AB

User -> Props : Name >> "Agent1"
Props -> AB : setName("Agent1")

User -> Props : Start >> (0, 0)
Props -> AB : setStart(State(0, 0))

User -> DSL : withLearner: { ... }
DSL -> AB : withLearner()

User -> Props : Alpha >> 0.1
Props -> AB : setAlpha(0.1)

User -> Props : Gamma >> 0.9
Props -> AB : setGamma(0.9)

User -> DSL : onGoal: { ... }
DSL -> AB : onGoal()

User -> Props : Give >> 100
Props -> AB : addReward(100)

User -> Props : EndEpisode >> true
Props -> AB : addEndEpisode()

AB -> SB : addAgent(agentSpec)
deactivate AB

User -> DSL : walls: { ... }
DSL -> SB : addWalls()

User -> Props : Episodes >> 1000
Props -> SB : setEpisodes(1000)

User -> Props : WithGUI >> true
Props -> SB : withGUI(true)

DSL -> SB : build()
SB -> World : create WorldSpec
activate World
SB -> Runner : create and start
activate Runner

@enduml

## LLM Integration Architecture

The system provides comprehensive LLM integration capabilities:

@startuml LLM Integration Architecture
!theme plain
title AgentCrafter - LLM Integration Architecture

package "DSL Layer" {
  trait LLMQLearning {
    -llmConfig: LLMConfig
    +useLLM(block: LLMConfig ?=> Unit): Unit
    +simulation(block: SimulationWrapper ?=> Unit): Unit
  }
  
  class LLMConfig {
    +enabled: Boolean
    +model: String
    +wallsEnabled: Boolean
    +wallsModel: String
    +wallsPrompt: String
  }
  
  enum LLMProperty {
    Enabled
    Model
    WallsEnabled
    WallsModel
    WallsPrompt
    +>>(value: T): Unit
  }
}

package "LLM Services" {
  class LLMApiClient {
    -apiKey: String
    -baseUrl: String
    +generateQTable(prompt: String, model: String): Option[String]
    +generateWalls(prompt: String, model: String): Option[String]
    +makeApiCall(prompt: String, model: String): String
    -buildRequestPayload(prompt: String, model: String): String
    -parseResponse(response: String): Option[String]
  }
  
  class LLMQTableService {
    +loadQTableFromLLM(builder: SimulationBuilder, model: String, filePath: String): Option[String]
    +loadQTableIntoAgents(builder: SimulationBuilder, qTableJson: String): Unit
    -generateQTablePrompt(world: WorldSpec): String
    -parseQTableResponse(response: String): Map[(State, Action), Double]
  }
  
  class LLMWallService {
    +generateWallsFromPrompt(prompt: String, model: String, rows: Int, cols: Int): Set[State]
    -parseWallResponse(response: String, rows: Int, cols: Int): Set[State]
    -validateWallConfiguration(walls: Set[State], rows: Int, cols: Int): Set[State]
  }
}

package "Prompt Management" {
  class PromptTemplate {
    +qTablePrompt: String
    +wallGenerationPrompt: String
    +scenarioPrompt: String
    +generatePrompt(template: String, params: Map[String, Any]): String
  }
  
  class PromptBuilder {
    +buildQTablePrompt(world: WorldSpec): String
    +buildWallPrompt(requirements: String, dimensions: (Int, Int)): String
    +addContext(context: String): PromptBuilder
    +addConstraints(constraints: List[String]): PromptBuilder
  }
}

package "External API" {
  class OpenAIClient {
    +chat(messages: List[Message], model: String): String
    +completion(prompt: String, model: String): String
    -handleRateLimit(): Unit
    -retryOnFailure(request: () => String): String
  }
  
  class APIResponse {
    +content: String
    +usage: TokenUsage
    +model: String
    +finishReason: String
  }
}

package "Integration Points" {
  class LLMSimulationWrapper {
    +withLLMQTable(): Unit
    +withLLMWalls(): Unit
    +enableLLMFeatures(): Unit
  }
  
  class ConfigurationValidator {
    +validateLLMConfig(config: LLMConfig): Boolean
    +validateAPIKey(): Boolean
    +validateModelAvailability(model: String): Boolean
  }
}

' Relationships
LLMQLearning --> LLMConfig
LLMQLearning --> LLMQTableService
LLMQLearning --> LLMWallService
LLMQTableService --> LLMApiClient
LLMWallService --> LLMApiClient
LLMApiClient --> OpenAIClient
LLMApiClient --> PromptBuilder
PromptBuilder --> PromptTemplate
OpenAIClient --> APIResponse
LLMSimulationWrapper --> ConfigurationValidator

note right of LLMQLearning : "Extends SimulationDSL\nwith LLM capabilities"
note bottom of LLMApiClient : "Handles all external\nAPI communication"
note left of PromptBuilder : "Generates context-aware\nprompts for different tasks"

@enduml

## Design Principles

### Separation of Concerns
- DSL syntax is separate from implementation
- Learning algorithms are independent of visualization
- LLM integration is optional and modular

### Layered Architecture
The system follows a layered architecture:
1. **DSL Layer**: User-facing domain-specific language
2. **Builder Layer**: Fluent API for constructing simulations
3. **Domain Layer**: Core business objects and logic
4. **Service Layer**: LLM integration and external services
5. **Execution Layer**: Runtime simulation management
6. **Visualization Layer**: GUI and rendering components

### Key Patterns
- **Builder Pattern**: Used extensively for fluent DSL construction
- **Strategy Pattern**: Different learner implementations
- **Observer Pattern**: Visualization updates
- **Factory Pattern**: Agent and simulation creation
- **Service Layer**: LLM integration abstraction

## Usage Guidelines

### For Developers
1. Start with the **System Overview** to understand the big picture
2. Study the **DSL Flow** to understand how user configurations are processed
3. Examine the **Learning Architecture** for reinforcement learning details
4. Review **Multi-Agent Coordination** for multi-agent scenarios
5. Explore **LLM Integration** for AI enhancement features

### For Contributors
- Use these diagrams as reference when adding new features
- Update diagrams when making architectural changes
- Ensure new components fit within the existing layered structure

### For Users
- The **DSL Flow** diagram helps understand the configuration syntax
- **Multi-Agent Coordination** explains how agents interact
- **LLM Integration** shows available AI enhancement features

---

*These diagrams are living documentation and should be updated as the system evolves.*