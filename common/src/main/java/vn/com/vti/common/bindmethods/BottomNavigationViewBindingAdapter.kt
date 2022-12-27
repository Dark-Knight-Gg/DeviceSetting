package vn.com.vti.common.bindmethods

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

object BottomNavigationViewBindingAdapter {

    @BindingAdapter("bottomNavigationBadges")
    @JvmStatic
    fun bottomNavigationBadges(
        view: BottomNavigationView,
        badges: Map<Int, Int>?,
    ) {
        badges?.forEach {
            view.getOrCreateBadge(it.key).apply {
                isVisible = it.value > 0
                number = it.value
            }
        }
    }

    @BindingAdapter("bottomNavigationCurrentMenuReselected")
    @JvmStatic
    fun bottomNavigationCurrentMenuReselected(
        view: BottomNavigationView,
        listener: NavigationBarView.OnItemSelectedListener?,
    ) {
        view.setOnItemSelectedListener(listener)
    }

    @BindingAdapter("bottomNavigationCurrentMenu")
    @JvmStatic
    fun setBottomNavigationSelectedMenu(
        view: BottomNavigationView,
        selected: Int?,
    ) {
        selected?.let {
            if (view.selectedItemId != it) view.selectedItemId = it
        }
    }

    @BindingAdapter(
        value = ["bottomNavigationCurrentMenu", "bottomNavigationCurrentMenuAttrChanged"],
        requireAll = false
    )
    @JvmStatic
    fun setBottomNavigationSelectedMenuAttrChanged(
        view: BottomNavigationView,
        selectedListener: NavigationBarView.OnItemSelectedListener?,
        attrChanged: InverseBindingListener?,
    ) {
        view.setOnItemSelectedListener {
            attrChanged?.onChange()
            selectedListener?.onNavigationItemSelected(it) ?: true
        }
    }

    @InverseBindingAdapter(
        attribute = "bottomNavigationCurrentMenu",
        event = "bottomNavigationCurrentMenuAttrChanged"
    )
    @JvmStatic
    fun getBottomNavigationSelectedMenu(view: BottomNavigationView) = view.selectedItemId
}