package galaxyraiders.core.game

import galaxyraiders.core.physics.Point2D
import galaxyraiders.core.physics.Vector2D
import java.util.Timer
import java.util.TimerTask


class Explosion(
  initialPosition: Point2D
) :
  SpaceObject("Explosion", '*', initialPosition, Vector2D(0.0,0.0), 1.0, 0.0) {//ver qual raio é o melhor

    var isTriggered: Boolean = true
      private set

    fun untrigger() {
      this.isTriggered = false
    }

    init {
      var timerTask = object : TimerTask() {
        override fun run() {
          untrigger()
        }
      }

      Timer().schedule(timerTask, 2000)
    }
  }
