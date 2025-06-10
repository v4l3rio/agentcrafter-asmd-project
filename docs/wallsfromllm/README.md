# Walls From LLM Feature

The `wallsFromLLM` feature allows you to generate maze layouts and wall configurations using Large Language Models (LLMs) directly within your MARL simulation DSL.

## Overview

This feature extends the existing simulation DSL with the ability to automatically generate interesting, challenging, and solvable maze environments using AI. Instead of manually designing walls or using predefined ASCII art, you can describe what kind of environment you want, and the LLM will generate it for you.

## Basic Usage

```scala
import agentcrafter.llmqlearning.LLMQLearning
import agentcrafter.MARL.DSL.{AgentProperty, WallLLMProperty, SimulationProperty}

object MySimulation extends App with LLMQLearning:
  import AgentProperty.*
  import WallLLMProperty.*
  import SimulationProperty.*
  
  simulation:
    grid:
      10 x 12
    
    wallsFromLLM:
      Model >> "gpt-4o"
      Prompt >> """
        Create a challenging maze with multiple paths.
        Make sure positions (1,1) and (8,10) are accessible.
        Include some dead ends and interesting chokepoints.
        """
    
    agent:
      Name >> "Explorer"
      Start >> (1, 1)
      Goal >> (8, 10)
      // ... agent configuration
    
    Episodes >> 1000
    WithGUI >> true
```

## DSL Properties

The `wallsFromLLM` block supports the following properties:

### Model
Specifies which LLM model to use for generation.

```scala
Model >> "gpt-4o"          // Use GPT-4o
Model >> "gpt-3.5-turbo"   // Use GPT-3.5 Turbo
```

### Prompt
Provides the custom description of what kind of maze/environment you want.

```scala
Prompt >> """
  Create a simple maze suitable for learning.
  Include alternative paths and some obstacles.
  Make sure the start and goal positions remain accessible.
  """
```

## How It Works

1. **Context Gathering**: The system automatically gathers information about your simulation:
   - Grid dimensions
   - Agent start and goal positions (if defined)
   - Other simulation parameters

2. **Prompt Construction**: Your custom prompt is combined with:
   - Base instructions for maze generation
   - Current simulation context
   - Technical requirements (ASCII format, accessibility, etc.)

3. **LLM Generation**: The complete prompt is sent to the specified LLM model

4. **ASCII Extraction**: The system extracts the ASCII map from the LLM response

5. **Wall Loading**: The generated walls are loaded into your simulation

## Examples

### Simple Maze
```scala
wallsFromLLM:
  Model >> "gpt-4o"
  Prompt >> """
    Create a simple but interesting maze.
    Include a few alternative routes.
    Not too complex, suitable for learning.
    """
```

### Complex Labyrinth
```scala
wallsFromLLM:
  Model >> "gpt-4o"
  Prompt >> """
    Design a challenging labyrinth with:
    - Multiple dead ends
    - Several rooms connected by narrow corridors
    - At least 3 different paths between start and goal
    - Some chokepoints that require strategic thinking
    """
```

### Themed Environment
```scala
wallsFromLLM:
  Model >> "gpt-4o"
  Prompt >> """
    Create a dungeon-like environment with:
    - A central chamber
    - Branching corridors
    - Hidden passages
    - Suitable for treasure hunting scenarios
    """
```

## Best Practices

### Writing Effective Prompts

1. **Be Specific**: Clearly describe what you want
   ```scala
   // Good
   Prompt >> "Create a maze with 3 alternative paths and 2 dead ends"
   
   // Less effective
   Prompt >> "Make a maze"
   ```

2. **Mention Accessibility**: Always specify important positions that must remain accessible
   ```scala
   Prompt >> "Ensure positions (1,1), (5,5), and (9,9) are reachable"
   ```

3. **Consider Learning**: Think about what makes a good environment for RL agents
   ```scala
   Prompt >> "Create moderate complexity suitable for Q-learning agents"
   ```

### Grid Size Considerations

- **Small grids (5x5 to 8x8)**: Use simple prompts, avoid over-complexity
- **Medium grids (10x10 to 15x15)**: Good for detailed mazes with multiple features
- **Large grids (20x20+)**: Can support complex multi-room environments

### Model Selection

- **GPT-4o**: Best quality, more creative and complex designs
- **GPT-3.5-turbo**: Faster, good for simpler mazes, more cost-effective

## Error Handling

The system includes robust error handling:

- **LLM API failures**: Falls back gracefully, continues without generated walls
- **Invalid ASCII**: Attempts multiple extraction methods
- **Missing properties**: Clear error messages for required Model/Prompt

```scala
// This will throw an error
wallsFromLLM:
  Model >> "gpt-4o"
  // Missing Prompt - will cause IllegalArgumentException
```

## Integration with Other Features

The `wallsFromLLM` feature works seamlessly with:

- **LLM Q-tables**: Use both features together for fully AI-generated simulations
- **Manual walls**: Can be combined with manually defined walls
- **ASCII walls**: Can be used alongside `asciiWalls()` calls

```scala
simulation:
  // Generate base layout with LLM
  wallsFromLLM:
    Model >> "gpt-4o"
    Prompt >> "Create a basic maze structure"
  
  // Add specific manual walls
  walls:
    block >> (5, 5)
    block >> (6, 6)
```

## Complete Example

See the provided example files:
- `SimpleWallsLLMExample.scala` - Basic single-agent example
- `WallsFromLLMExample.scala` - Complex multi-agent scenario

## Requirements

- OpenAI API key configured in environment variables
- Internet connection for LLM API calls
- Valid LLM model name

## Troubleshooting

### Common Issues

1. **"LLM wall generation failed"**
   - Check your API key configuration
   - Verify internet connection
   - Try a simpler prompt

2. **"Failed to extract valid ASCII map"**
   - The LLM response didn't contain properly formatted ASCII
   - Try being more specific about format requirements in your prompt

3. **Walls don't match expectations**
   - Refine your prompt to be more specific
   - Try different model (GPT-4o vs GPT-3.5-turbo)
   - Check that grid dimensions are appropriate for your request

### Debug Tips

- Enable console output to see LLM responses
- Start with simple prompts and gradually increase complexity
- Test with smaller grid sizes first

## API Reference

### WallLLMProperty
- `Model >> String`: Sets the LLM model to use
- `Prompt >> String`: Sets the generation prompt

### LLMWallService
- `generateWallsFromLLM(builder, model, prompt)`: Core generation method
- `loadWallsIntoBuilder(builder, ascii)`: Loads ASCII walls into simulation

This feature opens up endless possibilities for creating diverse, interesting, and challenging environments for your MARL experiments!