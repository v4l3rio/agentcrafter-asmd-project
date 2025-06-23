package agentcrafter.llmqlearning

import scala.io.Source
import scala.util.Using

/**
 * Centralized prompt management for LLM integration.
 *
 * This object provides access to pre-defined prompt templates used for various LLM-powered features in AgentCrafter.
 * Prompts are loaded lazily from resource files bundled with the application, ensuring they're available at runtime
 * without requiring external file dependencies.
 *
 * The prompts are designed to work with different LLM providers and are optimized for reinforcement learning contexts,
 * providing clear instructions and examples for tasks like Q-table analysis and dynamic environment generation.
 *
 * All prompts are loaded using resource management to ensure proper cleanup and error handling during file operations.
 */
object Prompts:
  /**
   * Q-table generation prompt template.
   *
   * This prompt is designed to help LLMs generate Q-tables for both single and multiple agents.
   * For multiple agents, it considers their interactions, potential conflicts, and coordination opportunities.
   * For single agents, it generates a single optimized Q-table.
   * Each agent receives a Q-table optimized for their specific goals and environment context.
   */
  lazy val multiAgentQTable: String =
    Using.resource(Source.fromResource("prompts/multi_agent_qtable_generation_prompt.txt"))(_.mkString)

  /**
   * Dynamic wall generation prompt template.
   *
   * This prompt enables LLMs to generate interesting and challenging wall configurations for grid world environments.
   * It includes guidelines for creating balanced obstacle layouts that provide meaningful learning challenges without
   * making environments unsolvable.
   *
   * The prompt is loaded lazily from `src/main/resources/prompts/walls_generation_prompt.txt` and is bundled within the
   * JAR for distribution.
   */
  lazy val walls: String =
    Using.resource(Source.fromResource("prompts/walls_generation_prompt.txt"))(_.mkString)
