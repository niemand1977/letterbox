package ch.cryptobit.letterbox


import android.app.ProgressDialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat


class AnimalsOnTop : AppCompatActivity() {
    val TAG = "LetterBox"

    /* Is this actually created every time? */
    val soundPool = soundPool()

    val oneF: Float = 1.0.toFloat()


    fun makeDrawable(resourceId: Int): Drawable? {
        return ResourcesCompat.getDrawable(getResources(), resourceId, null)
    }

    fun makeDrawable(char: Char): Drawable? {
        return makeDrawable(letter_resource_map.get(char) ?: 0)
    }

    fun pop (set: MutableCollection<Char>) : Char {
        val e = set.random()
        set.remove(e)
        return e
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = R.layout.animals_on_top
        setContentView(activity)

        val letterWidget1 = findViewById<TextView>(R.id.mainLetter1)
        val letterWidget2 = findViewById<TextView>(R.id.mainLetter2)
        val letterWidget3 = findViewById<TextView>(R.id.mainLetter3)
        val listOfLetterWidgets = listOf(letterWidget1, letterWidget2, letterWidget3).shuffled()

        val disableButtons = {
            for (button in listOfLetterWidgets) {
                button.setOnClickListener({})
            }
        }
        val enableButtons = {
            for (button in listOfLetterWidgets) {
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

        fun noButtonContext (body: ()->Unit) {
            disableButtons()
            val r = body()
            enableButtons()
            return r
        }

        hideButtons()


        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            Log.v(TAG, "Loaded Sound Sample: " + sampleId + " status: " + status)
            showButtons()
        }
        // FIXME: Make this more static. It seems weird to load the sounds every time.
        val soundYes = soundPool.load(this, R.raw.yes, 1)
        val soundNo = soundPool.load(this, R.raw.no, 1)


        /** We get some OOM problems from time to time. I hope that by loading the images consecutively, we can identify the offending image */
        val DIAGNOSTIC = false
        if (DIAGNOSTIC) {
            for ((k,e) in letter_resource_map) {
                Log.e(TAG, "DIAG: " + k)
                var d = makeDrawable(e)
            }
        }

        val mut_set = ('A'..'Z').toMutableSet()
        val chosenLetter = pop(mut_set)
        Log.e(TAG, "Chosen letter " + chosenLetter)


        val imageWidget = findViewById<ImageView>(R.id.imageView);
        imageWidget.setImageDrawable(makeDrawable(chosenLetter))


        val mut_map = letter_resource_map.toMutableMap()
        val correctResourceId: Int = mut_map.remove(chosenLetter) ?: R.drawable.apple
        val correctResource = ResourcesCompat.getDrawable(getResources(), correctResourceId, null);

        val wrongResource1Key = pop(mut_set)
        Log.e(TAG, "Chosen wrong Key " + wrongResource1Key)

        val wrongResource2Key = pop(mut_set)
        Log.e(TAG, "Chosen other wrong Key " + wrongResource2Key)

        /* This is the suffled list of buttons, so it's random, hopefully */
        val correctButton = listOfLetterWidgets.get(0)
        val wrongButton1 = listOfLetterWidgets.get(1)
        val wrongButton2 = listOfLetterWidgets.get(2)
        val wrongButtonResourceList : List<Pair<TextView, Char>> = listOf(
            Pair(wrongButton1, wrongResource1Key),
            Pair(wrongButton2, wrongResource2Key)
        )


        correctButton.setText(""+chosenLetter);
        correctButton.setOnClickListener {
            disableButtons()
//            textWidget.setText("Prima!!1  \n next");
            soundPool.play(soundYes, oneF, oneF, 0, 0, oneF);
            Thread.sleep(500)
            soundPool.release()
            enableButtons()
            this.recreate()
        }

        for (pair in wrongButtonResourceList) {
            val button = pair.first
            val resource = pair.second
            button.setText("" + resource)
            button.setOnClickListener {
                disableButtons()
//                textWidget.setText("OH noez :( \n  next");
                soundPool.play(soundNo, oneF, oneF, 0, 0, oneF);
                Thread.sleep(500)
                soundPool.release()
                enableButtons()
                this.recreate()
            }
        }
    }
}
