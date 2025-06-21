package agentcrafter.common

import scala.collection.mutable
import scala.util.Random

/**
 * Implementazione della Q-table per l'algoritmo Q-learning.
 * 
 * Questa classe gestisce la memorizzazione e l'aggiornamento dei valori Q
 * per le coppie stato-azione. Utilizza una mappa mutabile per efficienza
 * durante l'apprendimento.
 *
 * @param config Configurazione dei parametri di apprendimento
 */
class QTable(config: LearningConfig):
  
  // Mappa che memorizza i valori Q per ogni coppia (stato, azione)
  private val table: mutable.Map[(State, Action), Double] =
    mutable.Map().withDefaultValue(config.optimistic)

  /**
   * Ottiene il valore Q per una specifica coppia stato-azione.
   *
   * @param state Lo stato
   * @param action L'azione
   * @return Il valore Q corrente
   */
  def getValue(state: State, action: Action): Double =
    table.getOrElse((state, action), config.optimistic)

  /**
   * Aggiorna un valore Q utilizzando la regola di aggiornamento Q-learning.
   *
   * @param state Lo stato corrente
   * @param action L'azione presa
   * @param reward La ricompensa immediata ricevuta
   * @param nextState Lo stato risultante dopo aver preso l'azione
   */
  def updateValue(state: State, action: Action, reward: Reward, nextState: State): Unit =
    val currentValue = getValue(state, action)
    val maxNextValue = Action.values.map(a => getValue(nextState, a)).max
    
    val newValue = (1.0 - config.alpha) * currentValue + 
                   config.alpha * (reward + config.gamma * maxNextValue)
    
    table((state, action)) = newValue

  /**
   * Ottiene tutti i valori Q per tutte le azioni da uno stato specifico.
   *
   * @param state Lo stato da interrogare
   * @return Array dei valori Q per tutte le azioni possibili
   */
  def getStateValues(state: State): Array[Double] =
    Action.values.map(action => getValue(state, action))

  /**
   * Seleziona l'azione con il valore Q più alto per uno stato dato (politica greedy).
   * In caso di pareggio, sceglie casualmente tra le azioni migliori.
   *
   * @param state Lo stato per cui selezionare l'azione
   * @param rng Generatore di numeri casuali per il tie-breaking
   * @return L'azione con il valore Q più alto
   */
  def getBestAction(state: State)(using rng: Random): Action =
    val actionValues = Action.values.map(action => action -> getValue(state, action))
    val maxValue = actionValues.map(_._2).max
    val bestActions = actionValues.filter(_._2 == maxValue).map(_._1)
    
    // Scelta casuale in caso di pareggio
    bestActions(rng.nextInt(bestActions.length))

  /**
   * Crea uno snapshot immutabile della Q-table corrente.
   *
   * @return Una mappa immutabile contenente tutti i valori Q correnti
   */
  def createSnapshot(): Map[(State, Action), Double] =
    table.toMap

  /**
   * Ottiene il numero di coppie stato-azione visitate.
   *
   * @return Il numero di entry nella Q-table
   */
  def size: Int = table.size

  /**
   * Verifica se una coppia stato-azione è stata visitata.
   *
   * @param state Lo stato
   * @param action L'azione
   * @return true se la coppia è stata visitata
   */
  def hasBeenVisited(state: State, action: Action): Boolean =
    table.contains((state, action))

  /**
   * Resetta la Q-table rimuovendo tutti i valori appresi.
   */
  def reset(): Unit =
    table.clear()