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

The `LLMQLearning` trait extends the simulation DSL with a new keyword `useLLM` that enables automatic Q-Table generation via LLM API calls using clean Scala 3 extensions.

#### Usage

```scala
import llmqlearning.LLMQLearning
import MARL.DSL._

object MySimulation extends App with LLMQLearning:
  import AgentProperty._
  import LearnerProperty._
  // ... other imports
  
  simulation:
    useLLM(true)  // Enable LLM Q-Table generation
    grid:
      10 x 10
    agent:
      Name >> "Runner"
      Start >> (1, 9)
      withLearner:
        Alpha >> 0.1
        Gamma >> 0.99
        // ... other learner parameters
      Goal >> (2, 4)
      Reward >> 100.0
    // ... rest of simulation configuration
```

#### How it works

1. When `useLLM(true)` is set, the simulation will call the OpenAI API before running
2. The LLM receives the simulation DSL configuration and generates an appropriate Q-Table
3. The generated Q-Table is loaded into all agents before the simulation starts
4. If the LLM call fails, the simulation continues with normal Q-Learning
5. Uses clean Scala 3 patterns without code duplication (DRY principle)

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
- `LLMApiClient.scala` - OpenAI API client (already existing)
- `SimulationApp.scala` - Example simulation using LLM extensions
- `QTableExample.scala` - Standalone example of Q-Table loading

## Example Usage

See `QTableExample.scala` for a complete example of loading Q-Tables from JSON.

See `SimulationApp.scala` for an example of using the LLM-enabled simulation DSL.

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

- The LLM prompt is currently in Italian as per the original requirements
- Q-Table loading uses reflection to access private QLearner internals
- The system supports both grid-optimized and map-based QLearner storage