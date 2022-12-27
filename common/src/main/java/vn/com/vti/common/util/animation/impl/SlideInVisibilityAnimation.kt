package vn.com.vti.common.util.animation.impl

import android.view.View
import androidx.core.view.ViewCompat
import vn.com.vti.common.util.animation.IVisibilityAnimation
import kotlin.math.max
import kotlin.math.min

class SlideInVisibilityAnimation(
    alpha: Float, duration: Long,
    extractTranslationXValue: (View) -> Int,
    extractTranslationYValue: (View) -> Int
) : IVisibilityAnimation {
    private val alpha: Float
    private val duration: Long
    private val extractTranslationYValue: (View) -> Int
    private val extractTranslationXValue: (View) -> Int
    private var ignoreFirst = true
    override fun onAnimateShow(target: View) {
        target.apply {
            this.alpha = alpha
            if (width == 0 || height == 0) {
                visibility = View.INVISIBLE
                post { animateShow(this) }
            } else {
                animateShow(target)
            }
        }
    }

    private fun animateShow(target: View) {
        target.run {
            visibility = View.VISIBLE
            translationX = extractTranslationXValue(target).toFloat()
            translationY = extractTranslationYValue(target).toFloat()
            ViewCompat.animate(target)
                .alpha(1.0f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(duration)
                .start()
        }
    }

    override fun onAnimateHide(
        target: View,
        targetVisibility: Int
    ) {
        target.alpha = 1.0f
        if (ignoreFirst) {
            ignoreFirst = false
            target.visibility = targetVisibility
            return
        }
        ViewCompat.animate(target)
            .alpha(alpha)
            .translationX(extractTranslationXValue(target).toFloat())
            .translationY(extractTranslationYValue(target).toFloat())
            .setDuration(duration)
            .withEndAction { target.visibility = targetVisibility }
            .start()
    }

    fun setIgnoreFirst(ignoreFirst: Boolean) {
        this.ignoreFirst = ignoreFirst
    }

    companion object {
        fun fromTop(alpha: Float, duration: Long): SlideInVisibilityAnimation {
            return SlideInVisibilityAnimation(alpha,
                duration,
                { 0 },
                { it.measuredHeight }
            )
        }

        fun fromBottom(alpha: Float, duration: Long): SlideInVisibilityAnimation {
            return SlideInVisibilityAnimation(alpha,
                duration,
                { 0 },
                { it.measuredHeight }
            )
        }

        fun fromLeft(alpha: Float, duration: Long): SlideInVisibilityAnimation {
            return SlideInVisibilityAnimation(alpha,
                duration,
                { it.measuredWidth },
                { 0 }
            )
        }

        fun fromRight(alpha: Float, duration: Long): SlideInVisibilityAnimation {
            return SlideInVisibilityAnimation(
                alpha,
                duration,
                { it.measuredWidth },
                { 0 }
            )
        }
    }

    init {
        this.alpha = max(0f, min(alpha, 1f))
        this.duration = max(0L, duration)
        this.extractTranslationXValue = extractTranslationXValue
        this.extractTranslationYValue = extractTranslationYValue
    }
}