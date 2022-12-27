package vn.com.vti.devicesetting.ui.login.contract

import androidx.lifecycle.MutableLiveData
import vn.com.vti.common.viewmodel.AbsViewModel

interface LoginContract {
    interface ViewModel : AbsViewModel {

        fun getEmail(): MutableLiveData<String>

        fun getPassword(): MutableLiveData<String>

        fun isShowPassword(): MutableLiveData<Boolean>

        fun onSubmitLogin()
    }
}