package vn.com.vti.common.bindmethods.img

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.module.LibraryGlideModule
import com.bumptech.glide.request.RequestOptions
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import vn.com.vti.common.bindmethods.img.ImageEffect.CENTER_CROP
import vn.com.vti.common.bindmethods.img.ImageEffect.CENTER_INSIDE
import vn.com.vti.common.bindmethods.img.ImageEffect.CIRCLE
import vn.com.vti.common.bindmethods.img.ImageEffect.FIT_CENTER
import vn.com.vti.common.bindmethods.img.ImageEffect.NONE
import vn.com.vti.common.model.FilePath
import vn.com.vti.common.network.interceptor.AuthCredential
import vn.com.vti.common.network.interceptor.NoAuth
import vn.com.vti.common.util.AppResources
import java.io.File
import java.io.InputStream
import kotlin.math.min

/**
 * - This should be implemented as [LibraryGlideModule] and can be used in library module
 * or [AppGlideModule] has multi-configuration. <br/>
 * - Now it only support one configuration of AppGlideModule. So `common` module is the one and only
 * place to defining glide configuration. <br/>
 * - `app` or any dependency modules must not define any `AppGlideModule`s
 * and should use [GlideApp] instead of [Glide] for common purpose
 */
@GlideModule
class CommonGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory())
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setLogLevel(Log.ERROR)
    }
}

@Suppress("unused")
object ImageBindingAdapter {
    @BindingAdapter("android:src")
    @JvmStatic
    fun ImageView.bindingImage(@DrawableRes imageResId: Int?) =
        if (imageResId == null || imageResId == 0) {
            setImageDrawable(null)
        } else {
            setImageResource(imageResId)
        }

    @BindingAdapter(value = ["imgSource", "imgPlaceHolder", "imgOptions"], requireAll = false)
    @JvmStatic
    fun ImageView.loadImage(
        imagePath: String?,
        @DrawableRes placeholder: Int? = null,
        options: RequestOptions? = null,
    ) {
        if (TextUtils.isEmpty(imagePath)) {
            applyPlaceholder(this, placeholder, options)
        } else {
            GlideApp.with(this).load(imagePath).placeholder(placeholder ?: 0).run {
                options?.let {
                    apply(it)
                } ?: this
            }.into(this)
        }
    }

    @BindingAdapter(value = ["imgSource", "imgPlaceHolder", "imgOptions"], requireAll = false)
    @JvmStatic
    fun ImageView.loadImage(
        imageUri: Uri?,
        @DrawableRes placeholder: Int? = null,
        options: RequestOptions? = null,
    ) {
        if (imageUri == null) {
            applyPlaceholder(this, placeholder, options)
        } else {
            GlideApp.with(this).load(imageUri).placeholder(placeholder ?: 0).run {
                options?.let {
                    apply(it)
                } ?: this
            }.into(this)
        }
    }

    @BindingAdapter(value = ["imgSource", "imgPlaceHolder", "imgOptions"], requireAll = false)
    @JvmStatic
    fun ImageView.loadImage(
        imageFile: File?,
        @DrawableRes placeholderResId: Int? = null,
        options: RequestOptions? = null,
    ) {
        if (imageFile == null) {
            applyPlaceholder(this, placeholderResId, options)
        } else {
            GlideApp.with(this).load(imageFile).placeholder(placeholderResId ?: 0)
                .run { options?.let { apply(it) } ?: this }.into(this)
        }
    }

    @BindingAdapter(value = ["imgSource", "imgPlaceHolder", "imgOptions"], requireAll = false)
    @JvmStatic
    fun ImageView.loadImage(
        drawable: Drawable?,
        placeholder: Drawable? = null,
        options: RequestOptions? = null,
    ) {
        GlideApp.with(this).load(drawable ?: placeholder).placeholder(placeholder)
            .run { options?.let { apply(it) } ?: this }.into(this)
    }

    @BindingAdapter(value = ["imgSource", "imgPlaceHolder", "imgOptions"], requireAll = false)
    @JvmStatic
    fun ImageView.loadImage(
        filePath: FilePath?,
        @DrawableRes placeholder: Int? = null,
        options: RequestOptions? = null,
    ) {
        if (filePath == null) {
            applyPlaceholder(this, placeholder, options)
        } else {
            val uri = if (filePath.local) {
                Uri.fromFile(File(filePath.path))
            } else {
                Uri.parse(filePath.path)
            }
            GlideApp.with(this).load(uri).placeholder(placeholder ?: 0).run {
                options?.let {
                    apply(it)
                } ?: this
            }.into(this)
        }
    }

