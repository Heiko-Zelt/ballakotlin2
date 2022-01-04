package de.heikozelt.ballakotlin2.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.heikozelt.ballakotlin2.BallaApplication
import de.heikozelt.ballakotlin2.R


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

        val colorsTextView = findViewById<TextView?>(R.id.settings_label_number_of_colors)
        val colorsSeekBar = findViewById<SeekBar?>(R.id.settings_seekbar_number_of_colors)
        val extraTubesTextView = findViewById<TextView?>(R.id.settings_label_additional_tubes)
        val extraTubesSeekBar = findViewById<SeekBar?>(R.id.settings_seekbar_additional_tubes)
        val heightTextView = findViewById<TextView?>(R.id.settings_label_height)
        val heightSeekBar = findViewById<SeekBar?>(R.id.settings_seekbar_height)

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
                Log.d(TAG, "colorsSeekBar?.setOnSeekBarChangeListener(i=$i)")
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
                Log.d(TAG, "extraTubesSeekBar?.setOnSeekBarChangeListener(i=$i)")
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
                Log.d(TAG, "heightSeekBar?.setOnSeekBarChangeListener(i=$i)")
                updateHeightText(seek.progress + MIN_HEIGHT)
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
            }
        })


        /*
        val controller = (application as BallaApplication).gameController
        if (controller != null) {
            colorsSeekBar?.progress = controller.getNumberOfColors() - MIN_COLORS
            extraTubesSeekBar?.progress = controller.getInitialExtraTubes() - MIN_EXTRA
            heightSeekBar?.progress = controller.getTubeHeight() - MIN_HEIGHT
        }
        */

        // get data from intent instead of "global variable"
        val bundle = intent.extras
        val bundleColors = bundle?.getInt("number_of_colors")
        Log.d(TAG, "bundleColors: $bundleColors")
        if (bundleColors != null) {
            val value = bundleColors - MIN_COLORS
            colorsSeekBar?.progress = value
            updateColorsText(bundleColors)
        }
        val bundleExtra = bundle?.getInt("extra_tubes")
        Log.d(TAG, "bundleExtra: $bundleExtra")
        if (bundleExtra != null) {
            val value = bundleExtra - MIN_EXTRA
            extraTubesSeekBar?.progress = value
            updateExtraTubesText(bundleExtra)
        }
        val bundleHeight = bundle?.getInt("height")
        Log.d(TAG, "bundleHeight: $bundleHeight")
        if (bundleHeight != null) {
            val value = bundleHeight - MIN_EXTRA
            heightSeekBar?.progress = bundleHeight - MIN_HEIGHT
            updateHeightText(bundleHeight)
        }

        findViewById<View?>(R.id.settings_btn_ok)?.setOnClickListener {
            Log.i(TAG, "user clicked on ok button")
            if (colorsSeekBar != null && extraTubesSeekBar != null && heightSeekBar != null) {
                /*
                val colors = colorsSeekBar.progress + MIN_COLORS
                val extra = extraTubesSeekBar.progress + MIN_EXTRA
                val height = heightSeekBar.progress + MIN_HEIGHT
                (application as BallaApplication).gameController?.actionNewGame(
                    colors,
                    extra,
                    height
                )
                */
                val resultIntent = Intent()
                val resultBundle = Bundle()
                resultBundle.putInt("number_of_colors", colorsSeekBar.progress + MIN_COLORS)
                resultBundle.putInt("extra_tubes", extraTubesSeekBar.progress + MIN_EXTRA)
                resultBundle.putInt("height", heightSeekBar.progress + MIN_HEIGHT)
                resultIntent.putExtras(resultBundle)
                setResult(RESULT_OK, resultIntent)
            }
            finish()
        }

        findViewById<View?>(R.id.settings_btn_cancel)?.setOnClickListener {
            Log.i(TAG, "user clicked on cancel button")
            setResult(RESULT_CANCELED, intent)
            finish()
        }

        findViewById<View?>(R.id.settings_btn_defaults)?.setOnClickListener {
            Log.i(TAG, "user clicked on defaults button")
            colorsSeekBar?.progress = BallaApplication.NUMBER_OF_COLORS - MIN_COLORS
            extraTubesSeekBar?.progress = BallaApplication.NUMBER_OF_EXTRA_TUBES - MIN_EXTRA
            heightSeekBar?.progress = BallaApplication.TUBE_HEIGHT - MIN_HEIGHT
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