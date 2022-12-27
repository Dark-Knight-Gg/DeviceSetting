package vn.com.vti.common.util.animation.trigger

import android.view.View
import android.view.animation.CycleInterpolator
import androidx.core.view.ViewCompat
import vn.com.vti.common.util.animation.IAnimation
import kotlin.math.max

class ShakeAnimation(distance: Float, circle: Int, duration: Long) :
    IAnimation {
    private val distance: Float = max(distance, 0f)
    private val cycle: Int = max(0, circle)
    private val duration: Long = max(0, duration)
    override fun animate(view: View) {
        ViewCompat.animate(view)
            .translationX(distance)
            .setInterpolator(CycleInterpolator(cycle.toFloat()))
            .setDuration(duration)
            .start()
    }

    companion object {

        @JvmStatic
        fun newInstance(distance: Float, cycle: Int, duration: Long) =
            ShakeAnimation(distance, cycle, duration)
    }

}