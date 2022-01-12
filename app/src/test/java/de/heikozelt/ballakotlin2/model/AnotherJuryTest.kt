package de.heikozelt.ballakotlin2.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

//import org.junit.Test
//import org.junit.Assert.*

class AnotherJuryTest {

    /**
     * <pre>
     * 1 2 3 _    _ 2 3 _
     * 1 2 3 _ => 1 2 3 _
     * 1 2 3 _    1 2 3 1
     * </pre>
     */
    @Test
    fun rateBackwardMove_first_move() {
        val gs = GameState().apply {
            resize(3, 1, 3)
            rainbow()
        }
        val jury = AnotherJury(gs)
        // jeweils Zug in leere Röhre
        assertEquals(3 * AnotherJury.FACTOR_EMPTY, jury.rateBackwardMove(Move(0, 3)))
        assertEquals(3 * AnotherJury.FACTOR_EMPTY, jury.rateBackwardMove(Move(1, 3)))
        assertEquals(3 * AnotherJury.FACTOR_EMPTY, jury.rateBackwardMove(Move(2, 3)))
    }

    /**
     * <pre>
     * _ 2 3 _
     * 1 2 3 _
     * 1 2 3 1
     * </pre>
     */
    @Test
    fun rateBackwardMove_b() {
        val gs = GameState().apply {
            resize(3, 1, 3)
            rainbow()
            moveBall(Move(0, 3))
        }
        val jury = AnotherJury(gs)

        // Zug auf einen Ball in gleicher Farbe von einer Säule in gleicher Farbe
        assertEquals((2 + 1) * AnotherJury.FACTOR_SAME, jury.rateBackwardMove(Move(0, 3)))

        // langweilig
        assertEquals(1, jury.rateBackwardMove(Move(1, 0)))

        // Zug auf einzelnen Ball einer anderen Farbe
        assertEquals((3 + 2) * AnotherJury.FACTOR_ONE, jury.rateBackwardMove(Move(1, 3)))

        // langweilig
        assertEquals(1, jury.rateBackwardMove(Move(2, 0)))

        // Zug auf einzelnen Ball einer anderen Farbe
        assertEquals((3 + 2) * AnotherJury.FACTOR_ONE, jury.rateBackwardMove(Move(2, 3)))
    }


    /**
     * <pre>
     * 1 _ _ 4 _
     * 1 2 _ 4 3
     * 1 2 2 4 3
     * 1 2 3 4 3
     * </pre>
     */
    @Test
    fun rateBackwardMove_c() {
        val gs = GameState().apply {
            resize(4, 1, 4)
            tubes[0].apply {
                addBall(1); addBall(1); addBall(1); addBall(1)
            }
            tubes[1].apply {
                addBall(2); addBall(2); addBall(2)
            }
            tubes[2].apply {
                addBall(3); addBall(2)
            }
            tubes[3].apply {
                addBall(4); addBall(4); addBall(4); addBall(4)
            }
            tubes[4].apply {
                addBall(3); addBall(3); addBall(3)
            }
            //dump()
        }
        val jury = AnotherJury(gs)

        assertEquals(1, jury.rateBackwardMove(Move(0, 1)))

        // Zug auf einzelnen Ball einer anderen Farbe, 24
        assertEquals((2 + 4) * AnotherJury.FACTOR_ONE, jury.rateBackwardMove(Move(0, 2)))

        assertEquals(1, jury.rateBackwardMove(Move(0, 4)))

        // Zug auf einen Ball in gleicher Farbe von einer Säule in gleicher Farbe, 48
        assertEquals((3 + 1) * AnotherJury.FACTOR_SAME, jury.rateBackwardMove(Move(1, 2)))

        assertEquals(1, jury.rateBackwardMove(Move(1, 4)))
        assertEquals(1, jury.rateBackwardMove(Move(3, 1)))

        // Zug auf einzelnen Ball einer anderen Farbe, 24
        assertEquals((4 + 2) * AnotherJury.FACTOR_ONE, jury.rateBackwardMove(Move(3, 2)))

        assertEquals(1, jury.rateBackwardMove(Move(3, 4)))
        assertEquals(1, jury.rateBackwardMove(Move(4, 1)))

        // Zug auf einzelnen Ball einer anderen Farbe, 20
        assertEquals((3 + 2) * AnotherJury.FACTOR_ONE, jury.rateBackwardMove(Move(4, 2)))
    }

}