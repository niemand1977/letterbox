package ch.cryptobit.letterbox


import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat


const val EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE"

class MainActivity : AppCompatActivity() {
    val TAG = "LetterBox"

    /* Building this every time seems to be a bit silly. We release it later, so if you make it static, don't forget to remove that release */
    val soundPool = soundPool()
    val oneF: Float = 1.0.toFloat()

    fun makeDrawable(resourceId: Int): Drawable? {
        return ResourcesCompat.getDrawable(getResources(), resourceId, null)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ANIMALS_ON_TOP = true
        if (ANIMALS_ON_TOP) {
            val message = "foobar"
            val intent = Intent(this, AnimalsOnTop::class.java).apply {
                putExtra(EXTRA_MESSAGE, message)
            }
            startActivity(intent)
            return
        }

        val activity = R.layout.activity_main
        setContentView(activity)

        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status -> Log.v(TAG, "Loaded Sound Sample: " + sampleId + " status: " + status) }
        // FIXME: Make this more static. It seems weird to load the sounds every time.
        val soundYes = soundPool.load(this, R.raw.yes, 1)
        val soundNo = soundPool.load(this, R.raw.no, 1)


        /** We get some OOM problems from time to time. I hope that by loading the images consucutively, we can identify the offending image */
        val DIAGNOSTIC = false
        if (DIAGNOSTIC) {
            for ((k,e) in letter_resource_map) {
                Log.e(TAG, "DIAG: " + k)
                var d = makeDrawable(e)
            }
        }


        val chosenLetter = ('A'..'Z').random()
        Log.e(TAG, "Chosen letter " + chosenLetter)

        val textWidget = findViewById<TextView>(R.id.mainLetter)
        textWidget.setText("" + chosenLetter);

        val imageButton1 = findViewById<ImageButton>(R.id.button1)
        val imageButton2 = findViewById<ImageButton>(R.id.button2)
        val imageButton3 = findViewById<ImageButton>(R.id.button3)
        val listOfButtons = listOf(imageButton1, imageButton2, imageButton3).shuffled()

        val disableButtons = {
            for (button in listOfButtons) {
                button.setEnabled(false)
            }
        }
        val enableButtons = {
            for (button in listOfButtons) {
                button.setEnabled(false)
            }
        }


        val mut_map = letter_resource_map.toMutableMap()
        val correctResourceId: Int = mut_map.remove(chosenLetter) ?: R.drawable.apple
        val correctResource = ResourcesCompat.getDrawable(getResources(), correctResourceId, null)

        val wrongResource1Key = mut_map.keys.random()
        Log.e(TAG, "Chosen wrong Key " + wrongResource1Key)

        val wrongResourceId1 = mut_map.remove(wrongResource1Key) ?: R.drawable.apple
        val wrongResource2Key = mut_map.keys.random()
        Log.e(TAG, "Chosen other wrong Key " + wrongResource2Key)
        val wrongResourceId2 = mut_map.remove(wrongResource2Key) ?: R.drawable.apple
        val wrongResourceList = listOf(wrongResourceId1, wrongResourceId2)


        /* This is the suffled list of buttons, so it's random, hopefully */
        val correctButton = listOfButtons.get(0)
        val wrongButton1 = listOfButtons.get(1)
        val wrongButton2 = listOfButtons.get(2)
        val wrongButtonResourceList : List<Pair<ImageButton, Int>> = listOf(
            Pair(wrongButton1, wrongResourceId1),
            Pair(wrongButton2, wrongResourceId2)
        )


        correctButton.setImageDrawable(correctResource)
        // Alternatively: correctButton.setImageBitmap(decodeSampledBitmapFromResource(resources, correctResourceId, 100, 100))
        correctButton.setOnClickListener {
            disableButtons()

            textWidget.setText("Prima!!1  \n next");
            soundPool.play(soundYes, oneF, oneF, 0, 0, oneF)
            soundPool.release()
            Thread.sleep(500)
            enableButtons()
            this.recreate()
        }

        for (pair in wrongButtonResourceList) {
            val button = pair.first
            val resource = pair.second
            button.setImageDrawable(ResourcesCompat.getDrawable(getResources(), resource, null))
            button.setOnClickListener {
                disableButtons()
                textWidget.setText("OH noez :( \n  next")
                soundPool.play(soundNo, oneF, oneF, 0, 0, oneF)
                soundPool.release()
                Thread.sleep(500)
                enableButtons()
                this.recreate()
            }
        }
    }
}
