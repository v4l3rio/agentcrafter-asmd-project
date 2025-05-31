package llmqlearning

import scala.util.{Failure, Success}

// ---------------------------------------------------------------------------
// Example usage
// ---------------------------------------------------------------------------
object LLMAPICallExample extends App:
  val client = LLMApiClient()
  client.callLLM(
      "Sei un simulatore di Reinforcement Learning. Il tuo compito è generare Q-Table.\n" +
      "La simulazione è descritta utilizzando un Domain Specific Language, in questa descrizione sono presenti tutte le informazioni necessarie.\n" +
      "Il tuo compito è interpretare il DSL, estraendo da esso le informazioni chiave, lanciare la simulazione e ritornare il risultato della simulazione\n"+
      "ovvero: La Q-Table. È importante che l'output generato da questa richiesta sia solo un JSON questo perché la risposta verrà interpretata in maniera automatica, quindi non scrivere niente oltre il JSON.\n" +
      "Alcune caratteristiche possono aiutarti nel compito:\n" +
      "Le azioni possibili sono: Up, Down, Left, Right, Stay\n" +
      "Ogni azione svolta senza reward positivo, comporta un reward di -1\n" +
      "Le coordinate partono da (0,0) e vanno fino al valore espresso in grid\n"+
      "Il formato delle righe deve essere questo:\n"+
      "\"(x, y)\": {\"Up\": value, \"Down\": value, \"Left\": value, \"Right\": value, \"Stay\": value},"+
      "Elabora la Q-Table e mandamela con tutti i pesi alla fine della simulazione\n") match
    case Success(res) =>
      println(res)
    case Failure(ex)  =>
      println(s"Error calling OpenAI API: ${ex.getMessage}")