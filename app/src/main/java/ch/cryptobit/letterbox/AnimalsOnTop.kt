package ch.cryptobit.letterbox

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.net.Uri
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.LocaleListCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.util.AtomicFile
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.reflect.Method
import org.json.JSONObject

const val PREFS_NAME = "letterbox_prefs"
const val PREF_ENABLED_LETTERS_PREFIX = "enabled_letters_"
const val PREF_PROGRESS_ENABLED = "progress_enabled"
const val PROGRESS_FILE = "letter_progress.json"
const val PROGRESS_VERSION = 1

/* A letter counts as mastered at this level; mastered letters appear less
 * often in the queue. The level is capped at PROGRESS_MAX_LEVEL. */
const val PROGRESS_THRESHOLD = 3
const val PROGRESS_MAX_LEVEL = 5

/* Levels decay by one per four weeks without seeing the letter, floored
 * at zero. */
const val PROGRESS_DECAY_MS = 4L * 7 * 24 * 60 * 60 * 1000

const val GITLAB_ISSUES_URL = "https://gitlab.com/muelli/letterbox/-/issues"

class AnimalsOnTop : AppCompatActivity() {

    companion object {
        /* Session-only flag, deliberately not persisted: the menu button
         * becomes visible again once the app process is restarted. */
        private var menuHidden = false
    }

    private val TAG = "LetterBox"
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var soundPool: android.media.SoundPool
    private var soundYes = 0
    private var soundNo = 0
    private var soundsLoaded = 0
    private val oneF = 1.0f

    private lateinit var imageWidget: ImageView
    private lateinit var letterWidgets: List<TextView>
    private lateinit var lettersContainer: View
    // Tracks the animal/letter shown last round so loadRound() can rule it out,
    // preventing the same animal from appearing twice in a row.
    private var previousLetter: Char? = null
    // Tracks which physical button held the correct answer last round, so the
    // same button doesn't get it twice in a row (a child could learn the
    // position instead of the letter).
    private var previousButtonIndex: Int? = null

    // Resolved once at launch from the per-app locale (or the system locale
    // when none was picked): it decides which words/letters are used for the
    // whole session.
    private lateinit var currentLanguage: AppLanguage
    private lateinit var currentAlphabet: List<Char>
    private var currentWord: String? = null

    /* Only letters the user enabled in the letter-selection dialog and that
     * actually have animals take part in the game. */
    private lateinit var letterMap: Map<Char, List<Int>>

    /* Upcoming letters of the current queue cycle; kept in memory only, so
     * a fresh app start always deals a freshly shuffled queue. */
    private val letterQueue = mutableListOf<Char>()

    private lateinit var menuButton: ImageButton

