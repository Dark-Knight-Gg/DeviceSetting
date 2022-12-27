package vn.com.vti.common.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.annotation.NonNull
import androidx.exifinterface.media.ExifInterface
import timber.log.Timber
import vn.com.vti.common.appInstance
import vn.com.vti.common.util.extension.DateTimeXs.toTimeString
import java.io.*

fun createImageCaptureFilePath(
    @NonNull context: Context,
    useTempFile: Boolean
): String? {
    val manager: PackageManager = context.packageManager
    if (!manager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
        return null
    }
    return try {
        if (useTempFile) createTempFile(context) else createExternalImageFile()
    } catch (ex: IOException) {
        Timber.e(ex)
        null
    }?.absolutePath
}

const val IMAGE_SIZE_LIMIT: Long = 1024 * 1024 // equivalent to 1MB; unit: bytes
const val IMAGE_RESOLUTION_LIMIT = 1024

@Throws(IOException::class)
fun prepareImageFileForUpload(file: File, limitSizeInByte: Long): File {
    if (!file.exists()) return file
    Timber.d("resizeBitmap input -> ${file.length() / 1024}kB @${file.absolutePath}")
    val outputFile =
        AppFiles.createTempFile(appInstance(), "temp_${System.currentTimeMillis()}", ".jpg")
    val quality = if (file.length() >= limitSizeInByte) 90 else 100
    standardizeImageFileOrientation(file, outputFile, quality)
    if (outputFile.length() >= limitSizeInByte) {
        recursiveReduceImageFileSize(outputFile, limitSizeInByte)
    }
    Timber.d("resizeBitmap output -> ${outputFile.length() / 1024}kB @${outputFile.absolutePath}")
    return outputFile
}

@Throws(IOException::class)
fun prepareImageFileForUploadRecognition(file: File): File {
    if (!file.exists()) return file
    Timber.d("prepareImageFileForUploadRecognition input -> ${file.length() / 1024}kB @${file.absolutePath}")

    //STANDARLIZE
    val outputFile =
        AppFiles.createTempFile(appInstance(), "temp_${System.currentTimeMillis()}", ".jpg")
    standardizeImageFileOrientation(file, outputFile, 100)
    val bitmap = BitmapFactory.decodeFile(outputFile.absolutePath)
    val width = bitmap.width
    val height = bitmap.height
    val minOfSize = minOf(width, height)
    Timber.d("prepareImageFileForUploadRecognition standardized -> ${file.length() / 1024}kB @${file.absolutePath} w=${width} h=${height}")

    //CROP CENTER
    val cropCentered: Bitmap
    if (width != height) {
        cropCentered = if (width > height) {
            Bitmap.createBitmap(bitmap, (width - minOfSize) shr 1, 0, minOfSize, minOfSize)
        } else {
            Bitmap.createBitmap(bitmap, 0, (height - minOfSize) shr 1, minOfSize, minOfSize)
        }
        bitmap.recycle()
    } else {
        cropCentered = bitmap
    }
    Timber.d("prepareImageFileForUploadRecognition centerCrop -> w=${cropCentered.width} h=${cropCentered.height}")

    //REDUCE IMAGE RESOLUTION
    val resized: Bitmap
    if (cropCentered.height > IMAGE_RESOLUTION_LIMIT) {
        resized = Bitmap.createScaledBitmap(
            cropCentered,
            IMAGE_RESOLUTION_LIMIT,
            IMAGE_RESOLUTION_LIMIT,
            true
        )
        cropCentered.recycle()
    } else {
        resized = cropCentered
    }
    val outputStream = FileOutputStream(outputFile)
    resized.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.close()
    Timber.d("prepareImageFileForUploadRecognition scaleDown -> w=${resized.width} h=${resized.height}")
    resized.recycle()

    //REDUCE IMAGE SIZE
    val limitSizeInByte = IMAGE_SIZE_LIMIT
    if (outputFile.length() >= limitSizeInByte) {
        recursiveReduceImageFileSize(outputFile, limitSizeInByte)
    }
    Timber.d("prepareImageFileForUploadRecognition reduceFileSize -> ${outputFile.length() / 1024}kB @${outputFile.absolutePath}")
    return outputFile
}

@Throws(IOException::class)
private fun recursiveReduceImageFileSize(file: File, limitSizeInByte: Long) {
    val options = BitmapFactory.Options().apply {
        //reduce bitmap pixels size by 4
        inSampleSize = 2
    }
    val inputStream = FileInputStream(file)
    val bitmap = BitmapFactory.decodeStream(inputStream, null, options) ?: return
    inputStream.close()
    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    bitmap.recycle()
    outputStream.close()
    Timber.d("recursiveCompressImage ${file.length() / 1024}kB")
    if (file.length() > limitSizeInByte) {
        recursiveReduceImageFileSize(file, limitSizeInByte)
    }

}

fun standardizeImageFileOrientation(input: File, output: File, quality: Int = 100) {
    if (!input.exists()) return
    val matrix = Matrix()
    when (ExifInterface(input.absolutePath).getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
    }
    val original = BitmapFactory.decodeFile(input.absolutePath)
    if (original != null) {
        val rotated =
            Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, false)
        val fos = FileOutputStream(output)
        rotated.compress(Bitmap.CompressFormat.JPEG, quality, fos)
        fos.close()
        original.recycle()
        rotated.recycle()
    }
}

@SuppressLint("SimpleDateFormat")
@Throws(IOException::class)
fun createTempFile(@NonNull context: Context): File {
    val storageDir: File? = context.getExternalFilesDir("")
    return File.createTempFile(
        generateFileName(),  /* prefix */
        ".jpg",  /* suffix */
        storageDir /* directory */
    )
}

fun createExternalImageFile(): File? {
    return appInstance().getExternalFilesDir(null)?.let {
        val imagesFolder = File(it, "images")
        imagesFolder.mkdirs()
        return File(imagesFolder, generateFileName("jpg"))
    }
}

private fun generateFileName(ext: String? = null) =
    System.currentTimeMillis().toTimeString("yyyyMMdd_HHmmss").let { name ->
        "IMG_$name${ext?.let { ".$it" } ?: ""}"
    }

fun Uri.extractFile(): File {
    val context = appInstance()
    val fileExtension = getFileExtension(context)
    val tempFile =
        AppFiles.createTempFile(context, "temp-${System.currentTimeMillis()}", ".$fileExtension")
    try {
        val outputStream = FileOutputStream(tempFile)
        val inputStream = context.contentResolver.openInputStream(this)
        inputStream?.let {
            copy(inputStream, outputStream)
        }
        outputStream.flush()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return tempFile
}

fun Uri.getFileExtension(context: Context): String? {
    val fileType: String? = context.contentResolver.getType(this)
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
}

@Throws(IOException::class)
private fun copy(source: InputStream, target: OutputStream) {
    val buf = ByteArray(8192)
    var length: Int
    while (source.read(buf).also { length = it } > 0) {
        target.write(buf, 0, length)
    }
}