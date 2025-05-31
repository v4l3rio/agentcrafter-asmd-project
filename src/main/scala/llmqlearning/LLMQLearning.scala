package llmqlearning

import MARL.DSL.{SimulationDSL, SimulationWrapper}
import MARL.builders.SimulationBuilder
import scala.util.{Success, Failure}
import scala.collection.mutable



/**
 * LLM-enabled simulation DSL trait
 */
trait LLMQLearning extends SimulationDSL:
  
  // Mutable context to track LLM state
  private var llmEnabled: Boolean = false
  
  /**
   * DSL keyword to enable LLM Q-Table generation
   */
  def useLLM(enabled: Boolean)(using wrapper: SimulationWrapper): Unit =
    llmEnabled = enabled
  
  /**
   * Override simulation method to use LLM-enhanced builder
   */
  override def simulation(block: SimulationWrapper ?=> Unit) =
    given wrapper: SimulationWrapper = SimulationWrapper(new SimulationBuilder)
    block
    
    if llmEnabled then
      loadQTableFromLLM(wrapper.builder) match
        case Some(qTableJson) =>
          println("LLM Response received, loading Q-Table...")
          loadQTableIntoAgents(wrapper.builder, qTableJson)
        case None =>
          println("Failed to get Q-Table from LLM, proceeding with normal simulation")
    
    wrapper.builder.play()

/**
 * Helper functions for LLM integration
 */
private def loadQTableFromLLM(builder: SimulationBuilder): Option[String] =
  val client = LLMApiClient()
  val prompt = """Sei un simulatore di Reinforcement Learning. Il tuo compito è generare Q-Table.
    |La simulazione è descritta utilizzando un Domain Specific Language, in questa descrizione sono presenti tutte le informazioni necessarie.
    |Il tuo compito è interpretare il DSL, estraendo da esso le informazioni chiave, lanciare la simulazione e ritornare il risultato della simulazione
    |ovvero: La Q-Table. È importante che l'output generato da questa richiesta sia solo un JSON questo perché la risposta verrà interpretata in maniera automatica, quindi non scrivere niente oltre il JSON.
    |Alcune caratteristiche possono aiutarti nel compito:
    |Le azioni possibili sono: Up, Down, Left, Right, Stay
    |Ogni azione svolta senza reward positivo, comporta un reward di -1
    |Le coordinate partono da (0,0) e vanno fino al valore espresso in grid
    |Il formato delle righe deve essere questo:
    |"(x, y)": {"Up": value, "Down": value, "Left": value, "Right": value, "Stay": value},
    |Elabora la Q-Table e mandamela con tutti i pesi alla fine della simulazione""".stripMargin
  
  println("Calling LLM API to generate Q-Table...")
  client.callLLM(prompt) match
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