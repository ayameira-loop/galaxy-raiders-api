@file:Suppress("MatchingDeclarationName")
package galaxyraiders

import galaxyraiders.adapters.BasicRandomGenerator
import galaxyraiders.adapters.tui.TextUserInterface
import galaxyraiders.adapters.web.WebUserInterface
import galaxyraiders.core.game.GameEngine
import kotlin.concurrent.thread
import kotlin.random.Random
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import java.io.File

object AppConfig {
  val config = Config("GR__APP__")

  val randomSeed = config.get<Int>("RANDOM_SEED")
  val operationMode = config.get<OperationMode>("OPERATION_MODE")
}

fun main() {
  val generator = BasicRandomGenerator(
    rng = Random(seed = AppConfig.randomSeed)
  )

  val ui = when (AppConfig.operationMode) {
    OperationMode.Text -> TextUserInterface()
    OperationMode.Web -> WebUserInterface()
  }

  val (controller, visualizer) = ui.build()

  val gameEngine = GameEngine(
    generator, controller, visualizer
  )
  //data e hora de inicio
  val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
  //ta no formato windows, concertar depois ou nao
  val current = LocalDateTime.now().format(formatter)
  //System.out.println(" C DATE is  "+current)
  //parsejson()

  thread { gameEngine.execute() }

  ui.start()
}
//teste para ler json
// fun parsejson(){
    
//   val filename = "core/score/Scoreboard.json"
//   var lines:List<String> = File(filename).readLines()
//   lines.forEach {line -> println(line)}


//   println("Json file read complete!")
    
// }