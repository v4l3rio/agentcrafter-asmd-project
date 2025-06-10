# AgentCrafter DSL Grammar

This document provides the formal grammar specification for the AgentCrafter Domain-Specific Language (DSL) used to define multi-agent reinforcement learning simulations.

```bnf
simulation_block ::= "simulation:" simulation_config ;

simulation_config ::= llm_block? grid_block agent_block+ wall_block? ascii_wall_block? llm_wall_block? simulation_property* ;

llm_block ::= "useLLM:" llm_config_block ;

llm_config_block ::= llm_property+ ;

llm_property ::= "Enabled" ">>" boolean
               | "Model" ">>" string ;

grid_block ::= "grid:" grid_size ;

grid_size ::= number "x" number ;

agent_block ::= "agent:" agent_config ;

agent_config ::= agent_property+ ;

agent_property ::= "Name" ">>" string
                 | "Start" ">>" coordinate
                 | "Goal" ">>" coordinate
                 | learner_block
                 | trigger_block ;

learner_block ::= "withLearner:" learner_config ;

learner_config ::= learner_property+ ;

learner_property ::= "Alpha" ">>" number
                   | "Gamma" ">>" number
                   | "Eps0" ">>" number
                   | "EpsMin" ">>" number
                   | "Warm" ">>" number
                   | "Optimistic" ">>" number ;

trigger_block ::= "onGoal:" trigger_config ;

trigger_config ::= trigger_property+ ;

trigger_property ::= "Give" ">>" number
                   | "OpenWall" ">>" coordinate
                   | "EndEpisode" ">>" boolean ;

wall_block ::= "walls:" wall_config ;

wall_config ::= wall_element+ ;

wall_element ::= line_block
               | block_property ;

line_block ::= "line:" line_config ;

line_config ::= line_property+ ;

line_property ::= "Direction" ">>" direction
                | "From" ">>" coordinate
                | "To" ">>" coordinate ;

direction ::= "\"horizontal\"" | "\"vertical\"" ;

block_property ::= "block" ">>" coordinate ;

ascii_wall_block ::= "asciiWalls:" string ;

llm_wall_block ::= "wallsFromLLM:" llm_wall_config ;

llm_wall_config ::= wall_llm_property+ ;

wall_llm_property ::= "Model" ">>" string
                    | "Prompt" ">>" string ;

simulation_property ::= "Penalty" ">>" number
                      | "Episodes" ">>" number
                      | "Steps" ">>" number
                      | "ShowAfter" ">>" number
                      | "Delay" ">>" number
                      | "WithGUI" ">>" boolean ;

coordinate ::= "(" number "," number ")" ;

boolean ::= "true" | "false" ;

number ::= [0-9]+ ("." [0-9]+)? ;

string ::= "\"" [^\"]* "\"" ;
```

## Grammar Elements

### Core Structure

- **simulation_block**: The root element that defines a complete simulation configuration
- **simulation_config**: Contains all configuration blocks for the simulation

### Configuration Blocks

#### LLM Configuration
- **llm_block**: Optional block for configuring Large Language Model integration
- **llm_config_block**: Contains LLM-specific properties
- **llm_property**: Individual LLM configuration properties
  - **enabled_property**: Enables/disables LLM features
  - **model_property**: Specifies the LLM model to use

#### Grid Configuration
- **grid_block**: Defines the simulation grid dimensions
- **grid_size**: Specifies width and height using "NxM" format

#### Agent Configuration
- **agent_block**: Defines agent properties and behavior
- **agent_config**: Contains all agent-specific configurations
- **agent_property**: Individual agent properties including:
  - **name_property**: Agent identifier
  - **start_property**: Initial position coordinates
  - **goal_property**: Target position coordinates
  - **reward_property**: Reward value for reaching the goal
  - **learner_block**: Learning algorithm configuration

#### Learner Configuration
- **learner_block**: Configures the Q-Learning algorithm parameters
- **learner_config**: Contains learning-specific properties
- **learner_property**: Individual learning parameters:
  - **Alpha**: Learning rate (0.0 to 1.0)
  - **Gamma**: Discount factor (0.0 to 1.0)
  - **Eps0**: Initial exploration rate
  - **EpsMin**: Minimum exploration rate
  - **Warm**: Warm-up episodes
  - **Optimistic**: Optimistic initialization value

#### Simulation Control
- **episodes_block**: Number of training episodes
- **steps_block**: Maximum steps per episode
- **delay_block**: Delay between steps (milliseconds)
- **show_after_block**: Episodes after which to show visualization
- **gui_block**: Enable/disable graphical user interface

### Data Types

#### Primitive Types
- **coordinate**: Tuple format "(x, y)" for position specification
- **boolean**: Literal values "true" or "false"
- **number**: Integer or decimal numbers
- **string**: Quoted string literals

## Usage Examples

### Basic Simulation
```
simulation:
  grid: 10x10
  agent:
    Name >> "Agent1"
    Start >> (0, 0)
    Goal >> (9, 9)
    Reward >> 100
    withLearner:
      Alpha >> 0.1
      Gamma >> 0.9
      Eps0 >> 0.3
  Episodes >> 1000
  WithGUI >> true
```

### LLM-Enhanced Simulation
```
simulation:
  useLLM:
    Enabled >> true
    Model >> "gpt-4o"
  grid: 15x15
  agent:
    Name >> "LLMAgent"
    Start >> (1, 1)
    Goal >> (14, 14)
    withLearner:
      Alpha >> 0.15
      Gamma >> 0.95
  Episodes >> 500
  Steps >> 200
  Delay >> 50
  ShowAfter >> 100
  WithGUI >> true
```

### Multi-Agent Simulation
```
simulation:
  grid: 12x12
  agent:
    Name >> "Agent1"
    Start >> (0, 0)
    Goal >> (11, 11)
    withLearner:
      Alpha >> 0.1
      Gamma >> 0.9
  agent:
    Name >> "Agent2"
    Start >> (11, 0)
    Goal >> (0, 11)
    withLearner:
      Alpha >> 0.15
      Gamma >> 0.85
  Episodes >> 2000
  WithGUI >> true
```

## Grammar Notes

### Syntax Rules
1. **Case Sensitivity**: All keywords and property names are case-sensitive
2. **Whitespace**: Whitespace is generally ignored except within string literals
3. **Comments**: The grammar does not currently support comments
4. **Termination**: Statements are terminated by the end of the property assignment

### Property Assignment
- Uses the `>>` operator for all property assignments
- Format: `PropertyName >> Value`
- Values must match the expected type for each property

### Block Structure
- Blocks are defined using `:` after the block name
- Block content is indented (though indentation is not grammatically required)
- Nested blocks are supported (e.g., `withLearner:` within `agent:`)

### Optional Elements
- Elements marked with `?` are optional
- Elements marked with `+` require at least one occurrence
- Elements without modifiers are required exactly once

---

*This grammar specification defines the complete syntax for AgentCrafter DSL configurations, enabling precise and flexible definition of reinforcement learning simulations with optional LLM integration.*