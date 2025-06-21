package agentcrafter.common

import agentcrafter.common.Constants
import scala.annotation.tailrec
import scala.util.Random

/**
 * Type alias per rappresentare la traiettoria di un agente durante un episodio.
 * Contiene stato, azione, flag di esplorazione e valori Q per ogni passo.
 */
type Trajectory = List[(State, Action, Boolean, Array[Double])]

/**
 * Type alias per i valori di ricompensa nel sistema di apprendimento.
 */
type Reward = Double

/**
 * Type alias per gli identificatori degli agenti.
 */
type ID = String

/**
 * Type alias per i risultati degli episodi contenenti stato di successo, numero di passi e traiettoria.
 */
type EpisodeOutcome = (Boolean, Int, Trajectory)

/**
 * Type alias per le funzioni di aggiornamento dell'ambiente che gestiscono le transizioni di stato.
 */
type UpdateFunction = (State, Action) => StepResult

/**
 * Type alias per le funzioni di reset dell'ambiente che restituiscono lo stato iniziale.
 */
type ResetFunction = () => State

/**
 * Factory object per creare istanze di QLearner.
 */
object QLearner:
  /**
   * Crea una nuova istanza di QLearner con i parametri specificati.
   *
   * @param goalState Lo stato obiettivo che l'agente deve raggiungere
   * @param goalReward La ricompensa data quando si raggiunge lo stato obiettivo
   * @param updateFunction Funzione che gestisce le transizioni di stato nell'ambiente
   * @param resetFunction Funzione che resetta l'ambiente allo stato iniziale
   * @param learningConfig Parametri di configurazione per l'algoritmo Q-learning
   * @return Una nuova istanza di QLearner
   */
  def apply(
    goalState: State,
    goalReward: Reward,
    updateFunction: UpdateFunction,
    resetFunction: ResetFunction,
    learningConfig: LearningConfig = LearningConfig()
  ): QLearner =
    new QLearner(goalState, goalReward, updateFunction, resetFunction, learningConfig)

/**
 * Implementazione dell'algoritmo Q-Learning per il reinforcement learning.
 *
 * Questa classe implementa l'algoritmo Q-learning con esplorazione epsilon-greedy.
 * È stata ristrutturata per essere più lineare e comprensibile, separando le
 * responsabilità in classi dedicate.
 *
 * @param goalState Lo stato obiettivo che termina gli episodi con successo
 * @param goalReward La ricompensa data quando si raggiunge lo stato obiettivo
 * @param updateFunction Funzione che gestisce le transizioni di stato dell'ambiente
 * @param resetFunction Funzione che resetta l'ambiente alle condizioni iniziali
 * @param config Parametri di configurazione per l'algoritmo di apprendimento
 */
