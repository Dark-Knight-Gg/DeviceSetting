package vn.com.vti.devicesetting.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BaseResponse<T>(
    @SerializedName("ret") @Expose val ret: String = "OK",
    @SerializedName("msg") @Expose val message: String = "This is the message",
    @SerializedName("data") @Expose val result: T
) {
    fun isSuccess(): Boolean = "OK" == ret
}