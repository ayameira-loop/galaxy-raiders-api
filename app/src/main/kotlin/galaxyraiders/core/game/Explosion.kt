package galaxyraiders.core.game

import galaxyraiders.core.physics.Point2D
import galaxyraiders.core.physics.Vector2D

class Explosion(
  initialPosition: Point2D
) :
  SpaceObject("Explosion", '*', initialPosition, Vector2D(0.0,0.0), 1.0, 0.0){//ver qual raio é o melhor
    var is_trigged = false

    fun was_exploded(){
        is_trigged = true
    }
    
  }
