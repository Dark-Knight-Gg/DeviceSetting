package vn.com.vti.devicesetting.model.pojo.authentication

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") @Expose val username: String,
    @SerializedName("password") @Expose val password: String,
)