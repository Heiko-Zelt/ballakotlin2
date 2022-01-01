package de.heikozelt.ballakotlin2.view

import android.animation.ObjectAnimator
import android.util.Log

class MyBallAnimators {
    private val animators = mutableListOf<MyBallAnimator>()

    /**
     * find an animator by the position of it's ball
     */
    private fun findAnimator(col: Int, row: Int): MyBallAnimator? {
        Log.d(TAG, "findAnimator(col=$col, row=$row)")
        for (mba in animators) {
            if ((col == mba.col) && (row == mba.row)) {
                Log.d(TAG, "found")
                return mba
            }
        }
        Log.d(TAG, "not found")
        return null
    }

    /**
     * nur falls der Animator tatsaechlich in der Liste ist
     * The method does 2 operations:
     * <ol>
     *     <li>end old animation</li>
     *     <li>remove old animation</li>
     * </ol>
     */
    fun endRemove(col: Int, row: Int) {
        Log.d(TAG, "endAndRemoveAnimator(col=$col, row=$row)")
        val mba = findAnimator(col, row)
        if (mba != null) {
            mba.end()
            animators.remove(mba)
        }
    }

    /*
    fun startAndAddAnimator(animator: ObjectAnimator, col: Int, row: Int) {
        Log.d(TAG, "startAndAddAnimator(col=$col, row=$row)")
        val mba = MyBallAnimator(animator, col, row)
        animator.start()
    }
    */

    /**
     * The method does 4 operations:
     * <ol>
     *     <li>end old animation</li>
     *     <li>remove old an</li>
     *     <li>add new animation</li>
     *     <li>start new animation</li>
     * </ol>
     */
    fun endRemoveAddStart(animator: ObjectAnimator, col: Int, row: Int) {
        Log.d(TAG, "endRemoveAddStart(col=$col, row=$row)")
        endRemove(col, row)
        val mba = MyBallAnimator(animator, col, row)
        animators.add(mba)
        animator.start()
    }

    /**
     * The method does 2 operations:
     * <ol>
     *     <li>end old animation</li>
     *     <li>remove old animation</li>
     * </ol>
     * for all animations
     */
    fun endRemoveAll() {
        for (mba in animators) {
            mba.end()
        }
        animators.clear()
    }

    companion object {
        private const val TAG = "balla.MyBallAnimators"
    }
}