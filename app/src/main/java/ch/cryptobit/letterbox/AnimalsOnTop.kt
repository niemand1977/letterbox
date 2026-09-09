package ch.cryptobit.letterbox

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animals_on_top)

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
     * letter in 'A'..'Z' without horizontal or vertical clipping.
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

        // Measure widest letter (typically 'W' or 'M') and font height at 100px
        var maxCharWidth = 0f
        for (c in 'A'..'Z') {
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

    private fun loadRound() {
        updateLetterTextSize()
        val shuffledButtons = letterWidgets.shuffled()

        val letters = ('A'..'Z').toMutableSet()
        // Remove the previous round's letter before picking, so the next
        // animal is guaranteed to differ from the one just shown.
        previousLetter?.let { letters.remove(it) }
        val chosenLetter = letters.pop()
        // Remember this round's letter for the next exclusion check.
        previousLetter = chosenLetter
        Log.d(TAG, "Chosen letter $chosenLetter")

        imageWidget.setImageDrawable(
            ResourcesCompat.getDrawable(resources, letter_resource_map[chosenLetter] ?: 0, null)
        )

        shuffledButtons[0].text = "$chosenLetter"
        shuffledButtons[1].text = "${letters.pop()}"
        shuffledButtons[2].text = "${letters.pop()}"

        for (btn in shuffledButtons) btn.isClickable = true

        shuffledButtons[0].setOnClickListener { onAnswer(shuffledButtons, it as TextView, correct = true) }
        shuffledButtons[1].setOnClickListener { onAnswer(shuffledButtons, it as TextView, correct = false) }
        shuffledButtons[2].setOnClickListener { onAnswer(shuffledButtons, it as TextView, correct = false) }

        // Reset letter colours from previous round
        for (btn in shuffledButtons) btn.setTextColor(getColor(android.R.color.black))
    }

    private fun onAnswer(allButtons: List<TextView>, tapped: TextView, correct: Boolean) {
        // Disable all buttons immediately so the child can't double-tap
        for (btn in allButtons) btn.isClickable = false

        soundPool.play(if (correct) soundYes else soundNo, oneF, oneF, 0, 0, oneF)

        // Flash the tapped letter green (correct) or red (wrong)
        val flashColor = if (correct) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        flashTextColor(tapped, flashColor)

        handler.postDelayed({ loadRound() }, 800)
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
    }
}
