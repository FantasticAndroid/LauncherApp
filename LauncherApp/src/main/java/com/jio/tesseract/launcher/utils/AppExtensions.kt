package com.jio.tesseract.launcher.utils

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("imageDrawable")
fun ImageView.setImageDrawable(drawable: Drawable?) {
    setImageDrawable(drawable)
}

/**
 * To forcefully apply all cases in When Block.
 */
inline val <T> T.exhaustive get() = this
