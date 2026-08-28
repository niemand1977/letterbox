package ch.cryptobit.letterbox

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.SoundPool


fun soundPool(): SoundPool {
    return SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build()
        )
        .build()
}


/** The resources need to be reasonably free.
 * pixabay seems to be a good enough resource */
val letter_resource_map = mapOf(
    'A' to R.drawable.apple,
    'B' to R.drawable.bear,
    'C' to R.drawable.chameleon,
    'D' to R.drawable.ducky,
    'E' to R.drawable.elephant,
    'F' to R.drawable.fox,
    'G' to R.drawable.giraffe,
    'H' to R.drawable.bunny,
    'I' to R.drawable.igel,
    'J' to R.drawable.jaguar,
    'K' to R.drawable.kangaroo,
    'L' to R.drawable.lion,
    'M' to R.drawable.mouse,
    'N' to R.drawable.nest,
    'O' to R.drawable.octopus,
    'P' to R.drawable.penguin,
    'Q' to R.drawable.jellyfish,
    'R' to R.drawable.rainbow,
    'S' to R.drawable.snake,
    'T' to R.drawable.table,
    'U' to R.drawable.unicorn,
    'V' to R.drawable.vulture,
    'W' to R.drawable.whale,
    'X' to R.drawable.xylophone,
    'Y' to R.drawable.yak,
    'Z' to R.drawable.zebra
)

fun pop (set: MutableCollection<Char>) : Char {
    val e = set.random()
    set.remove(e)
    return e
}

fun <K, V> MutableMap<K, V>.pop(): Map.Entry<K,V> {
    val e = this.entries.random()
    this.remove(e.key)
    return e
}

fun <T> MutableCollection<T>.pop(): T {
    val c = this.random()
    this.remove(c)
    return c
}



/* From https://developer.android.com/topic/performance/graphics/load-bitmap.html#read-bitmap */
fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}


fun decodeSampledBitmapFromResource(
    res: Resources,
    resId: Int,
    reqWidth: Int,
    reqHeight: Int
): Bitmap {
    // First decode with inJustDecodeBounds=true to check dimensions
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, this)

        // Calculate inSampleSize
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        inJustDecodeBounds = false

        BitmapFactory.decodeResource(res, resId, this)
    }
}
