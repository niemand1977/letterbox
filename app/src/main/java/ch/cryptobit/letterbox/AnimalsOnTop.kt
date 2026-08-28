package ch.cryptobit.letterbox


import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat


class AnimalsOnTop : AppCompatActivity() {
    val TAG = "LetterBox"

    // SoundPool is created once per Activity instance and released in onDestroy.
    lateinit var soundPool: android.media.SoundPool

    val oneF: Float = 1.0f


    fun makeDrawable(resourceId: Int): Drawable? {
        return ResourcesCompat.getDrawable(resources, resourceId, null)
    }

    fun makeDrawable(char: Char): Drawable? {
        return makeDrawable(letter_resource_map.get(char) ?: 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        soundPool = soundPool()

        setContentView(R.layout.animals_on_top)

        val letterWidget1 = findViewById<TextView>(R.id.mainLetter1)
        val letterWidget2 = findViewById<TextView>(R.id.mainLetter2)
        val letterWidget3 = findViewById<TextView>(R.id.mainLetter3)
        val listOfLetterWidgets = listOf(letterWidget1, letterWidget2, letterWidget3).shuffled()

        val handler = Handler(Looper.getMainLooper())

        val disableButtons = {
            for (button in listOfLetterWidgets) {
                button.isClickable = false
            }
        }
        val hideButtons = {
            for (button in listOfLetterWidgets) {
                button.visibility = View.INVISIBLE
            }
        }
        val showButtons = {
            for (button in listOfLetterWidgets) {
                button.visibility = View.VISIBLE
            }
        }

        hideButtons()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            Log.v(TAG, "Loaded Sound Sample: $sampleId status: $status")
            showButtons()
        }
        val soundYes = soundPool.load(this, R.raw.yes, 1)
        val soundNo = soundPool.load(this, R.raw.no, 1)


        val mut_set = ('A'..'Z').toMutableSet()
        val chosenLetter = mut_set.pop()
        Log.e(TAG, "Chosen letter $chosenLetter")


        val imageWidget = findViewById<ImageView>(R.id.imageView)
        imageWidget.setImageDrawable(makeDrawable(chosenLetter))


        val mut_map = letter_resource_map.toMutableMap()
        mut_map.remove(chosenLetter)

        val wrongResource1Key = mut_set.pop()
        Log.e(TAG, "Chosen wrong Key $wrongResource1Key")

        val wrongResource2Key = mut_set.pop()
        Log.e(TAG, "Chosen other wrong Key $wrongResource2Key")

        /* This is the shuffled list of buttons, so it's random */
        val correctButton = listOfLetterWidgets[0]
        val wrongButton1 = listOfLetterWidgets[1]
        val wrongButton2 = listOfLetterWidgets[2]
        val wrongButtonResourceList: List<Pair<TextView, Char>> = listOf(
            Pair(wrongButton1, wrongResource1Key),
            Pair(wrongButton2, wrongResource2Key)
        )

        correctButton.text = "$chosenLetter"
        correctButton.setOnClickListener {
            disableButtons()
            soundPool.play(soundYes, oneF, oneF, 0, 0, oneF)
            // Delay recreation so the sound has time to play — no Thread.sleep on the UI thread.
            handler.postDelayed({ recreate() }, 700)
        }

        for ((button, resource) in wrongButtonResourceList) {
            button.text = "$resource"
            button.setOnClickListener {
                disableButtons()
                soundPool.play(soundNo, oneF, oneF, 0, 0, oneF)
                handler.postDelayed({ recreate() }, 700)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}
