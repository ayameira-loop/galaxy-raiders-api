package galaxyraiders.core.game

import galaxyraiders.Config
import galaxyraiders.ports.RandomGenerator
import galaxyraiders.ports.ui.Controller
import galaxyraiders.ports.ui.Controller.PlayerCommand
import galaxyraiders.ports.ui.Visualizer
import kotlin.system.measureTimeMillis

import com.fasterxml.jackson.module.kotlin.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.io.File

const val MILLISECONDS_PER_SECOND: Int = 1000

object GameEngineConfig {
  private val config = Config(prefix = "GR__CORE__GAME__GAME_ENGINE__")

  val frameRate = config.get<Int>("FRAME_RATE")
  val spaceFieldWidth = config.get<Int>("SPACEFIELD_WIDTH")
  val spaceFieldHeight = config.get<Int>("SPACEFIELD_HEIGHT")
  val asteroidProbability = config.get<Double>("ASTEROID_PROBABILITY")
  val coefficientRestitution = config.get<Double>("COEFFICIENT_RESTITUTION")

  val msPerFrame: Int = MILLISECONDS_PER_SECOND / this.frameRate
}

@Suppress("TooManyFunctions")
class GameEngine(
  val generator: RandomGenerator,
  val controller: Controller,
  val visualizer: Visualizer,
) {
  val field = SpaceField(
    width = GameEngineConfig.spaceFieldWidth,
    height = GameEngineConfig.spaceFieldHeight,
    generator = generator
  )

  var playing = true

   //data e hora de inicio
  val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
  val current = LocalDateTime.now().format(formatter)


  fun execute() {
    while (true) {
      val duration = measureTimeMillis { this.tick() }
      //depois que acabar o game, pegar as informações de date, pontuação
      //e nº de asteroids destruidos e chamar a funcão parse
      Thread.sleep(
        maxOf(0, GameEngineConfig.msPerFrame - duration)
      )
    }
    //apenas valores para teste
    this.parsejson(current, 120, 10)
    //está ocorrendo o problema Unreachable code, escolhe um lugar para colocar 
    //essa função
  }

  fun execute(maxIterations: Int) {
    repeat(maxIterations) {
      this.tick()
    }
  }

  fun tick() {
    this.processPlayerInput()
    this.updateSpaceObjects()
    this.renderSpaceField()
  }

  fun processPlayerInput() {
    this.controller.nextPlayerCommand()?.also {
      when (it) {
        PlayerCommand.MOVE_SHIP_UP ->
          this.field.ship.boostUp()
        PlayerCommand.MOVE_SHIP_DOWN ->
          this.field.ship.boostDown()
        PlayerCommand.MOVE_SHIP_LEFT ->
          this.field.ship.boostLeft()
        PlayerCommand.MOVE_SHIP_RIGHT ->
          this.field.ship.boostRight()
        PlayerCommand.LAUNCH_MISSILE ->
          this.field.generateMissile()
        PlayerCommand.PAUSE_GAME ->
          this.playing = !this.playing
      }
    }
  }

  fun updateSpaceObjects() { //mudei aqui para renderizar a classe explosion
    if (!this.playing) return
    this.handleCollisions()
    this.moveSpaceObjects()
    this.trimSpaceObjects()
    this.generateAsteroids()
  }

  fun handleCollisions() {
    this.field.spaceObjects.forEachPair {//faz o pair de 2 a 2 dos objetos na lista 
                                        //no space field
        (first, second) ->
      if (first.impacts(second)) {
        //se um asteroite e um missil colidir, construir um objeto explosion
        //calcular pontuação aqui
        if (first is Asteroid && second is Missile){
          this.field.generateExplosion(first) // estou passando o asteroide
                                              // depois usando o centro do asteroide para colocar a explosão
                                              // e nao o ponto de impacto
          //calcular aqui a pontuação do game
          //somar o numero de asteroids destruidos
        }
        first.collideWith(second, GameEngineConfig.coefficientRestitution)
      }
    }
  }

  fun moveSpaceObjects() {
    this.field.moveShip()
    this.field.moveAsteroids()
    this.field.moveMissiles()
  }

  fun trimSpaceObjects() {
    this.field.trimAsteroids()
    this.field.trimMissiles()
    this.field.trimExplosions()
  }

  fun generateAsteroids() {
    val probability = generator.generateProbability()

    if (probability <= GameEngineConfig.asteroidProbability) {
      this.field.generateAsteroid()
    }
  }

  fun renderSpaceField() {
    this.visualizer.renderSpaceField(this.field)//aqui que ele passa o json
  }

  //função para ler e escrever no arquivo json
  fun parsejson(currentDate: String, pointing: Int, numberAsteroidsDestroyed: Int){
      
    val mapper = jacksonObjectMapper()

    var path: String = "/home/gradle/galaxy-raiders/app/src/main/kotlin/galaxyraiders/core/score/Scoreboard.json"
    //se o json estiver vazio dar erro,corrigir isso
    val jsonString: String = File(path).readText(Charsets.UTF_8)
    val jsonTextList:ArrayList<DataGame> = mapper.readValue<ArrayList<DataGame>>(jsonString)
    val game = DataGame(currentDate,pointing,numberAsteroidsDestroyed)
    jsonTextList.add(game)
    // for (film in jsonTextList) {
    //     println(film)
    // }

      
    val jsonArray: String = mapper.writeValueAsString(jsonTextList)
    //println(jsonArray)
    File(path).writeText(jsonArray)

  }
}

fun <T> List<T>.forEachPair(action: (Pair<T, T>) -> Unit) {
  for (i in 0 until this.size) {
    for (j in i + 1 until this.size) {
      action(Pair(this[i], this[j]))
    }
  }
}


data class DataGame(
  val startGame: String,
  val pointing: Int,
  val nAsteroidsDestroyed: Int
)