package com.test.flagschallenge.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.test.flagschallenge.R
import java.io.IOException
import java.io.InputStream
import java.util.Locale

fun View.makeGone() {
    this.visibility = View.GONE
}

fun View.makeVisible() {
    this.visibility = View.VISIBLE
}

fun View.makeInvisible() {
    this.visibility = View.VISIBLE

}

fun Fragment.shortToast(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun View.showErrorSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration)
        .setBackgroundTint(ContextCompat.getColor(context, R.color.red)).show()
}

fun formatTime(millis: Long): String {
    val minutes = (millis / 1000) / 60
    val seconds = (millis / 1000) % 60
    return String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds)
}

fun getJsonFromAssets(context: Context, fileName: String): String? {
    val jsonString: String
    try {
        val input: InputStream = context.assets.open(fileName)

        val size = input.available()
        val buffer = ByteArray(size)
        input.read(buffer)
        input.close()
        jsonString = String(buffer, charset("UTF-8"))
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
    return jsonString
}