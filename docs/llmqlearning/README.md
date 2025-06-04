# LLM Q-Learning Extensions

This package provides extensions to the MARL simulation framework that enable integration with Large Language Models (LLMs) for Q-Table generation and loading.

## Features

### 1. Q-Table JSON Loading

The `QTableLoader` object provides functionality to load Q-Tables from JSON format into QLearner instances.

#### JSON Format

The expected JSON format for Q-Tables is:

```json
{
  "(0, 0)": {"Up": 0.0, "Down": 0.0, "Left": 0.0, "Right": 0.0, "Stay": 0.0},
  "(0, 1)": {"Up": 0.0, "Down": 0.0, "Left": 0.0, "Right": 0.0, "Stay": 0.0},
  "(1, 0)": {"Up": 0.1, "Down": 0.2, "Left": 0.0, "Right": 0.3, "Stay": 0.0}
}
```

Where:
- Keys are state coordinates in format `"(row, col)"`
- Values are objects mapping action names to Q-values
- Supported actions: `Up`, `Down`, `Left`, `Right`, `Stay`

#### Usage

```scala
import llmqlearning.QTableLoader
import common.QLearner
import scala.util.{Success, Failure}

val learner = new QLearner("agent-id")
val qTableJson = """{
  "(0, 0)": {"Up": 0.1, "Down": 0.0, "Left": 0.0, "Right": 0.2, "Stay": 0.0}
}"""

QTableLoader.loadQTableFromJson(qTableJson, learner) match {
  case Success(_) => println("Q-Table loaded successfully!")
  case Failure(ex) => println(s"Failed to load Q-Table: ${ex.getMessage}")
}
```

### 2. LLM DSL Extension

The `LLMQLearning` trait extends the simulation DSL with a new keyword `useLLM` that enables automatic Q-Table generation via LLM API calls using clean Scala 3 extensions. The implementation uses a property-based configuration system for flexible LLM settings.

#### Configuration Properties

The LLM extension supports the following configuration properties:

- `Enabled` - Boolean flag to enable/disable LLM Q-Table generation
- `Model` - String specifying the LLM model to use (default: "gpt-4o")

#### DSL Grammar

The complete simulation DSL with LLM extensions follows this formal grammar:

```
simulation_block ::= "simulation:" simulation_config ;

simulation_config ::= llm_block? grid_block agent_block+ episodes_block? steps_block? delay_block? show_after_block? gui_block? ;

llm_block ::= "useLLM:" llm_config_block ;

llm_config_block ::= llm_property+ ;

llm_property ::= enabled_property
               | model_property ;

enabled_property ::= "Enabled" ">>" boolean ;

model_property ::= "Model" ">>" string ;

grid_block ::= "grid:" grid_size ;

grid_size ::= number "x" number ;

agent_block ::= "agent:" agent_config ;

agent_config ::= agent_property+ ;

agent_property ::= name_property
                 | start_property
                 | learner_block
                 | goal_property
                 | reward_property ;

name_property ::= "Name" ">>" string ;

start_property ::= "Start" ">>" coordinate ;

goal_property ::= "Goal" ">>" coordinate ;

reward_property ::= "Reward" ">>" number ;

learner_block ::= "withLearner:" learner_config ;

learner_config ::= learner_property+ ;

learner_property ::= "Alpha" ">>" number
                   | "Gamma" ">>" number
                   | "Eps0" ">>" number
                   | "EpsMin" ">>" number
                   | "Warm" ">>" number
                   | "Optimistic" ">>" number ;

episodes_block ::= "Episodes" ">>" number ;

steps_block ::= "Steps" ">>" number ;

delay_block ::= "Delay" ">>" number ;

show_after_block ::= "ShowAfter" ">>" number ;

gui_block ::= "WithGUI" ">>" boolean ;

coordinate ::= "(" number "," number ")" ;

boolean ::= "true" | "false" ;

number ::= [0-9]+ ("." [0-9]+)? ;

string ::= "\"" [^\"]* "\"" ;
```

#### Usage

```scala
import llmqlearning.LLMQLearning
import agentcrafter.llmqlearning.LLMProperty.*

object MySimulation extends App with LLMQLearning

:

import agentcrafter.MARL.DSL.AgentProperty._
import agentcrafter.MARL.DSL.LearnerProperty._
// ... other imports

simulation:
  useLLM
:
Enabled >> true
Model >> "gpt-4o" // Optional: specify model
grid:
  10 x 10
agent:
  Name >> "Runner"
Start >> (1, 9)
withLearner:
  Alpha >> 0.1
Gamma >> 0.99
// ... other learner parameters
Goal >> (9, 9)
Reward >> 100.0
Episodes >> 10_000
Steps >> 500
Delay >> 100
ShowAfter >> 9_000
WithGUI >> true
```

#### Alternative Syntax

You can also enable LLM with just the default settings:

```scala
simulation:
  useLLM:
    Enabled >> true  // Uses default model "gpt-4o"
  // ... rest of configuration
```

#### How it works

1. When `Enabled >> true` is set in the `useLLM` block, the simulation will call the OpenAI API before running
2. The LLM receives the simulation DSL configuration and generates an appropriate Q-Table using the specified model
3. The generated Q-Table is loaded into all agents before the simulation starts
4. If the LLM call fails, the simulation continues with normal Q-Learning
5. Uses clean Scala 3 patterns with property-based configuration for type safety

#### Configuration System

The LLM extension uses an `LLMConfig` case class internally to manage configuration:

```scala
case class LLMConfig(var enabled: Boolean = false, var model: String = "gpt-4o")
```

The DSL properties (`Enabled`, `Model`) are defined in `LLMProperties.scala` and use Scala 3's context functions to provide a clean, type-safe configuration syntax. The `>>` operator is used consistently with the rest of the MARL DSL for property assignment.

### 3. LLM API Client

The `LLMApiClient` handles communication with the OpenAI API. It requires an API key to be set in the environment.

#### Setup

1. Create a `.env` file in your project root:
```
OPENAI_API_KEY=your_api_key_here
```

2. The client will automatically load the API key from the environment

## Files Overview

- `QTableLoader.scala` - Core functionality for loading Q-Tables from JSON with markdown support
- `LLMQLearning.scala` - Clean DSL extensions for LLM integration using Scala 3 patterns
- `LLMProperties.scala` - DSL property definitions for LLM configuration (Enabled, Model)
- `LLMApiClient.scala` - OpenAI API client with environment variable support
- `SimulationApp.scala` - Example simulation using LLM extensions with property-based configuration
- `LLMAPICallExample.scala` - Direct API call example

## Example Usage

See `SimulationApp.scala` for a complete example of using the LLM-enabled simulation DSL with property-based configuration.

See `LLMAPICallExample.scala` for a direct example of calling the LLM API without the DSL wrapper.

## Dependencies

This extension requires:
- `play-json` for JSON parsing
- `sttp-client4` for HTTP requests (already included)
- `dotenv-java` for environment variable loading (already included)

## Error Handling

The system is designed to be robust:
- If LLM API calls fail, simulations continue with normal Q-Learning
- JSON parsing errors are caught and reported
- Reflection-based Q-Table loading has fallback mechanisms

## Notes

- The LLM prompt is optimized for English to ensure better Q-Table generation quality
- Q-Table loading uses reflection to access private QLearner internals
- The system supports both grid-optimized and map-based QLearner storage
- Property-based configuration uses Scala 3 context functions for type safety
- The `>>` operator maintains consistency with the existing MARL DSL syntax
- Default model is "gpt-4o" but can be configured via the `Model` property