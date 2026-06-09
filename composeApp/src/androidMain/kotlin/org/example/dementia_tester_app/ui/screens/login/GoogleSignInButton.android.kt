package org.example.dementia_tester_app.ui.screens.login

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun GoogleSignInButton(
    onSignInSuccess: () -> Unit,
    onSignInError: (String) -> Unit
) {
    OutlinedButton(
        onClick = {
            onSignInError("Google Sign-In is not fully configured on Android yet.")
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Continue with Google")
    }
}
