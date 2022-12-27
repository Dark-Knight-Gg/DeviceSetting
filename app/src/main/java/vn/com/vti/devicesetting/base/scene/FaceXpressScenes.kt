package vn.com.vti.devicesetting.base.scene

import androidx.fragment.app.DialogFragment
import vn.com.vti.common.scene.BaseActivity
import vn.com.vti.common.scene.BaseVmFragment
import vn.com.vti.common.viewmodel.AbsViewModel
import vn.com.vti.devicesetting.BR
import vn.com.vti.devicesetting.base.dialog.loading.LoadingDialog

abstract class FaceXpressActivity<VIEWMODEL : AbsViewModel> : BaseActivity<VIEWMODEL>() {
    override fun provideLoadingDialog(): DialogFragment = LoadingDialog()

    override fun getViewModelVariableId() = BR.vm
}

abstract class FaceXpressFragment<VIEWMODEL : AbsViewModel> : BaseVmFragment<VIEWMODEL>() {

    override fun getViewModelVariableId() = BR.vm
}