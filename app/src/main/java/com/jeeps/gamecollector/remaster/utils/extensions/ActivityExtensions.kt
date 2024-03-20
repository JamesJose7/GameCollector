package com.jeeps.gamecollector.remaster.utils.extensions

import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import kotlin.math.roundToInt

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater.invoke(layoutInflater)
    }

fun AppCompatActivity.createSnackBar(
    rootView: View,
    message: String,
    length: Int = Snackbar.LENGTH_SHORT
): Snackbar {
    return Snackbar.make(rootView, message, length)
}

fun AppCompatActivity.showSnackBar(
    rootView: View,
    message: String,
    length: Int = Snackbar.LENGTH_SHORT
) {
    Snackbar.make(rootView, message, length).show()
}

fun AppCompatActivity.showToast(
    message: String,
    length: Int = Toast.LENGTH_SHORT
) {
    Toast.makeText(this, message, length).show()
}

fun AppCompatActivity.dpToPx(dp: Float): Int {
    return TypedValue
        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
        .roundToInt()
}