package agentcrafter.llmqlearning

import scala.io.Source
import scala.util.Using

/**
 * Centralized prompt management for LLM integration.
 *
 * This object provides access to pre-defined prompt templates used for various
 * LLM-powered features in AgentCrafter. Prompts are loaded lazily from resource
 * files bundled with the application, ensuring they're available at runtime
 * without requiring external file dependencies.
 *
 * The prompts are designed to work with different LLM providers and are optimized
 * for reinforcement learning contexts, providing clear instructions and examples
 * for tasks like Q-table analysis and dynamic environment generation.
 *
 * All prompts are loaded using resource management to ensure proper cleanup
 * and error handling during file operations.
 */
object Prompts:
  /**
   * Q-table generation and analysis prompt template.
   *
   * This prompt is designed to help LLMs understand and work with Q-tables
   * in reinforcement learning contexts. It provides instructions for analyzing
   * Q-values, suggesting optimal actions, and understanding the learning progress
   * of agents in grid-based environments.
   *
   * The prompt is loaded lazily from `src/main/resources/prompts/qtable_generation_prompt.txt`
   * and is bundled within the JAR for distribution.
   */
  lazy val qTable: String =
    Using.resource(Source.fromResource("prompts/qtable_generation_prompt.txt"))(_.mkString)

  /**
   * Dynamic wall generation prompt template.
   *
   * This prompt enables LLMs to generate interesting and challenging wall
   * configurations for grid world environments. It includes guidelines for
   * creating balanced obstacle layouts that provide meaningful learning
   * challenges without making environments unsolvable.
   *
   * The prompt is loaded lazily from `src/main/resources/prompts/walls_generation_prompt.txt`
   * and is bundled within the JAR for distribution.
   */
  lazy val walls: String =
    Using.resource(Source.fromResource("prompts/walls_generation_prompt.txt"))(_.mkString)