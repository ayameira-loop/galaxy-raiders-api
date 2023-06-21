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
import java.lang.Integer.min

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

  var score: Double = 0.0
  var numberAsteroidsDestroyed: Int = 0

   //data e hora de inicio
  val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
  val initTime = LocalDateTime.now().format(formatter)


  fun execute() {
    while (true) {
      val duration = measureTimeMillis { this.tick() }
      //depois que acabar o game, pegar as informações de date, pontuação
      //e nº de asteroids destruidos e chamar a funcão parse
      Thread.sleep(
        maxOf(0, GameEngineConfig.msPerFrame - duration)
      )
    }
    //está ocorrendo o problema Unreachable code, escolhe um lugar para colocar
    //essa função
  }

  fun execute(maxIterations: Int) {
    repeat(maxIterations) {
      val duration = measureTimeMillis { this.tick() }
      Thread.sleep(
        maxOf(0, GameEngineConfig.msPerFrame - duration)
      )
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

  fun updateSpaceObjects() {
    if (!this.playing) return
    this.handleCollisions()
    this.moveSpaceObjects()
    this.trimSpaceObjects()
    this.generateAsteroids()
  }

  fun handleCollisions() {
    this.field.spaceObjects.forEachPair {
        (first, second) ->
      if (first.impacts(second)) {
        first.collideWith(second, GameEngineConfig.coefficientRestitution)
        if (first is Missile && second is Asteroid) {
          this.field.generateExplosion(second)
          this.score += 10/second.radius
          this.numberAsteroidsDestroyed++
          this.updateScoreboard(initTime, this.score, this.numberAsteroidsDestroyed)
          this.updateLeaderboard(initTime, this.score, this.numberAsteroidsDestroyed)
        }
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

  fun updateScoreboard(currentDate: String, score: Double, numberAsteroidsDestroyed: Int) {
    val mapper = jacksonObjectMapper()
    var path: String = "/home/gradle/galaxy-raiders/app/src/main/kotlin/galaxyraiders/core/score/Scoreboard.json"
    val jsonString: String = File(path).readText(Charsets.UTF_8)
    val jsonTextList: ArrayList<DataGame> = if (jsonString.isNotEmpty()) {
      mapper.readValue(jsonString)
    } else {
      ArrayList()
    }
    val game = DataGame(currentDate, score, numberAsteroidsDestroyed)
    jsonTextList.add(game)

    val jsonArray: String = mapper.writeValueAsString(jsonTextList)
    File(path).writeText(jsonArray)
  }

  fun updateLeaderboard(currentDate: String, score: Double, numberAsteroidsDestroyed: Int) {
    val mapper = jacksonObjectMapper()
    var path: String = "/home/gradle/galaxy-raiders/app/src/main/kotlin/galaxyraiders/core/score/Leaderboard.json"
    val jsonString: String = File(path).readText(Charsets.UTF_8)
    val jsonTextList: ArrayList<DataGame> = if (jsonString.isNotEmpty()) {
      mapper.readValue(jsonString)
    } else {
      ArrayList()
    }
    val game = DataGame(currentDate, score, numberAsteroidsDestroyed)
    jsonTextList.add(game)

    val sortedList = jsonTextList.sortedByDescending { it.score }
    val top3Entries = sortedList.take(min(sortedList.size, 3))

    val jsonArray: String = mapper.writeValueAsString(top3Entries)
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


data class DataGame (
  val startGame: String,
  val score: Double,
  val asteroidsDestroyed: Int
)