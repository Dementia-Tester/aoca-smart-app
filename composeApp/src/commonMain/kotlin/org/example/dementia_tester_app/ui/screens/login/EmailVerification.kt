package org.example.dementia_tester_app.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.dementia_tester_app.auth.AuthResult
import org.example.dementia_tester_app.auth.AuthService
import org.example.dementia_tester_app.ui.components.ErrorMessage
import org.example.dementia_tester_app.ui.components.FormColors
import org.example.dementia_tester_app.ui.components.LoadingSpinner
import org.example.dementia_tester_app.ui.components.SuccessMessage

/**
 * Email verification screen that shows after signup
 * @param email User's email address
 * @param onVerified Callback to be invoked when the email is verified
 * @param onBack Callback to be invoked when the user wants to go back to login
 */
@Composable
fun EmailVerification(
    email: String,
    onVerified: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val authService = remember { AuthService() }
    var isLoading by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    // Function to check if email is verified
    fun checkEmailVerification() {
        isLoading = true
        // First reload the user to get the latest status
        authService.reloadUser { result ->
            when (result) {
                is AuthResult.Success -> {
                    if (authService.isEmailVerified()) {
                        isLoading = false
                        onVerified()
                    } else {
                        isLoading = false
                        errorMessage = "Your email is not verified yet. Please check your inbox and click the verification link."
                        showErrorMessage = true
                    }
                }
                is AuthResult.Error -> {
                    isLoading = false
                    errorMessage = result.message
                    showErrorMessage = true
                }
            }
        }
    }

    // Function to resend verification email
    fun resendVerificationEmail() {
        isLoading = true
        authService.sendEmailVerification { result ->
            isLoading = false
            when (result) {
                is AuthResult.Success -> {
                    successMessage = "Verification email sent! Please check your inbox."
                    showSuccessMessage = true
                    showErrorMessage = false
                }
                is AuthResult.Error -> {
                    errorMessage = result.message
                    showErrorMessage = true
                    showSuccessMessage = false
                }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Verify Your Email",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "We've sent a verification email to:\n$email\n\n" +
                    "Please check your inbox and click the verification link to continue." +
                    "The email may take a few minutes to show up in your inbox. If it doesn't show up, "+
                    "check your spam folder or click the resend button below.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = { checkEmailVerification() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FormColors.green
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                LoadingSpinner()
            } else {
                Text("I've Verified My Email")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { resendVerificationEmail() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = FormColors.green
            ),
            enabled = !isLoading
        ) {
            Text("Resend Verification Email")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { 
                authService.signOut()
                onBack() 
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            enabled = !isLoading
        ) {
            Text(
                text = "Back to Login",
                color = FormColors.green
            )
        }

        ErrorMessage(show = showErrorMessage, message = errorMessage)
        
        SuccessMessage(
            message = successMessage,
            isVisible = showSuccessMessage,
            modifier = Modifier.fillMaxWidth()
        )
    }
}