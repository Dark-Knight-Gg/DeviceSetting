package vn.com.vti.common.bindmethods

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

object ViewPager2BindingAdapter {
    @BindingAdapter("vpAdapter")
    @JvmStatic
    fun setViewPagerAdapter(viewPager: ViewPager2, adapter: RecyclerView.Adapter<*>) {
        viewPager.adapter = adapter
    }

    @BindingAdapter("vpOnPageChangeListener")
    @JvmStatic
    fun addOnPageChangeListener(
        viewPager: ViewPager2,
        listener: ViewPager2.OnPageChangeCallback?
    ) {
        listener?.let {
            viewPager.run {
                unregisterOnPageChangeCallback(it)
                registerOnPageChangeCallback(it)
            }
        }
    }

    @BindingAdapter("vpOffScreenPageLimit")
    @JvmStatic
    fun setOffScreenPageLimit(viewPager: ViewPager2, limit: Int) {
        viewPager.offscreenPageLimit = limit
    }

    @BindingAdapter("vpCurrentTabAttrChanged")
    @JvmStatic
    fun setListeners(view: ViewPager2, attrChange: InverseBindingListener) {
        view.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
            }

            override fun onPageSelected(position: Int) {
                attrChange.onChange()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    @BindingAdapter("vpCurrentTab")
    @JvmStatic
    fun setCurrentTab(
        pager: ViewPager2,
        liveCurrentTab: MutableLiveData<Int?>,
    ) {
        liveCurrentTab.value?.let {
            if (it != pager.currentItem) {
                pager.setCurrentItem(it, true)
            }
        }
    }

    @InverseBindingAdapter(attribute = "vpCurrentTab", event = "vpCurrentTabAttrChanged")
    @JvmStatic
    fun getCurrentTab(viewPager: ViewPager2) = viewPager.currentItem
}