package de.heikozelt.ballakotlin2.view



import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.switchmaterial.SwitchMaterial
//import android.widget.Switch
//import androidx.appcompat.widget.SwitchCompat
import de.heikozelt.ballakotlin2.R

/**
 * Toggles for Sound, Animations & Computer Support
 */
class TogglesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toggles)
        initWidgets()
        initButtonsOnClick()
    }

    private fun initWidgets() {
        val soundSwitch = findViewById<SwitchMaterial?>(R.id.toggles_switch_sound)
        val animationsSwitch = findViewById<SwitchMaterial?>(R.id.toggles_switch_animations)
        val computerSupportSwitch = findViewById<SwitchMaterial?>(R.id.toggles_switch_computer_support)

        val bundle = intent.extras
        val bundleSound = bundle?.getBoolean(BUNDLE_SOUND, true)
        Log.d(TAG, "bundleSound: $bundleSound")
        bundleSound?.let {
            soundSwitch?.isChecked = it
        }
        val bundleAnimations = bundle?.getBoolean(BUNDLE_ANIMATIONS, true)
        bundleAnimations?.let {
            animationsSwitch?.isChecked = it
        }
        val bundleComputerSupport = bundle?.getBoolean(BUNDLE_COMPUTER_SUPPORT, true)
        bundleComputerSupport?.let {
            computerSupportSwitch?.isChecked = it
        }
    }

    private fun initButtonsOnClick() {
        val soundSwitch = findViewById<SwitchMaterial?>(R.id.toggles_switch_sound)
        val animationsSwitch = findViewById<SwitchMaterial?>(R.id.toggles_switch_animations)
        val computerSupportSwitch = findViewById<SwitchMaterial?>(R.id.toggles_switch_computer_support)

        findViewById<View?>(R.id.toggles_btn_ok)?.setOnClickListener {
            Log.i(TAG, "user clicked on ok button")
            if (soundSwitch != null && animationsSwitch != null && computerSupportSwitch != null) {
                val resultIntent = Intent().apply {
                    putExtra(BUNDLE_SOUND, soundSwitch.isChecked)
                    putExtra(BUNDLE_ANIMATIONS, animationsSwitch.isChecked)
                    putExtra(BUNDLE_COMPUTER_SUPPORT, computerSupportSwitch.isChecked)
                }
                setResult(RESULT_OK, resultIntent)
            }

            finish()
        }

        findViewById<View?>(R.id.toggles_btn_cancel)?.setOnClickListener {
            Log.i(TAG, "user clicked on cancel button")
            //setResult(RESULT_CANCELED, intent)
            finish()
        }

        findViewById<View?>(R.id.toggles_btn_defaults)?.setOnClickListener {
            Log.i(TAG, "user clicked on defaults button")
        }
    }


    companion object {
        /**
         * for logging
         */
        private const val TAG = "balla.TogglesActivity"

        const val BUNDLE_SOUND = "sound"
        const val BUNDLE_ANIMATIONS = "animations"
        const val BUNDLE_COMPUTER_SUPPORT = "computer_support"
    }
}