package de.heikozelt.ballakotlin2.model

import org.junit.Test
import org.junit.Assert.*

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
        val gs = GameState(3, 1, 3)
        gs.initTubes()
        val jury = AnotherJury(gs)
        // jeweils Zug in leere Röhre
        assertEquals(41, jury.rateBackwardMove(Move(0, 3)))
        assertEquals(41, jury.rateBackwardMove(Move(1, 3)))
        assertEquals(41, jury.rateBackwardMove(Move(2, 3)))
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
        val gs = GameState(3, 1, 3)
        gs.initTubes()
        gs.moveBall(Move(0, 3))
        val jury = AnotherJury(gs)

        // Zug auf einen Ball in gleicher Farbe von einer Säule in gleicher Farbe
        assertEquals(2 * 12 + 1 * 12, jury.rateBackwardMove(Move(0, 3)))

        // langweilig
        assertEquals(1, jury.rateBackwardMove(Move(1, 0)))

        // Zug auf einzelnen Ball einer anderen Farbe
        assertEquals(3 * 4 + 2 * 4, jury.rateBackwardMove(Move(1, 3)))

        // langweilig
        assertEquals(1, jury.rateBackwardMove(Move(2, 0)))

        // Zug auf einzelnen Ball einer anderen Farbe
        assertEquals(3 * 4 + 2 * 4, jury.rateBackwardMove(Move(2, 3)))
    }

}