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
class DimensionsActivity : AppCompatActivity() {
    /**
     * Methode von Activity geerbt
     * wird z.B. aufgerufen, wenn Bildschirm gedreht wird
     */

    private var colorsTextView: TextView? = null
    private var colorsSeekBar:SeekBar? = null
    private var extraTubesTextView: TextView? = null
    private var extraTubesSeekBar:SeekBar? = null
    private var heightTextView: TextView? = null
    private var heightSeekBar:SeekBar? = null
    private var okBtn: View? = null
    private var cancelBtn: View? = null
    private var defaultsBtn: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dimensions)

        colorsTextView = findViewById(R.id.dimensions_label_number_of_colors)
        colorsSeekBar = findViewById(R.id.dimensions_seekbar_number_of_colors)
        extraTubesTextView = findViewById(R.id.dimensions_label_additional_tubes)
        extraTubesSeekBar = findViewById(R.id.dimensions_seekbar_additional_tubes)
        heightTextView = findViewById(R.id.dimensions_label_height)
        heightSeekBar = findViewById(R.id.dimensions_seekbar_height)
        okBtn = findViewById(R.id.dimensions_btn_ok)
        cancelBtn = findViewById(R.id.dimensions_btn_cancel)
        defaultsBtn = findViewById(R.id.dimensions_btn_defaults)

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

        // get data from intent instead of "global variables"
        val bundle = intent.extras
        val bundleColors = bundle?.getInt("number_of_colors")
        //val bundleColors = intent.getIntExtra("number_of_colors")
        Log.d(TAG, "bundleColors: $bundleColors")
        bundleColors?.let {
            colorsSeekBar?.progress = it - MIN_COLORS
            updateColorsText(it)
        }
        val bundleExtra = bundle?.getInt("extra_tubes")
        Log.d(TAG, "bundleExtra: $bundleExtra")
        bundleExtra?.let {
            extraTubesSeekBar?.progress = it - MIN_EXTRA
            updateExtraTubesText(it)
        }
        val bundleHeight = bundle?.getInt("height")
        Log.d(TAG, "bundleHeight: $bundleHeight")
        bundleHeight?.let {
            heightSeekBar?.progress = it - MIN_HEIGHT
            updateHeightText(it)
        }

        initButtonsOnClick()
    }

    private fun initButtonsOnClick() {
        okBtn?.setOnClickListener {
            Log.i(TAG, "user clicked on ok button")
            val resultIntent = Intent()
            colorsSeekBar?.let {
                resultIntent.putExtra("number_of_colors", it.progress + MIN_COLORS)
            }
            extraTubesSeekBar?.let {
                resultIntent.putExtra("extra_tubes", it.progress + MIN_EXTRA)
            }
            heightSeekBar?.let {
                resultIntent.putExtra("height", it.progress + MIN_HEIGHT)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        cancelBtn?.setOnClickListener {
            Log.i(TAG, "user clicked on cancel button")
            val resultIntent = Intent()
            setResult(RESULT_CANCELED, resultIntent)
            finish()
        }

        defaultsBtn?.setOnClickListener {
            Log.i(TAG, "user clicked on defaults button")
            colorsSeekBar?.progress = BallaApplication.NUMBER_OF_COLORS - MIN_COLORS
            extraTubesSeekBar?.progress = BallaApplication.NUMBER_OF_EXTRA_TUBES - MIN_EXTRA
            heightSeekBar?.progress = BallaApplication.TUBE_HEIGHT - MIN_HEIGHT
        }
    }

    companion object {
        private const val TAG = "balla.DimsActivity"

        const val MIN_COLORS = 2
        const val MAX_COLORS = 15

        private const val MIN_EXTRA = 1
        //private const val MAX_EXTRA = 3

        private const val MIN_HEIGHT = 3
        //private const val MAX_HEIGHT = 8
    }
}