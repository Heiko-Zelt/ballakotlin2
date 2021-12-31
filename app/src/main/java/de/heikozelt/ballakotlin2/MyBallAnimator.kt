package de.heikozelt.ballakotlin2

import android.animation.ObjectAnimator

/**
 * enthaelt einen Animator und die Ballposition.
 * die Ballposition bezieht sich auf den Spielstand ohne Animationen.
 * so kann jeder Ball und jeder Animator eindeutig identifiziert und wiedergefunden werden.
 */
class MyBallAnimator(val animator: ObjectAnimator, val col: Int, val row: Int) {

    fun end() {
        animator.end()
    }

}