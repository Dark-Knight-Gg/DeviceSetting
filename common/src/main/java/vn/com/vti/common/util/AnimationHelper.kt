package vn.com.vti.common.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.animation.doOnEnd

fun View.fadeInOut(
    fadeOutDuration: Long = AppResources.getResources()
        .getInteger(android.R.integer.config_longAnimTime).toLong(),
    fadeInDuration: Long = fadeOutDuration,
    fadeOutEndAction: (() -> Unit)? = null,
    fadeInEndAction: (() -> Unit)? = null,
    repeatCount: Int = 0,
    repeatMode: Int = ObjectAnimator.RESTART
): AnimatorSet {
    val fadeOut = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0f).apply {
        setAutoCancel(true)
        duration = fadeOutDuration
        this.repeatCount = repeatCount
        this.repeatMode = repeatMode
        fadeOutEndAction?.let {
            doOnEnd {
                fadeOutEndAction()
            }
        }
    }
    val fadeIn = ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f)
        .apply {
            setAutoCancel(true)
            duration = fadeInDuration
            this.repeatCount = repeatCount
            this.repeatMode = repeatMode
            fadeInEndAction?.let {
                doOnEnd {
                    fadeInEndAction()
                }
            }
        }
    return AnimatorSet().apply {
        playSequentially(fadeOut, fadeIn)
    }
}

fun View.flipChange(
    duration: Long = AppResources.getResources()
        .getInteger(android.R.integer.config_shortAnimTime).toLong(),
    onMiddleAction: () -> Unit = {}
): AnimatorSet {
    val flipBack = ObjectAnimator.ofFloat(this, View.SCALE_X, 1f, 0f)
        .apply {
            setAutoCancel(true)
            this.duration = duration
            onMiddleAction.let {
                doOnEnd {
                    onMiddleAction()
                }
            }
        }
    val flipFront = ObjectAnimator.ofFloat(this, View.SCALE_X, 0f, 1f)
        .apply {
            setAutoCancel(true)
        }
    return AnimatorSet().apply {
        playSequentially(flipBack, flipFront)
    }
}

fun TextView.fadeChangeText(
    @StringRes stringResId: Int,
    duration: Long = AppResources.getResources()
        .getInteger(android.R.integer.config_shortAnimTime).toLong()
): AnimatorSet =
    fadeInOut(duration, duration, fadeOutEndAction = { this.setText(stringResId) })

fun TextView.fadeChangeText(
    text: String,
    duration: Long = AppResources.getResources()
        .getInteger(android.R.integer.config_shortAnimTime).toLong()
): AnimatorSet =
    fadeInOut(duration, duration, fadeOutEndAction = { this.text = text })

fun ImageView.flipChangeImage(
    drawable: Drawable,
    duration: Long = AppResources.getResources()
        .getInteger(android.R.integer.config_shortAnimTime).toLong(),
): AnimatorSet = flipChange(duration) { this.setImageDrawable(drawable) }

fun ImageView.flipChangeImage(
    @DrawableRes drawableResId: Int,
    duration: Long = AppResources.getResources()
        .getInteger(android.R.integer.config_shortAnimTime).toLong(),
): AnimatorSet = flipChange(duration) { this.setImageResource(drawableResId) }

fun ImageView.flipChangeImage(
    bitmap: Bitmap,
    duration: Long = AppResources.getResources()
        .getInteger(android.R.integer.config_shortAnimTime).toLong(),
): AnimatorSet = flipChange(duration) { this.setImageBitmap(bitmap) }

fun ImageView.fadeChangeImage(
    @DrawableRes drawableResId: Int,
    duration: Long = resources
        .getInteger(android.R.integer.config_shortAnimTime).toLong(),
): AnimatorSet =
    fadeInOut(duration, fadeOutEndAction = { this.setImageResource(drawableResId) })