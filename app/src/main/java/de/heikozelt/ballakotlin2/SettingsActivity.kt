package de.heikozelt.ballakotlin2

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.heikozelt.ballakotlin2.model.GameState1Up
import de.heikozelt.ballakotlin2.model.GameStateListenerInterface


/**
 * Im Model-View-Controller-Entwurfsmuster gehoert eine Activity in den Bereich View
 * Beim Drehen des Bildschirms wird die Activity zerstört.
 * Es können also keine Daten dauerhaft in einer Activity gespeichert werden. :-(
 */
class SettingsActivity : AppCompatActivity() {
    /**
     * Methode von Activity geerbt
     * wird z.B. aufgerufen, wenn Bildschirm gedreht wird
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val colorsTextView = findViewById<TextView?>(R.id.labelNumberOfColors)
        val colorsSeekBar = findViewById<SeekBar?>(R.id.numberOfColors)
        val extraTubesTextView = findViewById<TextView?>(R.id.labelAdditionalTubes)
        val extraTubesSeekBar = findViewById<SeekBar?>(R.id.additionalTubes)
        val heightTextView = findViewById<TextView?>(R.id.labelHeight)
        val heightSeekBar = findViewById<SeekBar?>(R.id.height)

        fun updateColorsText(i: Int) {
            colorsTextView?.text = getString(R.string.label_number_of_colors, i)
        }

        fun updateExtraTubesText(i: Int) {
            extraTubesTextView?.text = getString(R.string.label_additional_tubes, i)
        }

        fun updateHeightText(i: Int) {
            heightTextView?.text = getString(R.string.label_height, i)
        }

        colorsSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, i: Int, b: Boolean) {
                updateColorsText(seek.progress + MIN_COLORS)
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
            }
        })

        extraTubesSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, i: Int, b: Boolean) {
                updateExtraTubesText(seek.progress + MIN_EXTRA)
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
            }
        })

        heightSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, i: Int, b: Boolean) {
                updateHeightText(seek.progress + MIN_HEIGHT)
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
            }
        })

        //colorsSeekBar?.progress = BallaApplication.NUMBER_OF_COLORS
        //updateColorsText(BallaApplication.NUMBER_OF_COLORS)
        val gs = (application as BallaApplication).getGameState1Up()
        if (gs != null) {
            colorsSeekBar?.progress = gs.getNumberOfColors() - MIN_COLORS
            extraTubesSeekBar?.progress = gs.getInitialExtraTubes() - MIN_EXTRA
            heightSeekBar?.progress = gs.getTubeHeight() - MIN_HEIGHT
            updateColorsText(gs.getNumberOfColors())
            updateExtraTubesText(gs.getInitialExtraTubes())
            updateHeightText(gs.getTubeHeight())
        }
    }

    /**
     * Methode von Activity geerbt
     */
    override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
    }

    /**
     * Methode von Activity geerbt
     */
    override fun onStop() {
        Log.d(TAG, "onStop()")
        super.onStop()
    }

    /**
     * Methode von Activity geerbt
     */
    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
    }

    /**
     * User clicked on ok button
     */
    fun onOkKlicked(vi: View) {
        Log.i(TAG, "user clicked on ok button")
        val colorsSeekBar = findViewById<SeekBar?>(R.id.numberOfColors)
        val extraTubesSeekBar = findViewById<SeekBar?>(R.id.additionalTubes)
        val heightSeekBar = findViewById<SeekBar?>(R.id.height)
        if (colorsSeekBar != null && extraTubesSeekBar != null && heightSeekBar != null) {
            (application as BallaApplication).getGameState1Up()?.actionNewGame(colorsSeekBar.progress + MIN_COLORS, extraTubesSeekBar.progress + MIN_EXTRA, heightSeekBar.progress + MIN_HEIGHT)
        }
        finish()
    }

    /**
     * User clicked on ok button
     */
    fun onCancelKlicked(vi: View) {
        Log.i(TAG, "user clicked on cancel button")
        finish()
    }

    fun onDefaultsKlicked(vi: View) {
        Log.i(TAG, "user clicked on defaults button")
        val colorsSeekBar = findViewById<SeekBar?>(R.id.numberOfColors)
        val extraTubesSeekBar = findViewById<SeekBar?>(R.id.additionalTubes)
        val heightSeekBar = findViewById<SeekBar?>(R.id.height)
        colorsSeekBar?.progress = BallaApplication.NUMBER_OF_COLORS - MIN_COLORS
        extraTubesSeekBar?.progress = BallaApplication.NUMBER_OF_EXTRA_TUBES - MIN_EXTRA
        heightSeekBar?.progress = BallaApplication.TUBE_HEIGHT - MIN_HEIGHT
    }

    companion object {
        private const val TAG = "balla.SettingsActivity"

        private const val MIN_COLORS = 2
        //private const val MAX_COLORS = 13
        private const val MIN_EXTRA = 1
        //private const val MAX_EXTRA = 3
        private const val MIN_HEIGHT = 3
        //private const val MAX_HEIGHT = 8
    }
}