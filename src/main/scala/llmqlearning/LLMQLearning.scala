package llmqlearning

import MARL.DSL.{SimulationDSL, SimulationWrapper}
import MARL.builders.SimulationBuilder
import MARL.AgentSpec
import scala.util.{Success, Failure}
import scala.collection.mutable
import llmqlearning.LLMProperty.*



/**
 * LLM configuration wrapper for DSL
 */
case class LLMConfig(var enabled: Boolean = false, var model: String = "gpt-4o")

// LLM Property DSL objects for configuration
object Enabled:
  def >>(value: Boolean)(using config: LLMConfig): Unit =
    config.enabled = value

object Model:
  def >>(value: String)(using config: LLMConfig): Unit =
    config.model = value

/**
 * LLM-enabled simulation DSL trait
 */
trait LLMQLearning extends SimulationDSL:
  
  // Mutable context to track LLM state
  private var llmConfig: LLMConfig = LLMConfig()
  
  /**
   * DSL keyword to enable LLM Q-Table generation with configuration
   */
  def useLLM(block: LLMConfig ?=> Unit)(using wrapper: SimulationWrapper): Unit =
    given config: LLMConfig = llmConfig
    block
    llmConfig = config
  
  /**
   * Override simulation method to use LLM-enhanced builder
   */
  override def simulation(block: SimulationWrapper ?=> Unit) =
    given wrapper: SimulationWrapper = SimulationWrapper(new SimulationBuilder)
    block
    
    if llmConfig.enabled then
      loadQTableFromLLM(wrapper.builder, llmConfig.model) match
        case Some(qTableJson) =>
          println(s"LLM Response received from ${llmConfig.model}, loading Q-Table...")
          loadQTableIntoAgents(wrapper.builder, qTableJson)
        case None =>
          println(s"Failed to get Q-Table from LLM (${llmConfig.model}), proceeding with normal simulation")
    
    wrapper.builder.play()

/**
 * Helper functions for LLM integration
 */
private def loadQTableFromLLM(builder: SimulationBuilder, model: String): Option[String] =
  val client = LLMApiClient()
  val prompt = """You are a Reinforcement Learning simulator specialized in generating optimal Q-Tables for grid-based environments.
    |
    |TASK: Analyze the provided Domain Specific Language (DSL) description and generate a complete Q-Table that represents an optimal policy for navigating from any position to the goal.
    |
    |CRITICAL REQUIREMENTS:
    |1. OUTPUT FORMAT: Return ONLY a valid JSON object - no additional text, explanations, or formatting
    |2. Q-VALUES STRATEGY: Create a value gradient that forms clear paths to the goal, not just high values at reward locations
    |3. PATH OPTIMIZATION: Ensure Q-values decrease gradually as distance from goal increases, creating natural navigation paths
    |
    |ENVIRONMENT RULES:
    |- Available actions: Up, Down, Left, Right, Stay
    |- Coordinate system: starts at (0,0) and extends to grid dimensions
    |- Step penalty: -1 for each action without positive reward
    |- Walls: represented by '#' in asciiWalls, block movement
    |- Goal reward: specified in agent configuration
    |
    |Q-TABLE GENERATION STRATEGY:
    |1. Identify the goal position and reward value from the DSL
    |2. Calculate optimal distances from each cell to the goal (considering walls)
    |3. Assign Q-values that create a gradient: higher values for actions leading toward goal
    |4. For each position, the action pointing toward the shortest path to goal should have the highest Q-value
    |5. Consider wall obstacles when calculating paths - blocked directions should have very low Q-values
    |6. Apply discount factor to create realistic value propagation
    |
    |JSON FORMAT (exact structure required):
    |"(row, col)": {"Up": value, "Down": value, "Left": value, "Right": value, "Stay": value}
    |
    |EXAMPLE Q-VALUE ASSIGNMENT:
    |- At goal: all actions have high positive values (goal reward - small penalty)
    |- Adjacent to goal: action toward goal = high value, others = lower values
    |- Further from goal: gradually decreasing values, with direction toward goal always highest
    |- Near walls: actions toward walls = very negative values
    |
    |Generate the complete Q-Table now:""".stripMargin
  
  println(s"Calling LLM API ($model) to generate Q-Table...")
  client.callLLM(prompt, model) match
    case Success(response) => Some(response)
    case Failure(ex) =>
      println(s"LLM API call failed: ${ex.getMessage}")
      None

private def loadQTableIntoAgents(builder: SimulationBuilder, qTableJson: String): Unit =
  try {
    // Access agents using reflection (since it's private)
    val agentsField = builder.getClass.getDeclaredField("agents")
    agentsField.setAccessible(true)
    val agents = agentsField.get(builder).asInstanceOf[mutable.Map[String, MARL.AgentSpec]]
    
    agents.values.foreach { agentSpec =>
      QTableLoader.loadQTableFromJson(qTableJson, agentSpec.learner) match
        case Success(_) =>
          println(s"Successfully loaded LLM Q-Table for agent: ${agentSpec.id}")
        case Failure(ex) =>
          println(s"Failed to load LLM Q-Table for agent ${agentSpec.id}: ${ex.getMessage}")
    }
  } catch {
    case ex: Exception =>
      println(s"Error accessing agents for Q-Table loading: ${ex.getMessage}")
  }