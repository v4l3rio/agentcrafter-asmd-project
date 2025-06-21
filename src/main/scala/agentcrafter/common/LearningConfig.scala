package agentcrafter.common

import agentcrafter.common.Constants

/**
 * Configurazione dei parametri per l'algoritmo Q-learning.
 * 
 * Questa classe contiene tutti i parametri necessari per configurare
 * l'algoritmo di apprendimento Q-learning con strategia epsilon-greedy.
 *
 * @param alpha Tasso di apprendimento (0.0 a 1.0) - controlla quanto le nuove informazioni sovrascrivono quelle vecchie
 * @param gamma Fattore di sconto (0.0 a 1.0) - determina l'importanza delle ricompense future
 * @param eps0 Tasso di esplorazione iniziale per la politica epsilon-greedy
 * @param epsMin Tasso di esplorazione minimo dopo il periodo di warm-up
 * @param warm Numero di episodi per il periodo di warm-up prima che inizi il decadimento di epsilon
 * @param optimistic Valore ottimistico iniziale per le coppie stato-azione non visitate
 */
case class LearningConfig(
    alpha: Double = Constants.DEFAULT_LEARNING_RATE,
    gamma: Double = Constants.DEFAULT_DISCOUNT_FACTOR,
    eps0: Double = Constants.DEFAULT_INITIAL_EXPLORATION_RATE,
    epsMin: Double = Constants.DEFAULT_MINIMUM_EXPLORATION_RATE,
    warm: Int = Constants.DEFAULT_WARMUP_EPISODES,
    optimistic: Double = Constants.DEFAULT_OPTIMISTIC_INITIALIZATION
):
  /**
   * Calcola il valore epsilon corrente basato sul numero di episodio.
   *
   * @param episodeNumber Numero dell'episodio corrente
   * @return Il valore epsilon per l'episodio corrente
   */
  def calculateEpsilon(episodeNumber: Int): Double =
    if episodeNumber < warm then 
      eps0 
    else 
      math.max(epsMin, eps0 - (eps0 - epsMin) * (episodeNumber - warm) / warm)

  /**
   * Verifica se i parametri sono validi.
   * 
   * @return true se tutti i parametri sono nei range corretti
   */
  def isValid: Boolean =
    alpha >= 0.0 && alpha <= 1.0 &&
    gamma >= 0.0 && gamma <= 1.0 &&
    eps0 >= 0.0 && eps0 <= 1.0 &&
    epsMin >= 0.0 && epsMin <= 1.0 &&
    warm >= 0 &&
    optimistic >= 0.0