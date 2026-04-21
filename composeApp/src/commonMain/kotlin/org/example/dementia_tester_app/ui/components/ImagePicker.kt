package org.example.dementia_tester_app.ui.components

import androidx.compose.runtime.Composable

/**
 * Platform-specific image picker launcher
 * @param onImagePicked Callback when an image is selected, providing the image data as ByteArray
 * @return A function that triggers the image picker
 */
@Composable
expect fun rememberImagePickerLauncher(onImagePicked: (ByteArray) -> Unit): () -> Unit
