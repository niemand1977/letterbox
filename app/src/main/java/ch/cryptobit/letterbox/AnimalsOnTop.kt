package ch.cryptobit.letterbox

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat

class AnimalsOnTop : AppCompatActivity() {

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

    // Detected once at launch: this app has no language switcher, so the
    // system locale decides which words/letters are used for the whole session.
    private lateinit var currentLanguage: AppLanguage
    private lateinit var currentAlphabet: List<Char>
    private var currentWord: String? = null

    private lateinit var tts: TextToSpeech
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animals_on_top)

        currentLanguage = AppLanguage.fromSystemLocale()
        currentAlphabet = LanguageMaps.getAlphabet(currentLanguage)
        Log.i(TAG, "Detected language ${currentLanguage.code}")

        imageWidget = findViewById(R.id.imageView)
        letterWidgets = listOf(
            findViewById(R.id.mainLetter1),
            findViewById(R.id.mainLetter2),
            findViewById(R.id.mainLetter3)
        )
        lettersContainer = findViewById(R.id.lettersContainer)

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

    private fun loadRound() {
        updateLetterTextSize()
        val shuffledButtons = letterWidgets.shuffled().toMutableList()
        // Guarantee the correct answer doesn't land on the same physical
        // button as last round, regardless of how the shuffle came out.
        if (letterWidgets.indexOf(shuffledButtons[0]) == previousButtonIndex) {
            val tmp = shuffledButtons[0]
            shuffledButtons[0] = shuffledButtons[1]
            shuffledButtons[1] = tmp
        }
        previousButtonIndex = letterWidgets.indexOf(shuffledButtons[0])

        val letters = currentAlphabet.toMutableSet()
        // Remove the previous round's letter before picking, so the next
        // animal is guaranteed to differ from the one just shown — but only
        // when this is a fresh round, not a retry after a wrong answer.
        val chosenLetter = if (repeatedLetter != null && repeatedLetter in letters) {
            repeatedLetter!!
        } else {
            previousLetter?.let { letters.remove(it) }
            letters.pop()
        }
        repeatedLetter = null
        previousLetter = chosenLetter
        Log.d(TAG, "Chosen letter $chosenLetter")

        val chosenDrawable = LanguageMaps.getMap(currentLanguage)[chosenLetter]!!.random()
        currentWord = LanguageMaps.getWord(chosenDrawable, currentLanguage)
        imageWidget.setImageDrawable(
            ResourcesCompat.getDrawable(resources, chosenDrawable, null)
        )

        // Pick two wrong letters from the remaining alphabet
        letters.remove(chosenLetter)
        val wrong1 = letters.pop()
        val wrong2 = letters.pop()

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
        // Disable all buttons immediately so the child can't double-tap
        for (btn in allButtons) btn.isClickable = false

        soundPool.play(if (correct) soundYes else soundNo, oneF, oneF, 0, 0, oneF)

        // Flash the tapped letter green (correct) or red (wrong)
        val flashColor = if (correct) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        flashTextColor(tapped, flashColor)

        if (correct) {
            // Correct answer: move on to a new letter after a short pause
            handler.postDelayed({ loadRound() }, 800)
        } else {
            // Wrong answer: repeat the same letter so the child gets another chance
            repeatedLetter = chosenLetter
            handler.postDelayed({ loadRound() }, 400)
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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        soundPool.release()
        tts.stop()
        tts.shutdown()
    }
}
