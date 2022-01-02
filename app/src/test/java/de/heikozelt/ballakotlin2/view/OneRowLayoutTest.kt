package de.heikozelt.ballakotlin2.view

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class OneRowLayoutTest {

    // Spielfeld mit Breite 1 und Höhe 1
    @Test
    fun constructor_1x1() {
        val layout = OneRowLayout(1, 1)
        // Breite
        assertEquals(MyDrawView.TUBE_WIDTH, layout.boardWidth)
        assertEquals(2 * MyDrawView.BALL_DIAMETER + MyDrawView.BALL_PADDING, layout.boardHeight)
    }

    @Test
    fun calculateTranslation() {
        val layout = OneRowLayout(1, 1)
        val w = layout.boardWidth
        val h= layout.boardHeight
        layout.calculateTranslation(w, h)
        assertEquals(1.0f, layout.scaleFactor)
        assertEquals(0.0f, layout.transX)
        assertEquals(0.0f, layout.transY)
    }

    @Test
    fun ballX_1x1() {
        val layout = OneRowLayout(1, 1)
        assertEquals(MyDrawView.BALL_RADIUS + MyDrawView.BALL_PADDING, layout.ballX(0))
    }

    @Test
    fun ballX_7x4() {
        val layout = OneRowLayout(7, 4)
        assertEquals(7 * (MyDrawView.TUBE_WIDTH + MyDrawView.TUBE_PADDING) + MyDrawView.BALL_PADDING + MyDrawView.BALL_RADIUS, layout.ballX(7))
    }

    @Test
    fun ballY_1x1() {
        val layout = OneRowLayout(1, 1)
        // = 3 Radien oder 1 1/2 Durchmesser
        assertEquals(MyDrawView.BALL_DIAMETER + MyDrawView.BALL_RADIUS, layout.ballY(0, 0))
    }

    @Test
    fun ballY_5x3() {
        val layout = OneRowLayout(5, 3)
        // Ball ist oben, rechts
        assertEquals(MyDrawView.BALL_RADIUS, layout.ballY(5, 3))
    }

    @Test
    fun virtualX() {
        val layout = OneRowLayout(1, 1)
        val w = layout.boardWidth
        val h= layout.boardHeight
        layout.calculateTranslation(w, h)
        assertEquals(0, layout.virtualX(0.0f))
        assertEquals(3, layout.virtualX(3.0f))
    }

    @Test
    fun virtualY() {
        val layout = OneRowLayout(1, 1)
        val w = layout.boardWidth
        val h= layout.boardHeight
        layout.calculateTranslation(w, h)
        assertEquals(0, layout.virtualY(0.0f))
        assertEquals(3, layout.virtualY(3.0f))
    }

    @Test
    fun column() {
        val layout = OneRowLayout(1, 1)
        val w = layout.boardWidth
        val h= layout.boardHeight
        layout.calculateTranslation(w, h)
        assertEquals(0, layout.column(0, 0))
        assertEquals(0, layout.column(3, 3))
    }

    /**
     * inside test
     */
    @Test
    fun isInside_true() {
        val layout = OneRowLayout(1, 1)
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
        val layout = OneRowLayout(1, 1)

        // linke, obere Ecke
        assertFalse(layout.isInside(-1, 0))
        assertFalse(layout.isInside(0, -1))

        // rechte, obere Ecke
        assertFalse(layout.isInside(layout.boardWidth, 0))

        // linke, untere Ecke
        assertFalse(layout.isInside(0, layout.boardHeight))
    }
}