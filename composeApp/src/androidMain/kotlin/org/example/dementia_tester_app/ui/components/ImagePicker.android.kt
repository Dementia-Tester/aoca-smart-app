package org.example.dementia_tester_app.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream

@Composable
actual fun rememberImagePickerLauncher(onImagePicked: (ByteArray) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap != null) {
                    // Fix orientation if needed
                    val exifInputStream = context.contentResolver.openInputStream(it)
                    val rotation = exifInputStream?.let { stream ->
                        val exif = ExifInterface(stream)
                        val orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        stream.close()
                        when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> 90
                            ExifInterface.ORIENTATION_ROTATE_180 -> 180
                            ExifInterface.ORIENTATION_ROTATE_270 -> 270
                            else -> 0
                        }
                    } ?: 0

                    val rotatedBitmap = if (rotation != 0) {
                        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                        Bitmap.createBitmap(
                            originalBitmap, 0, 0,
                            originalBitmap.width, originalBitmap.height,
                            matrix, true
                        )
                    } else {
                        originalBitmap
                    }

                    // Resize to max 800px width/height
                    val maxSize = 800
                    val scale = maxSize.toFloat() / Math.max(rotatedBitmap.width, rotatedBitmap.height)
                    val finalBitmap = if (scale < 1f) {
                        Bitmap.createScaledBitmap(
                            rotatedBitmap,
                            (rotatedBitmap.width * scale).toInt(),
                            (rotatedBitmap.height * scale).toInt(),
                            true
                        )
                    } else {
                        rotatedBitmap
                    }

                    // Compress to JPEG
                    val outputStream = ByteArrayOutputStream()
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    onImagePicked(outputStream.toByteArray())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    return { launcher.launch("image/*") }
}
