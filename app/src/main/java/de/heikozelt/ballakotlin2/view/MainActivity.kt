package de.heikozelt.ballakotlin2.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.widget.PopupMenu
import de.heikozelt.ballakotlin2.BallaApplication
import de.heikozelt.ballakotlin2.GameController
import de.heikozelt.ballakotlin2.R
import de.heikozelt.ballakotlin2.model.GameObserverInterface
import kotlinx.coroutines.Dispatchers.Main
import android.app.Activity
import android.content.Context
import android.view.*
import android.widget.PopupWindow


/**
 * Im Model-View-Controller-Entwurfsmuster gehoert eine Activity in den Bereich View
 * Beim Drehen des Bildschirms wird die Activity zerstört.
 * Es können also keine Daten dauerhaft in einer Activity gespeichert werden. :-(
 */
class MainActivity : AppCompatActivity(), GameObserverInterface {

    private var gameController: GameController? = null

    private var settingsResult: ActivityResultLauncher<Intent>? = null
    private var togglesResult: ActivityResultLauncher<Intent>? = null

    /**
     * GUI widgets
     */
    private var btnReset: View? = null
    private var btnUndo: View? = null
    private var btnLightbulb: View? = null
    private var btnBurger: View? = null
    private var drawView: MyDrawView? = null

    private var playAnimations: Boolean = true

