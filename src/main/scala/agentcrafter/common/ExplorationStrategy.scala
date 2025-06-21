package agentcrafter.common

import scala.util.Random

/**
 * Rappresenta il tipo di scelta fatta dall'agente durante la selezione dell'azione.
 */
enum ActionChoice:
  case Exploration(action: Action)
  case Exploitation(action: Action)

/**
 * Strategia di esplorazione epsilon-greedy per l'algoritmo Q-learning.
 * 
 * Questa classe gestisce la logica di scelta tra esplorazione (azioni casuali)
 * e sfruttamento (azioni basate sui valori Q appresi).
 *
 * @param config Configurazione dei parametri di apprendimento
 */
class ExplorationStrategy(config: LearningConfig):
  
  private var currentEpisode: Int = Constants.INITIAL_EPISODE_COUNT
  private given rng: Random = Random()

  /**
   * Sceglie un'azione utilizzando la strategia epsilon-greedy.
   *
   * @param state Lo stato corrente
   * @param qTable La Q-table per ottenere i valori delle azioni
   * @return Una tupla contenente (azione_scelta, è_esplorazione)
   */
  def chooseAction(state: State, qTable: QTable): (Action, Boolean) =
    val epsilon = config.calculateEpsilon(currentEpisode)
    
    if rng.nextDouble() < epsilon then
      // Esplorazione: scelta casuale
      val randomAction = Action.values(rng.nextInt(Action.values.length))
      (randomAction, true)
    else
      // Sfruttamento: scelta greedy basata sui valori Q
      val bestAction = qTable.getBestAction(state)
      (bestAction, false)

  /**
   * Versione più dettagliata che restituisce il tipo di scelta effettuata.
   *
   * @param state Lo stato corrente
   * @param qTable La Q-table per ottenere i valori delle azioni
   * @return Il tipo di scelta effettuata (esplorazione o sfruttamento)
   */
  def chooseActionDetailed(state: State, qTable: QTable): ActionChoice =
    val epsilon = config.calculateEpsilon(currentEpisode)
    
    if rng.nextDouble() < epsilon then
      val randomAction = Action.values(rng.nextInt(Action.values.length))
      ActionChoice.Exploration(randomAction)
    else
      val bestAction = qTable.getBestAction(state)
      ActionChoice.Exploitation(bestAction)

  /**
   * Ottiene il valore epsilon corrente.
   *
   * @return Il valore epsilon per l'episodio corrente
   */
  def getCurrentEpsilon: Double =
    config.calculateEpsilon(currentEpisode)

  /**
   * Incrementa il contatore degli episodi.
   * Dovrebbe essere chiamato all'inizio di ogni nuovo episodio.
   */
  def incrementEpisode(): Unit =
    currentEpisode += Constants.SINGLE_STEP_INCREMENT

  /**
   * Ottiene il numero dell'episodio corrente.
   *
   * @return Il numero dell'episodio corrente
   */
  def getCurrentEpisode: Int = currentEpisode

  /**
   * Resetta il contatore degli episodi.
   */
  def resetEpisodeCounter(): Unit =
    currentEpisode = Constants.INITIAL_EPISODE_COUNT

  /**
   * Imposta manualmente il numero dell'episodio corrente.
   *
   * @param episode Il nuovo numero di episodio
   */
  def setCurrentEpisode(episode: Int): Unit =
    currentEpisode = episode