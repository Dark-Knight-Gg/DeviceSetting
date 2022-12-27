package vn.com.vti.common.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.NonNull
import timber.log.Timber
import vn.com.vti.common.appInstance
import java.io.*
import java.util.*


@Suppress("unused")
object AppFiles {
    @Throws(IOException::class)
    fun readAssetFile(path: String): String {
        return try {
            assetBufferedReader(path).readAll()
        } catch (e: IOException) {
            Timber.e(e)
            throw e
        }
    }

    @Throws(IOException::class)
    fun readInternalFile(path: String): String? {
        return try {
            internalBufferedReader(path)?.readAll()
        } catch (e: IOException) {
            Timber.e(e)
            throw e
        }
    }

    @Throws(IOException::class)
    fun readExternalFile(path: String): String? {
        return try {
            externalBufferedReader(path)?.readAll()
        } catch (e: IOException) {
            Timber.e(e)
            throw e
        }
    }

    @Throws(IOException::class)
    fun readFile(fullFilePath: String): String? {
        return try {
            fileBufferedReader(fullFilePath)?.readAll()
        } catch (e: IOException) {
            Timber.e(e)
            throw e
        }
    }

    fun isExternalStorageReadOnly() =
        Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED_READ_ONLY

    fun isExternalStorageAvailable() =
        Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    @Throws(IOException::class)
    fun BufferedReader.readAll(): String {
        return try {
            StringBuilder().run {
                var line: String?
                while (readLine().also { line = it } != null) {
                    append(line)
                }
                toString()
            }
        } finally {
            try {
                close()
            } catch (e: IOException) {
                Timber.e(e)
            }
        }
    }

    @Throws(IOException::class)
    private fun assetBufferedReader(path: String) =
        BufferedReader(InputStreamReader(appInstance().assets.open(path)))

    @Throws(IOException::class)
    private fun internalBufferedReader(path: String) =
        appInstance().filesDir.toString().let { dir ->
            if (path.startsWith(File.separator)) {
                dir + path
            } else {
                dir + File.separator + path
            }.let { fileBufferedReader(it) }
        }

    @Throws(IOException::class)
    private fun externalBufferedReader(path: String): BufferedReader? =
        appInstance().getExternalFilesDir(null)?.toString().let {
            if (path.startsWith(File.separator)) {
                it + path
            } else {
                it + File.separator + path
            }
        }.let { fileBufferedReader(it) }

    @Throws(IOException::class)
    private fun fileBufferedReader(fullFilePath: String): BufferedReader? {
        return File(fullFilePath).run {
            if (exists()) {
                BufferedReader(InputStreamReader(FileInputStream(this)))
            } else null
        }
    }

    fun fromAssets(path: String): Uri = Uri.parse("file:///android_asset/$path")

    @Suppress("DEPRECATION")
    fun filePathFromImagePicker(imageUri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        return try {
            appInstance().contentResolver.query(imageUri, projection, null, null, null)
                ?.run {
                    moveToFirst()
                    val columnIndex: Int = getColumnIndex(projection[0])
                    val picturePath: String = getString(columnIndex)
                    close()
                    picturePath
                }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    fun createTempFile(@NonNull context: Context, name: String, suffix: String): File {
        val storageDir: File? = context.getExternalFilesDir("")
        return File.createTempFile(
            name,  /* prefix */
            suffix,  /* suffix */
            storageDir /* directory */
        )
    }

    fun clearTempFiles(@NonNull context: Context, filter: FileFilter = FileFilter { true }) {
        context.getExternalFilesDir("")?.listFiles(filter)?.forEach {
            it.deleteRecursively()
        }
    }
}

fun File.getMimeType(fallback: String = "image/*"): String {
    return MimeTypeMap.getFileExtensionFromUrl(toString())
        ?.run {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(lowercase(Locale.getDefault()))
        }
        ?: fallback
}

fun String.isAcceptableFileExtensions(arrString: Array<String>): Boolean {
    return lowercase(Locale.getDefault()).run {
        arrString.any { endsWith(it) }
    }
}