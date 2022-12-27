package vn.com.vti.common.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import java.io.File


class RequestMediaPicker : ActivityResultContract<Uri, Uri?>() {

    override fun createIntent(context: Context, input: Uri): Intent =
        Intent(Intent.ACTION_PICK, input)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return intent?.data
    }
}

class RequestExternalImagePicker : ActivityResultContract<String, Uri?>() {

    override fun createIntent(context: Context, input: String): Intent =
        (Intent.ACTION_GET_CONTENT to Intent.ACTION_PICK).transform {
            Intent(it).apply {
                type = "image/*"
            }
        }.let {
            Intent.createChooser(it.first, input).apply {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(it.second))
            }
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return intent?.data
    }

    private fun <T, R> Pair<T, T>.transform(fn: (T) -> R): Pair<R, R> = fn(first) to fn(second)

}

class RequestImageCapture : ActivityResultContract<String, String?>() {

    private var path: String? = null

    override fun createIntent(context: Context, input: String): Intent {
        path = input
        return File(input).let {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                it
            )
        }.let {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, it)
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        val temp = path
        path = null
        if (resultCode == Activity.RESULT_OK) {
            return temp
        }
        return null
    }

}