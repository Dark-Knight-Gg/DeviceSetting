package vn.com.vti.common.bindmethods

import android.view.View
import androidx.core.view.GravityCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData

object DrawerLayoutBindingAdapter {

    @BindingAdapter("drawerLayoutSlideAttrChanged", "drawerLayoutListener")
    @JvmStatic
    fun setDrawerSlideListener(
        view: DrawerLayout,
        attrChanged: InverseBindingListener?,
        callback: DrawerLayout.DrawerListener?
    ) {
        view.addDrawerListener(object : DrawerLayout.DrawerListener {

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                callback?.onDrawerSlide(drawerView, slideOffset)
            }

            override fun onDrawerOpened(drawerView: View) {
                attrChanged?.onChange()
                callback?.onDrawerOpened(drawerView)
            }

            override fun onDrawerClosed(drawerView: View) {
                attrChanged?.onChange()
                callback?.onDrawerClosed(drawerView)
            }

            override fun onDrawerStateChanged(newState: Int) {
                callback?.onDrawerStateChanged(newState)
            }

        })
    }

    @BindingAdapter("drawerLayoutSlide")
    @JvmStatic
    fun setDrawerLayoutSliderState(
        view: DrawerLayout,
        liveCurrentState: MutableLiveData<Boolean?>
    ) {
        liveCurrentState.value?.let {
            if (it != view.isOpen) {
                view.openDrawer(GravityCompat.START)
            }
        }
    }

    @InverseBindingAdapter(attribute = "drawerLayoutSlide", event = "drawerLayoutSlideAttrChanged")
    @JvmStatic
    fun getDrawerLayoutCurrentState(drawerLayout: DrawerLayout) = drawerLayout.isOpen
}