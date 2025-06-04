package agentcrafter.MARL.builders

case class WallLineBuilder(var direction: Option[String] = None,
                           var from: Option[(Int, Int)] = None,
                           var to: Option[(Int, Int)] = None)
