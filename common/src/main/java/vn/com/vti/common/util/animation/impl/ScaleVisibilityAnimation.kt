package vn.com.vti.common.util.animation.impl

import android.view.View
import androidx.core.view.ViewCompat
import vn.com.vti.common.util.animation.IVisibilityAnimation
import kotlin.math.max
import kotlin.math.min

class ScaleVisibilityAnimation(
    alpha: Float,
    minScale: Float,
    duration: Long
) : IVisibilityAnimation {
    private val alpha: Float
    private val minScale: Float
    private val duration: Long
    override fun onAnimateShow(target: View) {
        target.apply {
            this.alpha = alpha
            visibility = View.VISIBLE
            this.scaleX = scaleX
            this.scaleY = scaleY
            ViewCompat.animate(this)
                .alpha(1.0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(duration)
                .start()
        }
    }

    override fun onAnimateHide(target: View, targetVisibility: Int) {
        target.apply {
            alpha = 1.0f
            visibility = View.VISIBLE
            ViewCompat.animate(this)
                .alpha(alpha)
                .scaleX(minScale)
                .scaleY(minScale)
                .setDuration(duration)
                .withEndAction { visibility = targetVisibility }
                .start()
        }
    }

    init {
        this.alpha = max(0f, min(alpha, 1f))
        this.minScale = max(0f, min(minScale, 1f))
        this.duration = max(0L, duration)
    }
}