    private lateinit var tts: TextToSpeech
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animals_on_top)
        hideSystemBars()

        currentLanguage = loadLanguage()
        currentAlphabet = LanguageMaps.getAlphabet(currentLanguage)
        letterMap = LanguageMaps.getMap(currentLanguage)
            .filterKeys { it in getEnabledLetters(currentLanguage) }
        /* A selection that matches no animal (e.g. animals were dropped
         * in an update) would leave the round loader without letters;
         * fall back to the full map instead. */
        if (letterMap.isEmpty()) {
            Log.w(TAG, "Enabled letters left no animals; using the full map")
            letterMap = LanguageMaps.getMap(currentLanguage)
        }
        Log.i(TAG, "Detected language ${currentLanguage.code}")

        imageWidget = findViewById(R.id.imageView)
        letterWidgets = listOf(
            findViewById(R.id.mainLetter1),
            findViewById(R.id.mainLetter2),
            findViewById(R.id.mainLetter3)
        )
        lettersContainer = findViewById(R.id.lettersContainer)

        menuButton = findViewById(R.id.menu_hamburger)
        menuButton.setOnClickListener {
            /* While hidden, a short tap passes through to the animal image
             * below, so the corner stays touchable like the rest of it. */
            if (!menuHidden) showHamburgerMenu(it) else imageWidget.performClick()
        }
        /* With the menu hidden, a long press on the invisible button
         * is the way to open the menu again. */
        menuButton.setOnLongClickListener {
            showHamburgerMenu(it)
            true
        }
        /* INVISIBLE views receive no touch events, so transparency is
         * done via alpha to keep the long-press escape hatch alive. */
        menuButton.alpha = if (menuHidden) 0f else 1f

        lettersContainer.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateLetterTextSize()
        }

        // Speak the animal's name (in the detected language) whenever its picture is tapped.
        imageWidget.setOnClickListener {
            currentWord?.let { word ->
                if (ttsReady) tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(currentLanguage.locale)
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
                if (!ttsReady) Log.w(TAG, "No TTS voice for ${currentLanguage.code}")
            } else {
                Log.w(TAG, "TextToSpeech init failed with status $status")
            }
        }

        // Hide buttons until sounds are ready
        setButtonsVisible(false)

        soundPool = soundPool()
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            Log.v(TAG, "Loaded sound $sampleId status $status")
            soundsLoaded++
            // Both sounds must be loaded before we start the first round
            if (soundsLoaded >= 2) {
                setButtonsVisible(true)
                loadRound()
            }
        }
        soundYes = soundPool.load(this, R.raw.yes, 1)
        soundNo = soundPool.load(this, R.raw.no, 1)
    }

    /**
     * Dynamically compute the optimal font size so all 3 letter buttons have a
     * uniform, beautifully proportioned text size that is guaranteed to fit any
     * letter of the current language's alphabet without horizontal or vertical clipping.
     */
    private fun updateLetterTextSize() {
        val button = letterWidgets.firstOrNull() ?: return
        if (button.width <= 0 || button.height <= 0) return

        val availableWidth = (button.width - button.paddingLeft - button.paddingRight).coerceAtLeast(1)
        val availableHeight = (button.height - button.paddingTop - button.paddingBottom).coerceAtLeast(1)

        val testPaint = Paint().apply {
            typeface = button.typeface
            textSize = 100f
        }

        // Measure the current alphabet's widest letter and font height at 100px
        // (not just 'A'..'Z': other languages' scripts, e.g. Hangul or Hiragana,
        // have different glyph metrics).
        var maxCharWidth = 0f
        for (c in currentAlphabet) {
            val w = testPaint.measureText(c.toString())
            if (w > maxCharWidth) maxCharWidth = w
        }
        val fontMetrics = testPaint.fontMetrics
        val fontHeight = fontMetrics.descent - fontMetrics.ascent

        // Scale so that even the widest and tallest characters fit with 20% breathing room
        val scaleW = (availableWidth * 0.80f) / maxCharWidth
        val scaleH = (availableHeight * 0.80f) / fontHeight
        val optimalSizePx = 100f * minOf(scaleW, scaleH)

        for (widget in letterWidgets) {
            widget.setTextSize(TypedValue.COMPLEX_UNIT_PX, optimalSizePx)
        }
    }

    // When non-null, the next round repeats this letter (wrong answer retry).
    private var repeatedLetter: Char? = null

    /* True while the current round asks a fresh letter from the queue; false
     * on a retry round after a wrong answer. Progress counts first tries only. */
    private var firstTry = true

    /* Letter queue: each enabled letter appears exactly 2x before the queue is
     * refilled, so the distribution stays even. With progress saving enabled,
     * letters at a mastered effective level appear only 1x, so well-known
     * letters are offered less often. */
    private fun refillLetterQueue(): MutableList<Char> {
        val queue = letterMap.keys.toMutableList()
        if (isProgressEnabled()) {
            queue.addAll(letterMap.keys.filter {
                getLetterProgress(currentLanguage, it) < PROGRESS_THRESHOLD
            })
        } else {
            queue.addAll(letterMap.keys)
        }
        queue.shuffle()
        return queue
    }

    private fun popNextLetter(): Char {
        if (letterQueue.isEmpty()) letterQueue.addAll(refillLetterQueue())
        var letter = letterQueue.removeAt(0)
        /* A refill can put the just-shown letter right back at the front;
         * rotate it behind the remaining entries so rounds do not repeat
         * immediately. */
        if (letter == previousLetter && letterQueue.isNotEmpty()) {
            letterQueue.add(letter)
            letter = letterQueue.removeAt(0)
        }
        return letter
    }

    /* Drops the pending queue so the next round refills it, e.g. when the
     * progress-weighting toggle changes which letters belong in it. */
    private fun clearLetterQueue() {
        letterQueue.clear()
    }

    private fun loadRound() {
        updateLetterTextSize()
        setButtonsVisible(true)
        val shuffledButtons = letterWidgets.shuffled().toMutableList()
        // Guarantee the correct answer doesn't land on the same physical
        // button as last round, regardless of how the shuffle came out.
        if (letterWidgets.indexOf(shuffledButtons[0]) == previousButtonIndex) {
            val tmp = shuffledButtons[0]
            shuffledButtons[0] = shuffledButtons[1]
            shuffledButtons[1] = tmp
        }
        previousButtonIndex = letterWidgets.indexOf(shuffledButtons[0])

        /* On a retry after a wrong answer the same letter is asked again; a
         * fresh round pops the next letter off the queue. */
        val isRetry = repeatedLetter != null && repeatedLetter in letterMap.keys
        firstTry = !isRetry
        val chosenLetter = if (isRetry) repeatedLetter!! else popNextLetter()
        repeatedLetter = null
        previousLetter = chosenLetter

        val chosenDrawable = letterMap[chosenLetter]!!.random()
        currentWord = LanguageMaps.getWord(chosenDrawable, currentLanguage)
        imageWidget.setImageDrawable(
            ResourcesCompat.getDrawable(resources, chosenDrawable, null)
        )

        /* Wrong-answer letters come from the enabled letters first. Only if
         * fewer than 3 enabled letters are available do we mix in random
         * letters from the full alphabet so the game still works. */
        val enabledMinusChosen = letterMap.keys
            .filter { it != chosenLetter }.toMutableList().also { it.shuffle() }
        val wrong1: Char
        val wrong2: Char
        if (enabledMinusChosen.size >= 2) {
            wrong1 = enabledMinusChosen.removeAt(0)
            wrong2 = enabledMinusChosen.removeAt(0)
        } else {
            /* Not enough enabled letters left, fill up from the full alphabet. */
            val extraKeys = currentAlphabet
                .filter { it != chosenLetter && it !in enabledMinusChosen }
                .toMutableList().also { it.shuffle() }
            wrong1 = if (enabledMinusChosen.isNotEmpty()) enabledMinusChosen.removeAt(0) else extraKeys.removeAt(0)
            wrong2 = if (enabledMinusChosen.isNotEmpty()) enabledMinusChosen.removeAt(0) else extraKeys.removeAt(0)
        }

        shuffledButtons[0].text = "$chosenLetter"
        shuffledButtons[1].text = "$wrong1"
        shuffledButtons[2].text = "$wrong2"

        for (btn in shuffledButtons) btn.isClickable = true

        shuffledButtons[0].setOnClickListener { onAnswer(shuffledButtons, it as TextView, correct = true, chosenLetter) }
        shuffledButtons[1].setOnClickListener { onAnswer(shuffledButtons, it as TextView, correct = false, chosenLetter) }
        shuffledButtons[2].setOnClickListener { onAnswer(shuffledButtons, it as TextView, correct = false, chosenLetter) }

        // Reset letter colours from previous round
        for (btn in shuffledButtons) btn.setTextColor(getColor(android.R.color.black))
    }

    private fun onAnswer(allButtons: List<TextView>, tapped: TextView, correct: Boolean,
                         chosenLetter: Char) {
        // Disable all buttons immediately so the child can't double-tap. Also
        // clear any lingering pressed/ripple state: if a second finger answers
        // while a different button is still held down, that other button
        // never gets an ACTION_UP/CANCEL of its own and would otherwise stay
        // visually "stuck" pressed into the next round.
        for (btn in allButtons) {
            btn.isClickable = false
            btn.isPressed = false
        }

        /* Correct first tries increase the letter's level, wrong first tries
         * decrease it; retry rounds do not affect progress at all. */
        if (isProgressEnabled() && firstTry) {
            recordAnswer(currentLanguage, chosenLetter, correct)
        }

        soundPool.play(if (correct) soundYes else soundNo, oneF, oneF, 0, 0, oneF)

        /* Speak the animal's name shortly after the feedback sound, but only
         * after a correct answer, so the child is not confused by hearing the
         * name while still learning the right letter. */
        if (correct) {
            handler.postDelayed({
                currentWord?.let { word ->
                    if (ttsReady) tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }, 400)
        }

        // Flash the tapped letter green (correct) or red (wrong)
        val flashColor = if (correct) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        flashTextColor(tapped, flashColor)

        if (correct) {
            /* Pause long enough for the spoken word to finish, hide the
             * buttons briefly, then move on to a new letter. */
            handler.postDelayed({ setButtonsVisible(false) }, 1550)
            handler.postDelayed({ loadRound() }, 1750)
        } else {
            // Wrong answer: repeat the same letter so the child gets another chance
            repeatedLetter = chosenLetter
            handler.postDelayed({ setButtonsVisible(false) }, 400)
            handler.postDelayed({ loadRound() }, 600)
        }
    }

    private fun flashTextColor(view: TextView, toColor: Int) {
        val originalColor = view.currentTextColor
        ValueAnimator.ofObject(ArgbEvaluator(), originalColor, toColor, originalColor).apply {
            duration = 700
            addUpdateListener { view.setTextColor(it.animatedValue as Int) }
            start()
        }
    }

    private fun setButtonsVisible(visible: Boolean) {
        val v = if (visible) View.VISIBLE else View.INVISIBLE
        for (btn in letterWidgets) btn.visibility = v
    }

    /* A long press on the animal shows author and image-source attribution;
     * see app/src/main/res/drawable/src/LICENSE for the full per-image list. */
    private fun showCreditsDialog() {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION") packageInfo.versionCode
        }
        val html = """
            <big><b>LetterBox</b></big><br>
            Version ${packageInfo.versionName} ($versionCode)<br><br>
            By Tobias Mueller (<a href="https://gitlab.com/muelli">@muelli on GitLab</a>)<br>
            Ruben Barkow-Kuder<br>
            <a href="https://eclabs.de/#kontakt">eclabs.de/#kontakt</a><br><br>
            <b>Animal images from:</b><br>
            Pixabay &#183; Openclipart (CC0) &#183; Vecteezy &#183; Wikimedia Commons (CC BY-SA 4.0) &#183; OpenAI ChatGPT
        """.trimIndent()
        val textView = TextView(this)
        val padding = (16 * resources.displayMetrics.density).toInt()
        textView.setPadding(padding, padding, padding, padding)
        textView.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
        textView.movementMethod = LinkMovementMethod.getInstance()
        val dialog = AlertDialog.Builder(this)
            .setView(textView)
            .setPositiveButton(android.R.string.ok, null)
            .setNeutralButton(R.string.about_feature_request, null)
            .show()
        /* The issue tracker opens only on a long press, so a child exploring
         * the About dialog cannot wander into the browser in two taps. A
         * short tap explains the gate to the adult. */
        val requestButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        requestButton.setOnClickListener {
            Toast.makeText(this, R.string.feature_request_hint, Toast.LENGTH_SHORT).show()
        }
        requestButton.setOnLongClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITLAB_ISSUES_URL)))
                dialog.dismiss()
            } catch (e: ActivityNotFoundException) {
                Log.w(TAG, "No browser can open $GITLAB_ISSUES_URL", e)
                Toast.makeText(this, R.string.no_browser, Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    /* Full-screen immersive mode: no status bar or navigation bar, so a
     * tapping toddler doesn't accidentally leave the app. Swiping from an
     * edge still reveals the bars temporarily, as intended by the OS. */
    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    // Desktop keyboard support: 1/2/3 pick by screen position; a letter
    // key picks whichever button currently shows that letter.
    private val keyToIndex = mapOf(
        KeyEvent.KEYCODE_1 to 0, KeyEvent.KEYCODE_NUMPAD_1 to 0,
        KeyEvent.KEYCODE_2 to 1, KeyEvent.KEYCODE_NUMPAD_2 to 1,
        KeyEvent.KEYCODE_3 to 2, KeyEvent.KEYCODE_NUMPAD_3 to 2
    )

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        val index = keyToIndex[keyCode] ?: letterWidgets.indexOfFirst {
            it.text.firstOrNull()?.equals(event.unicodeChar.toChar(), ignoreCase = true) == true
        }
        val btn = letterWidgets.getOrNull(index) ?: return super.onKeyUp(keyCode, event)
        if (btn.isClickable) btn.performClick()
        return true
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // The bars can be swiped back in; re-hide them once the window regains focus.
        if (hasFocus) hideSystemBars()
    }

    /* --- Language persistence --- */

    /* The chosen language is stored via Android's per-app language mechanism
     * (AppCompatDelegate), so it is already applied before resources load. */
    private fun loadLanguage(): AppLanguage {
        val locale = AppCompatDelegate.getApplicationLocales()[0]
        return if (locale != null) AppLanguage.fromLocale(locale) else AppLanguage.fromSystemLocale()
    }

    /* --- Enabled letters --- */

    /* The letter selection is persisted per language in SharedPreferences;
     * without a stored selection the full alphabet is enabled. */
    private fun getEnabledLetters(language: AppLanguage): Set<Char> {
        val stored = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getString(PREF_ENABLED_LETTERS_PREFIX + language.code, null)
        if (stored.isNullOrEmpty()) {
            return LanguageMaps.getAlphabet(language).toSet()
        }
        return stored.toCharArray().toSet()
    }

    private fun saveEnabledLetters(language: AppLanguage, letters: Set<Char>) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit().putString(PREF_ENABLED_LETTERS_PREFIX + language.code,
                String(letters.toCharArray())).apply()
    }

    /* --- Progress --- */

    private fun isProgressEnabled(): Boolean {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getBoolean(PREF_PROGRESS_ENABLED, false)
    }

    private fun setProgressEnabled(enabled: Boolean) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit().putBoolean(PREF_PROGRESS_ENABLED, enabled).apply()
    }

    /* AtomicFile writes via a temporary backup so an interrupted write
     * cannot corrupt the stored file; on read it restores that backup
     * automatically. */
    private val progressFile by lazy { AtomicFile(File(filesDir, PROGRESS_FILE)) }

    /* The progress document is loaded from disk once and then mutated in
     * memory; writes only happen when progressWritable is true. A file that
     * is unreadable or carries an unknown version is left untouched so a
     * future version can still migrate it, and nothing is written for the
     * rest of the session. */
    private var progressRoot: JSONObject? = null
    private var progressWritable = true

    /* Progress lives in its own JSON file (excluded from cloud backup but
     * allowed to move with device-to-device transfer) with a version field,
     * so a future format change can be migrated instead of deleted. Per
     * language and letter it stores a level and when the letter was last
     * seen:
     * {"version":1,"languages":{"en":{"A":{"level":2,"lastSeen":...}}}}
     */
    private fun loadProgressJson(): JSONObject {
        progressRoot?.let { return it }
        val root = try {
            JSONObject(String(progressFile.readFully()))
        } catch (e: FileNotFoundException) {
            return freshProgressRoot()
        } catch (e: Exception) {
            Log.w(TAG, "Progress file unreadable; keeping it intact for a future migration", e)
            progressWritable = false
            return freshProgressRoot()
        }
        val version = root.optInt("version", -1)
        /* A versioned file whose body does not match the expected shape is
         * corrupt: optJSONObject would silently treat foreign types as
         * absent and the next write would discard the stored levels. */
        val languages = root.opt("languages")
        if (version != PROGRESS_VERSION || (languages != null && languages !is JSONObject)) {
            Log.w(TAG, "Progress file unreadable (version=$version); keeping it intact for a future migration")
            progressWritable = false
            return freshProgressRoot()
        }
        return root.also { progressRoot = it }
    }

    private fun freshProgressRoot(): JSONObject =
        JSONObject().put("version", PROGRESS_VERSION).also { progressRoot = it }

    private fun saveProgressJson(root: JSONObject, force: Boolean = false): Boolean {
        if (!force && !progressWritable) {
            Log.w(TAG, "Progress not written: stored file needs a future migration")
            return false
        }
        val stream = try {
            progressFile.startWrite()
        } catch (e: IOException) {
            Log.w(TAG, "Could not open progress file for writing", e)
            return false
        }
        return try {
            stream.write(root.toString().toByteArray(Charsets.UTF_8))
            progressFile.finishWrite(stream)
            true
        } catch (e: IOException) {
            Log.w(TAG, "Could not write progress file", e)
            progressFile.failWrite(stream)
            false
        }
    }

    /* Levels decay by one per PROGRESS_DECAY_MS since last seen, floored at
     * zero. The stored level is left untouched here and only rewritten when
     * the letter is answered again. */
    private fun effectiveLevel(level: Int, lastSeen: Long, now: Long): Int {
        val decay = ((now - lastSeen).coerceAtLeast(0) / PROGRESS_DECAY_MS).toInt()
        return (level - decay).coerceAtLeast(0)
    }

    /* Returns the letter's effective (decay-adjusted) level in the given
     * language; zero when the letter was never seen. */
    private fun getLetterProgress(language: AppLanguage, letter: Char): Int {
        val entry = loadProgressJson()
            .optJSONObject("languages")
            ?.optJSONObject(language.code)
            ?.optJSONObject(letter.toString()) ?: return 0
        return effectiveLevel(
            entry.optInt("level"), entry.optLong("lastSeen"),
            System.currentTimeMillis()
        )
    }

    /* First the stored level decays to its current value, then a correct
     * first try adds one level and a wrong one subtracts one, clamped to
     * 0..PROGRESS_MAX_LEVEL, and lastSeen is set to now. Retry rounds never
     * reach this method. */
    private fun recordAnswer(language: AppLanguage, letter: Char, correct: Boolean) {
        val now = System.currentTimeMillis()
        val root = loadProgressJson()
        if (!progressWritable) return
        val languages = root.optJSONObject("languages")
            ?: JSONObject().also { root.put("languages", it) }
        val langObj = languages.optJSONObject(language.code)
            ?: JSONObject().also { languages.put(language.code, it) }
        val level = langObj.optJSONObject(letter.toString())?.let {
            effectiveLevel(it.optInt("level"), it.optLong("lastSeen"), now)
        } ?: 0
        val newLevel = (level + if (correct) 1 else -1).coerceIn(0, PROGRESS_MAX_LEVEL)
        langObj.put(letter.toString(), JSONObject()
            .put("level", newLevel)
            .put("lastSeen", now))
        saveProgressJson(root)
    }

    /* Removes all stored progress for every language; the versioned file
     * itself is kept. An explicit reset may overwrite a file that regular
     * writes refuse to touch - the user asked for it, and the result is a
     * valid file again, so writing is re-enabled afterwards. */
    private fun resetAllProgress() {
        if (saveProgressJson(freshProgressRoot(), force = true)) {
            progressWritable = true
            Log.i(TAG, "All progress reset to zero")
        } else {
            Log.w(TAG, "Progress reset applied in memory only; stored file left untouched")
        }
    }

    /* --- Hamburger popup menu --- */

    private fun showHamburgerMenu(anchor: View) {
        val popup = PopupMenu(this, anchor, Gravity.END)
        val menu = popup.menu
        /* Show the current language with its flag as the first menu item. */
        val langItem = menu.add(0, R.id.menu_language, 0, currentLanguage.displayName)
        langItem.setIcon(currentLanguage.flagResId)
        /* Show how many letters are currently enabled, e.g. "Letters (5/26)";
         * when all are enabled the plain label is enough. */
        val enabledCount = getEnabledLetters(currentLanguage)
            .count { it in currentAlphabet }
        val lettersLabel = if (enabledCount == currentAlphabet.size) {
            getString(R.string.menu_letters)
        } else {
            "${getString(R.string.menu_letters)} ($enabledCount/${currentAlphabet.size})"
        }
        val lettersItem = menu.add(0, R.id.menu_letters, 1, lettersLabel)
        lettersItem.setIcon(R.drawable.ic_letters)
        /* The checked state is drawn as the item's icon, aligned with the
         * other icons; isCheckable would add a second checkmark on the right. */
        val progressItem = menu.add(0, R.id.menu_progress, 2, R.string.menu_progress)
        progressItem.setIcon(if (isProgressEnabled()) R.drawable.ic_check_box else R.drawable.ic_check_box_outline)
        val hideItem = menu.add(0, R.id.menu_hide, 3, R.string.menu_hide)
        hideItem.setIcon(if (menuHidden) R.drawable.ic_check_box else R.drawable.ic_check_box_outline)
        val aboutItem = menu.add(0, R.id.menu_about, 4, R.string.menu_about)
        aboutItem.setIcon(android.R.drawable.ic_menu_info_details)
        forceShowPopupIcons(popup)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_language -> {
                    showLanguageMenu(anchor)
                    true
                }
                R.id.menu_letters -> {
                    showLetterSelectionMenu()
                    true
                }
                R.id.menu_progress -> {
                    val newState = !isProgressEnabled()
                    setProgressEnabled(newState)
                    /* Drop the letter queue so the new weighting takes effect
                     * on the next round. */
                    clearLetterQueue()
                    Log.i(TAG, "Progress saving ${if (newState) "enabled" else "disabled"}")
                    /* When progress saving is turned off, ask whether to reset
                     * all stored progress for every language to zero. */
                    if (!newState) {
                        AlertDialog.Builder(this)
                            .setTitle(R.string.progress_reset_title)
                            .setMessage(R.string.progress_reset_message)
                            .setPositiveButton(R.string.progress_reset_confirm) { _, _ ->
                                resetAllProgress()
                            }
                            .setNegativeButton(R.string.progress_reset_keep, null)
                            .show()
                    }
                    true
                }
                R.id.menu_about -> {
                    showCreditsDialog()
                    true
                }
                R.id.menu_hide -> {
                    if (menuHidden) {
                        setMenuHidden(false)
                    } else {
                        /* Explain the long-press escape hatch before the
                         * button disappears. */
                        AlertDialog.Builder(this)
                            .setTitle(R.string.hide_menu_confirm_title)
                            .setMessage(R.string.hide_menu_confirm_message)
                            .setPositiveButton(android.R.string.ok) { _, _ -> setMenuHidden(true) }
                            .setNegativeButton(android.R.string.cancel, null)
                            .show()
                    }
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun setMenuHidden(hidden: Boolean) {
        menuHidden = hidden
        menuButton.alpha = if (hidden) 0f else 1f
        Log.i(TAG, "Menu button ${if (hidden) "hidden" else "visible"}")
    }

    /* --- Letter selection dialog --- */

    private fun showLetterSelectionMenu() {
        val allLetters = LanguageMaps.getAlphabet(currentLanguage)
        val top10 = LanguageMaps.getTop10Letters(currentLanguage)
        var enabled = getEnabledLetters(currentLanguage).toMutableSet()
        val progressOn = isProgressEnabled()

        val dialogView = layoutInflater.inflate(R.layout.letter_selection_dialog, null)
        val grid = dialogView.findViewById<LinearLayout>(R.id.letterGrid)
        val toggleButtons = mutableMapOf<Char, ToggleButton>()

        /* Portrait uses six columns. Landscape uses at least seven columns and
         * adds columns when necessary so the grid never exceeds four rows. */
        val columns = if (
            resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        ) {
            maxOf(7, (allLetters.size + 3) / 4)
        } else {
            6
        }
        var row: LinearLayout? = null
        for ((index, letter) in allLetters.withIndex()) {
            if (index % columns == 0) {
                row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                grid.addView(row)
            }
            val container = android.widget.FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(4, 4, 4, 4)
                }
            }
            val tb = ToggleButton(this).apply {
                textSize = 28f
                text = letter.toString()
                textOn = letter.toString()
                textOff = letter.toString()
                isChecked = letter in enabled
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
                )
                updateToggleBackground(this)
                setOnClickListener {
                    if (isChecked) {
                        enabled.add(letter)
                    } else {
                        enabled.remove(letter)
                    }
                    updateToggleBackground(this)
                }
            }
            container.addView(tb)

            val count = getLetterProgress(currentLanguage, letter)
            if (progressOn && count > 0) {
                val density = resources.displayMetrics.density
                val mastered = count >= PROGRESS_THRESHOLD
                val badge = TextView(this).apply {
                    text = count.toString()
                    textSize = 11f
                    setTextColor(Color.parseColor(if (mastered) "#FFFFFF" else "#616161"))
                    gravity = Gravity.CENTER
                    background = android.graphics.drawable.GradientDrawable().apply {
                        shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                        setColor(Color.parseColor(if (mastered) "#4CAF50" else "#F5F5F5"))
                        setStroke(
                            (1 * density).toInt(),
                            Color.parseColor(if (mastered) "#2E7D32" else "#BDBDBD")
                        )
                        /* Corner radii order: top-left, top-right, bottom-right,
                         * bottom-left. Only the bottom-left corner is rounded. */
                        cornerRadii = floatArrayOf(
                            0f, 0f,
                            0f, 0f,
                            0f, 0f,
                            10 * density, 10 * density
                        )
                    }
                    elevation = 8 * density
                    val badgeSize = (22 * density).toInt()
                    layoutParams = android.widget.FrameLayout.LayoutParams(
                        badgeSize,
                        badgeSize,
                        Gravity.TOP or Gravity.END
                    )
                    setOnClickListener { tb.performClick() }
                }
                container.addView(badge)
            }

            toggleButtons[letter] = tb
            row!!.addView(container)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        fun applyAndClose() {
            if (enabled.isEmpty()) {
                /* Prevent disabling all letters - re-enable everything */
                enabled = allLetters.toMutableSet()
            }
            saveEnabledLetters(currentLanguage, enabled)
            dialog.dismiss()
            recreate()
        }

        dialogView.findViewById<android.widget.Button>(R.id.btnAll).setOnClickListener {
            for (letter in allLetters) {
                toggleButtons[letter]?.let { tb ->
                    tb.isChecked = true
                    updateToggleBackground(tb)
                }
                enabled.add(letter)
            }
        }
        dialogView.findViewById<android.widget.Button>(R.id.btnTop10).setOnClickListener {
            for (letter in allLetters) {
                val on = letter in top10
                toggleButtons[letter]?.let { tb ->
                    tb.isChecked = on
                    updateToggleBackground(tb)
                }
                if (on) enabled.add(letter) else enabled.remove(letter)
            }
        }
        val onlyABtn = dialogView.findViewById<android.widget.Button>(R.id.btnOnlyA)
        /* The label carries the first letter of the current alphabet instead
         * of hardcoding 'A', which doesn't exist in Japanese/Korean. */
        val firstLetter = allLetters.first()
        onlyABtn.text = getString(R.string.letters_only_a, firstLetter.toString())
        onlyABtn.setOnClickListener {
            for (letter in allLetters) {
                val on = letter == firstLetter
                toggleButtons[letter]?.let { tb ->
                    tb.isChecked = on
                    updateToggleBackground(tb)
                }
                if (on) enabled.add(letter) else enabled.remove(letter)
            }
        }
        val okBtn = dialogView.findViewById<ImageButton>(R.id.btnOK)
        okBtn.setOnClickListener {
            applyAndClose()
        }
        val cancelBtn = dialogView.findViewById<ImageButton>(R.id.btnCancel)
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.apply {
            /* Replace the inset, rounded AlertDialog window with an opaque
             * edge-to-edge surface that covers the complete app window. */
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.WHITE))
            decorView.setPadding(0, 0, 0, 0)
            setLayout(
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.MATCH_PARENT
            )
            decorView.systemUiVisibility = window.decorView.systemUiVisibility
        }
    }

    private fun updateToggleBackground(tb: ToggleButton) {
        if (tb.isChecked) {
            tb.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorPrimary))
            tb.setTextColor(Color.WHITE)
        } else {
            tb.setBackgroundColor(Color.parseColor("#E0E0E0"))
            tb.setTextColor(Color.BLACK)
        }
    }

    /* Force-show icons in a popup menu (PopupMenu hides them by default). */
    private fun forceShowPopupIcons(popup: PopupMenu) {
        try {
            val field = popup.javaClass.getDeclaredField("mPopup")
            field.isAccessible = true
            val menuPopupHelper = field.get(popup)
            val method: Method = menuPopupHelper.javaClass.getMethod("setForceShowIcon", Boolean::class.javaPrimitiveType)
            method.invoke(menuPopupHelper, true)
        } catch (e: Exception) {
            Log.w(TAG, "Could not force show icons in PopupMenu: " + e.message)
        }
    }

    /* --- Language selection popup --- */

    private fun showLanguageMenu(anchor: View) {
        val popup = PopupMenu(this, anchor, Gravity.END)
        val menu = popup.menu
        AppLanguage.entries.forEachIndexed { index, lang ->
            val menuItem = menu.add(0, index, index, lang.displayName)
            menuItem.setIcon(lang.flagResId)
            if (lang == currentLanguage) {
                menuItem.isCheckable = true
                menuItem.isChecked = true
            }
        }
        forceShowPopupIcons(popup)
        popup.setOnMenuItemClickListener { item ->
            val lang = AppLanguage.entries[item.itemId]
            Log.i(TAG, "Language changed to " + lang.displayName)
            /* Persists the choice and recreates the activity with the new
             * locale; also exposes it in system per-app language settings. */
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang.code))
            true
        }
        popup.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        soundPool.release()
        tts.stop()
        tts.shutdown()
    }
}
