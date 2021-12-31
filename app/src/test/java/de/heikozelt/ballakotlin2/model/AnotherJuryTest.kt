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
        val gs = GameState(3, 1, 3)
        gs.initTubes()
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
        val gs = GameState(3, 1, 3)
        gs.initTubes()
        gs.moveBall(Move(0, 3))
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
        val gs = GameState(4, 1, 4)
        gs.tubes[0].addBall(1)
        gs.tubes[0].addBall(1)
        gs.tubes[0].addBall(1)
        gs.tubes[0].addBall(1)
        gs.tubes[1].addBall(2)
        gs.tubes[1].addBall(2)
        gs.tubes[1].addBall(2)
        gs.tubes[2].addBall(3)
        gs.tubes[2].addBall(2)
        gs.tubes[3].addBall(4)
        gs.tubes[3].addBall(4)
        gs.tubes[3].addBall(4)
        gs.tubes[3].addBall(4)
        gs.tubes[4].addBall(3)
        gs.tubes[4].addBall(3)
        gs.tubes[4].addBall(3)
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