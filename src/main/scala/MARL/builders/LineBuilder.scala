package MARL.builders

case class LineBuilder(var direction: Option[String] = None, 
                       var from: Option[(Int, Int)] = None, 
                       var to: Option[(Int, Int)] = None)
