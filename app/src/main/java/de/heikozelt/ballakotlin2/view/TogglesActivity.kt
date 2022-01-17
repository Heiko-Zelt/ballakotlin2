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

    var soundSwitch: SwitchMaterial? = null
    var animationsSwitch: SwitchMaterial? = null
    var computerSupportSwitch: SwitchMaterial? = null
    var okBtn: View? = null
    var cancelBtn: View? = null
    var defaultsBtn: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toggles)

        soundSwitch = findViewById(R.id.toggles_switch_sound)
        animationsSwitch = findViewById(R.id.toggles_switch_animations)
        computerSupportSwitch = findViewById(R.id.toggles_switch_computer_support)
        okBtn = findViewById(R.id.toggles_btn_ok)
        cancelBtn = findViewById(R.id.toggles_btn_cancel)
        defaultsBtn = findViewById(R.id.toggles_btn_defaults)

        initWidgets()
        initButtonsOnClick()
    }

    private fun initWidgets() {
        val bundleSound = intent?.getBooleanExtra(BUNDLE_SOUND, true)
        //Log.d(TAG, "bundleSound: $bundleSound")
        bundleSound?.let {
            soundSwitch?.isChecked = it
        }
        val bundleAnimations = intent?.getBooleanExtra(BUNDLE_ANIMATIONS, true)
        bundleAnimations?.let {
            animationsSwitch?.isChecked = it
        }
        val bundleComputerSupport = intent?.getBooleanExtra(BUNDLE_COMPUTER_SUPPORT, true)
        bundleComputerSupport?.let {
            computerSupportSwitch?.isChecked = it
        }
    }

    private fun initButtonsOnClick() {
        okBtn?.setOnClickListener {
            Log.i(TAG, "user clicked on ok button")
            val resultIntent = Intent()
            soundSwitch?.let {
                resultIntent.putExtra(BUNDLE_SOUND, it.isChecked)
            }
            animationsSwitch?.let {
                resultIntent.putExtra(BUNDLE_ANIMATIONS, it.isChecked)
            }
            computerSupportSwitch?.let {
                resultIntent.putExtra(BUNDLE_COMPUTER_SUPPORT, it.isChecked)
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
            soundSwitch?.isChecked = true
            animationsSwitch?.isChecked = true
            computerSupportSwitch?.isChecked = true
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