package galaxyraiders.core.game

import galaxyraiders.core.physics.Point2D
import galaxyraiders.core.physics.Vector2D
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("Given an explosion")
class ExplosionTest {
  private val explosion = Explosion(
    initialPosition = Point2D(1.0, 1.0)
  )

  @Test
  fun `it has its parameters initialized correctly`() {
    assertAll(
      "Explosion should initialize all its parameters correctly",
      { assertNotNull(explosion) },
      { assertEquals(Point2D(1.0, 1.0), explosion.center) },
    )
  }

  @Test
  fun `it is triggered upon creation`() {
    assertTrue(explosion.isTriggered)
  }

  @Test
  fun `it can be untriggered (explosion is over)`() {
    explosion.untrigger()
    assertFalse(explosion.isTriggered)
  }
}
