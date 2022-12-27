package vn.com.vti.devicesetting.ui.login

import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import dagger.hilt.android.AndroidEntryPoint
import vn.com.vti.devicesetting.R
import vn.com.vti.devicesetting.base.scene.FaceXpressActivity
import vn.com.vti.devicesetting.databinding.ActivityLoginBinding
import vn.com.vti.devicesetting.ui.login.contract.LoginContract
import vn.com.vti.devicesetting.ui.login.contract.LoginViewModel

@AndroidEntryPoint
class LoginActivity : FaceXpressActivity<LoginContract.ViewModel>() {

    override val viewModel: LoginContract.ViewModel by viewModels<LoginViewModel>()

    override fun onCreateViewDataBinding(): ViewDataBinding {
        return DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
    }
}