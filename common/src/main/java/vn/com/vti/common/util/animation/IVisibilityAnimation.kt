package vn.com.vti.common.util.animation

import android.view.View

interface IVisibilityAnimation {
    /**
     * Call when target view requests to show with animation (change it state to [View.VISIBLE]
     *
     * @param target the animated view
     */
    fun onAnimateShow(target: View)

    /**
     * Call when target view request to hide with animation
     *
     * @param target           the animated view
     * @param targetVisibility should be [View.GONE] or [View.INVISIBLE]
     */
    fun onAnimateHide(target: View, targetVisibility: Int)
}