    @BindingAdapter(value = ["imgSource", "imgPlaceHolder", "imgOptions"], requireAll = false)
    @JvmStatic
    fun ImageView.loadImage(
        imagePath: String?,
        placeholderResId: Drawable? = null,
        options: RequestOptions? = null,
    ) {
        if (TextUtils.isEmpty(imagePath)) {
            applyPlaceholder(this, placeholderResId, options)
        } else {
            GlideApp.with(this).load(imagePath).run { options?.let { apply(it) } ?: this }
                .placeholder(placeholderResId).into(this)
        }
    }

    @BindingAdapter(
        value = ["imgSource", "imgPlaceHolder", "imgOptions", "imgAuthentication"],
        requireAll = false
    )
    @JvmStatic
    fun ImageView.loadImageProxy(
        imagePath: String?,
        imgAuthentication: AuthCredential?,
        placeholder: Drawable? = null,
        options: RequestOptions? = null,
    ) {
        if (TextUtils.isEmpty(imagePath)) {
            applyPlaceholder(this, placeholder, options)
        } else {
            val target = if (imgAuthentication != null && imgAuthentication !is NoAuth) {
                GlideUrl(
                    imagePath, LazyHeaders.Builder().addHeader(
                        imgAuthentication.getAuthKey(), imgAuthentication.buildAuthValue() ?: ""
                    ).build()
                )
            } else {
                GlideUrl(imagePath)
            }
            GlideApp.with(this).load(target).run { options?.let { apply(it) } ?: this }
                .placeholder(placeholder).into(this)
        }
    }

    @JvmStatic
    fun createOptions(effect: Int): RequestOptions {
        return when (effect) {
            FIT_CENTER -> RequestOptions.fitCenterTransform()
            CIRCLE -> RequestOptions.circleCropTransform()
            CENTER_CROP -> RequestOptions.centerCropTransform()
            CENTER_INSIDE -> RequestOptions.centerInsideTransform()
            NONE -> RequestOptions()
            else -> RequestOptions()
        }
    }

    @JvmStatic
    fun createCenterCropRoundCorner(@DimenRes dimenResId: Int) = RequestOptions().transform(
        CenterCrop(), RoundedCorners(AppResources.getDimensionPixelSize(dimenResId))
    )

    @JvmStatic
    fun createFitCenterRoundCorner(@DimenRes dimenResId: Int) = RequestOptions().transform(
        FitCenter(), RoundedCorners(AppResources.getDimensionPixelSize(dimenResId))
    )

    @JvmStatic
    private fun applyPlaceholder(
        view: ImageView,
        @DrawableRes placeholder: Int?,
        options: RequestOptions?,
    ) {
        view.apply {
            if (placeholder == 0) {
                setImageDrawable(null)
            } else {
                GlideApp.with(this).load(placeholder).run { options?.let { apply(it) } ?: this }
                    .into(this)
            }
        }
    }

    @JvmStatic
    private fun applyPlaceholder(
        view: ImageView,
        placeholder: Drawable?,
        options: RequestOptions?,
    ) {
        view.apply {
            if (placeholder == null) {
                setImageDrawable(null)
            } else {
                GlideApp.with(this).load(placeholder).run { options?.let { apply(it) } ?: this }
                    .into(this)
            }
        }
    }

    @BindingAdapter("renderQRCode")
    @JvmStatic
    fun ImageView.renderQRCode(text: String?) {
        if (text == null || text.isEmpty()) {
            setImageDrawable(null)
        } else {
            min(width, height).let {
                if (it == 0) 256 else it
            }.runCatching {
                generateQrCode(text, this, this)
            }.let {
                setImageBitmap(it.getOrNull())
            }
        }
    }

    @Throws(WriterException::class, NullPointerException::class)
    @JvmStatic
    fun generateQrCode(text: String, width: Int, height: Int): Bitmap? {
        val bitMatrix: BitMatrix = try {
            MultiFormatWriter().encode(
                text, BarcodeFormat.QR_CODE, width, height, null
            )
        } catch (ex: IllegalArgumentException) {
            return null
        }
        val bitMatrixWidth = bitMatrix.width
        val bitMatrixHeight = bitMatrix.height
        val colorWhite = Color.WHITE
        val colorBlack = Color.BLACK
        return IntArray(bitMatrixWidth * bitMatrixHeight).apply {
            (0 until bitMatrixHeight).forEach { y ->
                val offset = y * bitMatrixWidth
                (0 until bitMatrixWidth).forEach { x ->
                    this[offset + x] = if (bitMatrix[x, y]) colorBlack else colorWhite
                }
            }
        }.let {
            Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_8888).apply {
                setPixels(it, 0, width, 0, 0, bitMatrixWidth, bitMatrixHeight)
            }
        }
    }
}