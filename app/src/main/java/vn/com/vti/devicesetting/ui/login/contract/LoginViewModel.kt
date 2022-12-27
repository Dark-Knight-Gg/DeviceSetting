package vn.com.vti.devicesetting.ui.login.contract

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import vn.com.vti.devicesetting.base.viewmodel.FaceXpressViewModel
import vn.com.vti.devicesetting.interactor.login.LoginUseCase
import vn.com.vti.devicesetting.model.pojo.authentication.LoginRequest
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application
) : FaceXpressViewModel(application), LoginContract.ViewModel {

    @Inject
    lateinit var loginUseCase: LoginUseCase

    private val showPassword = MutableLiveData(false)
    private val emailAddress = MutableLiveData<String>()
    private val userPassword = MutableLiveData<String>()

    override fun getEmail(): MutableLiveData<String> = emailAddress

    override fun getPassword(): MutableLiveData<String> = userPassword

    override fun isShowPassword(): MutableLiveData<Boolean> = showPassword


    override fun onSubmitLogin() {
        val accountName = emailAddress.value ?: ""
        val password = userPassword.value ?: ""
        val param = LoginRequest(accountName, password)
        fetch(loginUseCase, param, foldSuccess = {

        })
    }
}