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

#### Wall Configuration
- **wall_block**: Defines walls in the simulation
- **ascii_wall_block**: ASCII representation of walls
- **llm_wall_block**: LLM-generated walls

#### Agent Configuration
- **agent_block**: Defines agent properties and behavior
- **agent_config**: Contains all agent-specific configurations
- **agent_property**: Individual agent properties including:
  - **Name**: Agent identifier
  - **Start**: Initial position coordinates
  - **Goal**: Target position coordinates
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

#### Trigger Configuration
- **trigger_block**: Configures goal-reaching triggers
- **trigger_config**: Contains trigger-specific properties
- **trigger_property**: Individual trigger parameters:
  - **Give**: Bonus reward amount
  - **OpenWall**: Wall position to remove
  - **EndEpisode**: Whether to end the episode

#### Simulation Control
- **simulation_property**: Global simulation parameters:
  - **Penalty**: Movement penalty
  - **Episodes**: Number of training episodes
  - **Steps**: Maximum steps per episode
  - **ShowAfter**: Episodes after which to show visualization
  - **Delay**: Delay between steps (milliseconds)
  - **WithGUI**: Enable/disable graphical user interface

### Data Types

#### Primitive Types
- **coordinate**: Tuple format "(x, y)" for position specification
- **boolean**: Literal values "true" or "false"
- **number**: Integer or decimal numbers
- **string**: Quoted string literals


### Property Assignment
- Uses the `>>` operator for all property assignments
- Format: `PropertyName >> Value`
- Values must match the expected type for each property