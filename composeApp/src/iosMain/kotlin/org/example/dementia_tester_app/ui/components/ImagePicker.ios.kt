package org.example.dementia_tester_app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.*
import platform.Foundation.*
import platform.PhotosUI.*
import kotlinx.cinterop.*
import androidx.compose.ui.interop.LocalUIViewController
import platform.darwin.NSObject

@Composable
actual fun rememberImagePickerLauncher(onImagePicked: (ByteArray) -> Unit): () -> Unit {
    val viewController = LocalUIViewController.current
    val delegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, null)
                val result = didFinishPicking.firstOrNull() as? PHPickerResult ?: return
                val itemProvider = result.itemProvider
                
                if (itemProvider.canLoadObjectOfClass(platform.UIKit.UIImage)) {
                    itemProvider.loadObjectOfClass(platform.UIKit.UIImage) { image, error ->
                        if (image is UIImage) {
                            // Resize logic
                            val maxSize = 800.0
                            val width = image.size.useContents { width }
                            val height = image.size.useContents { height }
                            
                            val scale = if (width > height) {
                                if (width > maxSize) maxSize / width else 1.0
                            } else {
                                if (height > maxSize) maxSize / height else 1.0
                            }

                            val finalImage = if (scale < 1.0) {
                                val newSize = platform.CoreGraphics.CGSizeMake(width * scale, height * scale)
                                UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
                                image.drawInRect(platform.CoreGraphics.CGRectMake(0.0, 0.0, width * scale, height * scale))
                                val resized = UIGraphicsGetImageFromCurrentImageContext()
                                UIGraphicsEndImageContext()
                                resized ?: image
                            } else {
                                image
                            }

                            val data = UIImageJPEGRepresentation(finalImage, 0.8)
                            if (data != null) {
                                val bytes = ByteArray(data.length.toInt())
                                memcpy(bytes.refTo(0), data.bytes, data.length)
                                onImagePicked(bytes)
                            }
                        }
                    }
                }
            }
        }
    }

    return {
        val configuration = PHPickerConfiguration()
        configuration.filter = PHPickerFilter.imagesFilter()
        configuration.selectionLimit = 1
        val picker = PHPickerViewController(configuration)
        picker.delegate = delegate
        viewController.presentViewController(picker, animated = true, completion = null)
    }
}