class QLearner private (
  goalState: State,
  goalReward: Reward,
  updateFunction: UpdateFunction,
  resetFunction: ResetFunction,
  config: LearningConfig
) extends Learner:

  // Componenti principali del Q-learner
  private val qTable = new QTable(config)
  private val explorationStrategy = new ExplorationStrategy(config)
  
  private given rng: Random = Random()

  /**
   * Esegue un episodio completo di apprendimento.
   * 
   * @param maxSteps Numero massimo di passi per episodio
   * @return Risultato dell'episodio (successo, numero di passi, traiettoria)
   */
  override def episode(maxSteps: Int = Constants.DEFAULT_MAX_STEPS_PER_EPISODE): EpisodeOutcome =
    explorationStrategy.incrementEpisode()
    val initialState = resetFunction()
    
    runEpisodeLoop(initialState, maxSteps)

  /**
   * Loop principale dell'episodio - gestisce la logica di esecuzione passo dopo passo.
   */
  @tailrec
  private def runEpisodeLoop(
    state: State, 
    maxSteps: Int, 
    currentStep: Int = 0, 
    trajectory: List[(State, Action, Boolean, Array[Double])] = Nil
  ): EpisodeOutcome =
    
    // Verifica condizioni di terminazione
    if state == goalState then
      (true, currentStep, trajectory.reverse)
    else if currentStep >= maxSteps then
      (false, currentStep, trajectory.reverse)
    else
      // Scelta dell'azione usando la strategia di esplorazione
      val (action, isExploring) = explorationStrategy.chooseAction(state, qTable)
      
      // Esecuzione dell'azione nell'ambiente
      val StepResult(nextState, environmentReward) = updateFunction(state, action)
      
      // Calcolo della ricompensa finale
      val finalReward = if nextState == goalState then goalReward else environmentReward
      
      // Aggiornamento della Q-table
      qTable.updateValue(state, action, finalReward, nextState)
      
      // Registrazione del passo nella traiettoria
      val stepRecord = (state, action, isExploring, qTable.getStateValues(state))
      val newTrajectory = stepRecord :: trajectory
      
      // Continua con il prossimo passo
      runEpisodeLoop(nextState, maxSteps, currentStep + 1, newTrajectory)

  /**
   * Ottiene uno snapshot immutabile della Q-table corrente.
   * 
   * @return Mappa contenente tutti i valori Q correnti
   */
  def QTableSnapshot: Map[(State, Action), Double] =
    qTable.createSnapshot()

  /**
   * Ottiene il valore Q per una specifica coppia stato-azione.
   * 
   * @param state Lo stato
   * @param action L'azione
   * @return Il valore Q corrente
   */
  def getQValue(state: State, action: Action): Double =
    qTable.getValue(state, action)

  /**
   * Sceglie un'azione per lo stato dato utilizzando la strategia epsilon-greedy.
   * 
   * @param state Lo stato corrente
   * @return Tupla contenente (azione, è_esplorazione)
   */
  override def choose(state: State): (Action, Boolean) =
    explorationStrategy.chooseAction(state, qTable)

  /**
   * Ottiene il tasso di esplorazione corrente (epsilon).
   * 
   * @return Il valore epsilon per l'episodio corrente
   */
  override def eps: Double =
    explorationStrategy.getCurrentEpsilon

  /**
   * Aggiorna il valore Q per una coppia stato-azione.
   * 
   * @param state Lo stato corrente
   * @param action L'azione presa
   * @param reward La ricompensa ricevuta
   * @param nextState Lo stato successivo
   */
  override def update(state: State, action: Action, reward: Reward, nextState: State): Unit =
    qTable.updateValue(state, action, reward, nextState)

  /**
   * Aggiorna il valore Q utilizzando la logica di ricompensa dell'obiettivo.
   * 
   * Questo rispecchia il passo di aggiornamento eseguito all'interno di [[episode]].
   * La ricompensa dell'ambiente fornita viene combinata con la ricompensa dell'obiettivo
   * se lo stato successivo corrisponde allo stato obiettivo configurato.
   */
  override def updateWithGoal(state: State, action: Action, envReward: Reward, nextState: State): Unit =
    val finalReward = if nextState == goalState then goalReward else envReward
    qTable.updateValue(state, action, finalReward, nextState)

  /**
   * Incrementa il contatore degli episodi.
   */
  override def incEp(): Unit =
    explorationStrategy.incrementEpisode()

  /**
   * Ottiene informazioni di debug sulla configurazione corrente.
   * 
   * @return Stringa contenente informazioni sulla configurazione
   */
  def getDebugInfo: String =
    s"""QLearner Debug Info:
       |  Current Episode: ${explorationStrategy.getCurrentEpisode}
       |  Current Epsilon: ${explorationStrategy.getCurrentEpsilon}
       |  Q-Table Size: ${qTable.size}
       |  Goal State: $goalState
       |  Goal Reward: $goalReward""".stripMargin

  /**
   * Resetta completamente il learner allo stato iniziale.
   */
  def reset(): Unit =
    qTable.reset()
    explorationStrategy.resetEpisodeCounter()