    /**
     * Methode von Activity geerbt
     * wird z.B. aufgerufen, wenn Bildschirm gedreht wird
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        btnReset = findViewById(R.id.main_btn_reset_game)
        btnUndo = findViewById(R.id.main_btn_undo)
        btnLightbulb = findViewById(R.id.main_btn_lightbulb)
        btnBurger = findViewById(R.id.main_btn_burger_menu)
        drawView = findViewById(R.id.my_draw_view)

        // selber Referenz merken
        // Todo: Das ist keine Setter Injection!!!
        gameController = (application as BallaApplication).gameController

        Log.i(TAG, "injecting game state")
        drawView?.setGameController(gameController)
        gameController?.registerFeedbackContext(Main)
        gameController?.registerGameObserver(this)

        restoreWidgetsStatus()
        registerDimensionsResult()
        registerTogglesResult()
        initPopupMenu()
        initOnClick()

        val app = application as BallaApplication
        Log.d(TAG, "MainActivity.onCreate() playAnimations=$playAnimations")
        playAnimations = app.playAnimations
        injectSettings()

        Log.i(TAG, "invalidating / redrawing view")
        drawView?.invalidate()
    }

    private fun injectSettings() {
        drawView?.playAnimations = playAnimations
    }

    private fun restoreWidgetsStatus() {
        // Wenn der Bildschirm gedreht wird, dann wird die Activity neu instanziiert.
        // Status-Informationen der View gehen verloren.
        val gs = gameController?.getGameState()
        if (gs != null) {
            // Wenn das Puzzle bereits gelöst war, dann verschwindet
            // das Glückwunsch-Popup und muss erneut aufpoppen.
            if (gs.isSolved()) {
                puzzleSolved()
            }

            // Undo-Button-Status wiederherstellen
            enableUndoAndReset(gs.moveLog.isNotEmpty())
        }
        val allowed = gameController?.isCheatAllowed()
        if (allowed != null) {
            enableCheat(allowed)
        }
        val helpAvailable = gameController?.isHelpAvailable()
        if (helpAvailable != null) {
            enableHelp(helpAvailable)
        }
    }

    private fun initOnClick() {
        btnReset?.setOnClickListener {
            Log.i(TAG, "user clicked on reset game button")
            gameController?.actionResetGame()
        }

        btnUndo?.setOnClickListener {
            Log.i(TAG, "user clicked on undo button")
            gameController?.actionUndo()
        }

        btnLightbulb?.setOnClickListener {
            Log.i(TAG, "user clicked on light bulb button")
            gameController?.actionHelp()
        }
    }

    private fun initPopupMenu() {
        // Klick auf Burger Menu öffnet Settings-Activity
        //val imageView = findViewById<View?>(R.id.main_btn_burger_menu)
        // ?.setOnClickListener {imageView: View ->

        val popupMenu = PopupMenu(this, btnBurger)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.item_new_game -> gameController?.actionNewGame()
                R.id.item_dimensions -> openDimensionsActivity()
                R.id.item_cheat -> gameController?.actionCheat()
                R.id.item_sound_and_animations -> openTogglesActivity()
                R.id.item_close -> {
                    (application as BallaApplication).saveSettings()
                    finishAndRemoveTask()
                }
                else -> {
                    Toast.makeText(
                        applicationContext,
                        "not implemented",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            true
        }

        popupMenu.inflate(R.menu.popup_menu)

        for (i in 0 until popupMenu.menu.size()) {
            val item = popupMenu.menu.getItem(i)
            val spanString = SpannableString(item.getTitle().toString())
            val end = spanString.length
            spanString.setSpan(RelativeSizeSpan(1.2f), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            item.title = spanString
        }
        // .setOnLongClickListener
        btnBurger?.setOnClickListener {
            Log.i(TAG, "user clicked on menu button")
            try {
                val popup = PopupMenu::class.java.getDeclaredField("mPopup")
                popup.isAccessible = true
                val menu = popup.get(popupMenu)
                menu.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(menu, true)
            } catch (e: Exception) {
                Log.getStackTraceString(e)
            } finally {
                popupMenu.show()
            }
        }
    }

    private fun registerDimensionsResult() {
        settingsResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        )
        {
            Log.d(TAG, "activity result code: ${it.resultCode}")
            if (it.resultCode == RESULT_OK) {
                Log.d(TAG, "RESULT_OK")
                val resultIntent = it.data
                val resultBundle = resultIntent?.extras
                if (resultBundle != null) {
                    val colors = resultBundle.getInt("number_of_colors")
                    val extra = resultBundle.getInt("extra_tubes")
                    val height = resultBundle.getInt("height")
                    // todo: nur wenn sich etwas geändert hat?
                    (application as BallaApplication).gameController.actionNewGame(
                        colors,
                        extra,
                        height
                    )
                }
            } else if (it.resultCode == RESULT_CANCELED) {
                Log.d(TAG, "RESULT_CANCELED")
            } else {
                Log.e(TAG, "unknown activity result code")
            }
        }
    }

    private fun registerTogglesResult() {
        togglesResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        )
        {
            Log.d(TAG, "activity result code: ${it.resultCode}")
            if (it.resultCode == RESULT_OK) {
                Log.d(TAG, "received RESULT_OK")
                val resultIntent = it.data
                val resultBundle = resultIntent?.extras
                if (resultBundle != null) {
                    val sound = resultBundle.getBoolean(TogglesActivity.BUNDLE_SOUND, true)
                    val animations =
                        resultBundle.getBoolean(TogglesActivity.BUNDLE_ANIMATIONS, true)
                    val compSupport =
                        resultBundle.getBoolean(TogglesActivity.BUNDLE_COMPUTER_SUPPORT, true)
                    // todo: Reaktion auf neue Einstellungen
                    // an 3 Stellen ändern BallaApplication, MainActivity und MyDrawView.
                    // das muss doch einfacher gehen
                    val app = application as BallaApplication
                    app.playSound = sound
                    app.playAnimations = animations
                    app.computerSupport = compSupport
                    playAnimations = animations
                    injectSettings()
                }
            } else if (it.resultCode == RESULT_CANCELED) {
                Log.d(TAG, "received RESULT_CANCELED")
            } else {
                Log.e(TAG, "received unknown activity result code")
            }
        }
    }

    private fun openDimensionsActivity() {
        gameController?.let { gc ->
            val launchIntent = Intent(this, DimensionsActivity::class.java)
            launchIntent.putExtra("number_of_colors", gc.getNumberOfColors())
            launchIntent.putExtra("extra_tubes", gc.getInitialExtraTubes())
            launchIntent.putExtra("height", gc.getTubeHeight())
            settingsResult?.launch(launchIntent)
        }
    }

    private fun openTogglesActivity() {
        // todo: sound und animations uebergeben
        val sound = (application as BallaApplication).playSound
        val animations = (application as BallaApplication).playAnimations
        val compSupport = (application as BallaApplication).computerSupport

        val launchIntent = Intent(this, TogglesActivity::class.java).apply {
            putExtra(TogglesActivity.BUNDLE_SOUND, sound)
            putExtra(TogglesActivity.BUNDLE_ANIMATIONS, animations)
            putExtra(TogglesActivity.BUNDLE_COMPUTER_SUPPORT, compSupport)
        }
        togglesResult?.launch(launchIntent)
    }

/*
 * Hauptmenü initialisieren

override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val inflater = menuInflater
    val popup = inflater.inflate(R.menu.burger_menu, menu)
    return true
}

override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when(item.itemId) {
        R.id.item_new_game -> gameController?.actionNewGame()
    }
    return super.onOptionsItemSelected(item)
}

    fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_new_game -> gameController?.actionNewGame()
        }
        return super.onOptionsItemSelected(item)
    }

 */

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
        gameController?.unregisterGameStateListener()
    }

    /**
     * Methode von GameOvbserverInterface geerbt
     */
    override fun holeBallTubeSolved(
        fromCol: Int,
        toCol: Int,
        fromRow: Int,
        toRow: Int,
        color: Int
    ) {
        Log.i(
            TAG,
            "holeBallTubeSolved(fromCol=$fromCol, toCol=$toCol, fromRow=$fromRow, toRow=$toRow, color=$color)"
        )
        drawView?.holeBallTubeSolved(fromCol, toCol, fromRow, toRow, color)
    }

    /**
     * Methode von GameOvbserverInterface geerbt
     */
    override fun liftAndHoleBallTubeSolved(
        fromCol: Int,
        toCol: Int,
        fromRow: Int,
        toRow: Int,
        color: Int
    ) {
        Log.i(
            TAG,
            "liftAndHoleBallTubeSolved(fromCol=$fromCol, toCol=$toCol, fromRow=$fromRow, toRow=$toRow, color=$color)"
        )

        drawView?.liftAndHoleBallTubeSolved(fromCol, toCol, fromRow, toRow, color)
    }

    /**
     * Methode von GameObserverInterface geerbt
     */
    override fun puzzleSolved() {
        Log.i(TAG, "puzzle solved")
        //setTimeout(function() {
        val alertBuilder = AlertDialog.Builder(this)
        with(alertBuilder) {
            setTitle(getString(R.string.alert_title_puzzle_solved))
            setMessage(getString(R.string.alert_text_puzzle_solved))
            setCancelable(false)
            setPositiveButton(getString(R.string.button_ok)) { _, _ ->
                gameController?.actionNewGame()
                /*
                val v = findViewById<MyDrawView?>(R.id.myDrawView)
                v?.resetGameView()
                */
            }
            show()
        }
    }

    override fun newGameToast() {
        Toast.makeText(applicationContext, getString(R.string.toast_new_game), Toast.LENGTH_SHORT)
            .show()
    }

    /**
     * Methode von GameObserverInterface geerbt
     * called after new game, reset or cheat button clicked
     */
    override fun redraw() {
        Log.i(TAG, "redraw()")

        // Todo: Animationen stoppen?

        drawView?.selectBoardLayout()

        // Spielfeld in View einpassen
        drawView?.calculateTranslation()
        // Alle Animationen beenden und angehobenen Ball senken, falls nötig? Warum?
        //v?.flatten()
        drawView?.calculateBalls()
        // neu zeichnen
        drawView?.invalidate()
    }

    /**
     * Methode von GameObserverInterface geerbt
     */
    override fun liftBall(col: Int, row: Int, color: Int) {
        drawView?.liftBall(col, row, color)
    }

    /**
     * Methode von GameObserverInterface geerbt
     */
    override fun dropBall(col: Int, row: Int, color: Int) {
        drawView?.dropBall(col, row, color)
    }

    /**
     * Methode von GameObserverInterface geerbt
     */
    override fun holeBall(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        drawView?.holeBall(fromCol, toCol, fromRow, toRow, color)
    }

    /**
     * Methode von GameObserverInterface geerbt
     */
    override fun liftAndHoleBall(fromCol: Int, toCol: Int, fromRow: Int, toRow: Int, color: Int) {
        drawView?.liftAndHoleBall(fromCol, toCol, fromRow, toRow, color)
    }

    /**
     * Methode von GameObserverInterface geerbt
     */
    override fun enableUndoAndReset(enabled: Boolean) {
        Log.i(TAG, "enableUndoAndReset(${enabled})")
        enableView(btnUndo, enabled)
        enableView(btnReset, enabled)
    }

    /**
     * Methode von GameObserverInterface geerbt
     */
    override fun enableCheat(enabled: Boolean) {
        Log.i(TAG, "enableCheat(${enabled})")
        // todo: menu item enable / disable
    }

    /**
     * Methode von GameObserverInterface geerbt
     */
    override fun enableHelp(enabled: Boolean) {
        Log.i(TAG, "enableHelp(${enabled})")
        enableView(btnLightbulb, enabled)
    }

    /**
     * only to shorten / reuse code
     * enables or disables button (which is a view)
     */
    private fun enableView(view: View?, enabled: Boolean) {
        view?.isEnabled = enabled
        view?.alpha = if (enabled) {
            ALPHA_ENABLED
        } else {
            ALPHA_DISABLED
        }
    }

    companion object {
        private const val TAG = "balla.MainActivity"
        private const val ALPHA_ENABLED = 1.0f
        private const val ALPHA_DISABLED = 0.5f
    }
}