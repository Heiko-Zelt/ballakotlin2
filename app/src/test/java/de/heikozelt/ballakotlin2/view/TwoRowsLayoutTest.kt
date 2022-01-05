package de.heikozelt.ballakotlin2.view

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TwoRowsLayoutTest {

    // Spielfeld mit Breite 2 und Höhe 1 ergibt oben und unten je eine Röhre
    @Test
    fun constructor_2x1() {
        val layout = TwoRowsLayout(2, 1)
        assertEquals(MyDrawView.TUBE_WIDTH, layout.boardWidth)
        assertEquals(4 * MyDrawView.BALL_DIAMETER + 2 * MyDrawView.BALL_PADDING + TwoRowsLayout.TUBE_VERTICAL_PADDING, layout.boardHeight)
        assertEquals(1, layout.numberOfUpperTubes)
        assertEquals(0, layout.lowerTubesLeft)
        assertEquals(2 * MyDrawView.BALL_DIAMETER + MyDrawView.BALL_PADDING + TwoRowsLayout.TUBE_VERTICAL_PADDING, layout.lowerTubesTop)
    }

    // Spielfeld mit Breite 3 und Höhe 1 ergibt oben 2 und unten eine Röhre
    @Test
    fun constructor_3x1() {
        val layout = TwoRowsLayout(3, 1)
        assertEquals(2 * MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING, layout.boardWidth)
        assertEquals(4 * MyDrawView.BALL_DIAMETER + 2 * MyDrawView.BALL_PADDING + TwoRowsLayout.TUBE_VERTICAL_PADDING, layout.boardHeight)
        assertEquals(2, layout.numberOfUpperTubes)
        assertEquals((MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING) / 2, layout.lowerTubesLeft)
        assertEquals(2 * MyDrawView.BALL_DIAMETER + MyDrawView.BALL_PADDING + TwoRowsLayout.TUBE_VERTICAL_PADDING, layout.lowerTubesTop)
    }

    @Test
    fun calculateTransformation_factor1() {
        val layout = TwoRowsLayout(2, 1)
        val w = layout.boardWidth
        val h= layout.boardHeight
        layout.calculateTransformation(w, h)
        assertEquals(1.0f, layout.scaleFactor)
        assertEquals(0.0f, layout.translateX)
        assertEquals(0.0f, layout.translateY)
    }

    @Test
    fun calculateTransformation_factor10() {
        val layout = TwoRowsLayout(2, 1)
        val w = layout.boardWidth * 10
        val h= layout.boardHeight * 10
        layout.calculateTransformation(w, h)
        assertEquals(10.0f, layout.scaleFactor)
        assertEquals(0.0f, layout.translateX)
        assertEquals(0.0f, layout.translateY)
    }

    @Test
    fun ballX_2x1() {
        val layout = TwoRowsLayout(2, 1)
        assertEquals(MyDrawView.BALL_RADIUS + MyDrawView.BALL_PADDING, layout.ballX(0))
        assertEquals(MyDrawView.BALL_RADIUS + MyDrawView.BALL_PADDING, layout.ballX(1))
    }

    @Test
    fun ballX_7x4() {
        val layout = TwoRowsLayout(7, 4)
        // oben 4 Röhren
        assertEquals(MyDrawView.BALL_PADDING + MyDrawView.BALL_RADIUS, layout.ballX(0))
        assertEquals(MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING + MyDrawView.BALL_PADDING + MyDrawView.BALL_RADIUS, layout.ballX(1))
        assertEquals(2 * (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING) + MyDrawView.BALL_PADDING + MyDrawView.BALL_RADIUS, layout.ballX(2))
        assertEquals(3 * (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING) + MyDrawView.BALL_PADDING + MyDrawView.BALL_RADIUS, layout.ballX(3))

        // unten 3 Röhren
        val left = (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING) / 2
        assertEquals( left + MyDrawView.BALL_PADDING + MyDrawView.BALL_RADIUS, layout.ballX(4))
        assertEquals(left + MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING + MyDrawView.BALL_PADDING + MyDrawView.BALL_RADIUS, layout.ballX(5))
        assertEquals(left + 2 * (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING) + MyDrawView.BALL_PADDING + MyDrawView.BALL_RADIUS, layout.ballX(6))
    }

    @Test
    fun ballY_2x1() {
        val layout = TwoRowsLayout(2, 1)
        // = 3 Radien oder 1 1/2 Durchmesser
        assertEquals(MyDrawView.BALL_DIAMETER + MyDrawView.BALL_RADIUS, layout.ballY(0, 0))
        assertEquals(TwoRowsLayout.TUBE_VERTICAL_PADDING + 3 * MyDrawView.BALL_DIAMETER + MyDrawView.BALL_PADDING + MyDrawView.BALL_RADIUS, layout.ballY(1, 0))
    }

    @Test
    fun ballY_5x3() {
        val layout = TwoRowsLayout(5, 3)
        // oben 3 Röhren
        assertEquals(3 * MyDrawView.BALL_DIAMETER + MyDrawView.BALL_RADIUS, layout.ballY(0, 0))
        assertEquals(MyDrawView.BALL_DIAMETER + MyDrawView.BALL_RADIUS, layout.ballY(0, 2))

        // unten 2 Röhren
        val top = 4 * MyDrawView.BALL_DIAMETER + MyDrawView.BALL_PADDING + TwoRowsLayout.TUBE_VERTICAL_PADDING
        assertEquals(top + 3 * MyDrawView.BALL_DIAMETER + MyDrawView.BALL_RADIUS, layout.ballY(3, 0))
        assertEquals(top + MyDrawView.BALL_DIAMETER + MyDrawView.BALL_RADIUS, layout.ballY(3, 2))
        assertEquals(top + MyDrawView.BALL_DIAMETER + MyDrawView.BALL_RADIUS, layout.ballY(4, 2))
    }

    @Test
    fun tubeX_2x1() {
        val layout = TwoRowsLayout(2, 1)
        assertEquals(0, layout.tubeX(0))
        assertEquals(0, layout.tubeX(1))
    }

    @Test
    fun tubeX_3x1() {
        val layout = TwoRowsLayout(3, 1)
        // oben 2 Röhren
        assertEquals(0, layout.tubeX(0))
        assertEquals(MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING, layout.tubeX(1))

        // unten eine Röhre
        assertEquals((MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING) / 2, layout.tubeX(2))
    }

    @Test
    fun virtualX() {
        val layout = TwoRowsLayout(1, 1)
        val w = layout.boardWidth
        val h= layout.boardHeight
        layout.calculateTransformation(w, h)
        assertEquals(0, layout.virtualX(0.0f))
        assertEquals(3, layout.virtualX(3.0f))
    }

    @Test
    fun virtualY() {
        val layout = TwoRowsLayout(1, 1)
        val w = layout.boardWidth
        val h= layout.boardHeight
        layout.calculateTransformation(w, h)
        assertEquals(0, layout.virtualY(0.0f))
        assertEquals(3, layout.virtualY(3.0f))
    }

    @Test
    fun column_2x1() {
        val layout = TwoRowsLayout(1, 1)
        val w = layout.boardWidth
        val h= layout.boardHeight
        layout.calculateTransformation(w, h)

        // Klick auf linke, obere Ecke
        assertEquals(0, layout.column(0, 0))
        assertEquals(0, layout.column(3, 3))

        // Klick auf rechte, untere Ecke
        assertEquals(1, layout.column(w - 1, h - 1))
    }

    @Test
    fun column_3x1() {
        val layout = TwoRowsLayout(3, 1)
        val w = layout.boardWidth
        val h= layout.boardHeight
        layout.calculateTransformation(w, h)

        // Klick auf linke, obere Ecke
        assertEquals(0, layout.column(0, 0))
        assertEquals(0, layout.column(3, 3))

        // Klick zentriert, unten
        assertEquals(2, layout.column(w / 2, h - 1))
    }

    /**
     * inside test
     */
    @Test
    fun isInside_true() {
        val layout = TwoRowsLayout(2, 1)
        // rechte, obere Ecke
        assertTrue(layout.isInside(0, 0))

        // linke, untere Ecke
        assertTrue(layout.isInside(layout.boardWidth -1, layout.boardHeight-1))
    }

    /**
     * outside test
     */
    @Test
    fun isInside_false() {
        val layout = TwoRowsLayout(2, 1)

        // linke, obere Ecke
        assertFalse(layout.isInside(-1, 0))
        assertFalse(layout.isInside(0, -1))

        // rechte, obere Ecke
        assertFalse(layout.isInside(layout.boardWidth, 0))

        // linke, untere Ecke
        assertFalse(layout.isInside(0, layout.boardHeight))

        // zwischen obere und untere Reihe
        assertFalse(layout.isInside(0, layout.boardHeight / 2))
    }
}