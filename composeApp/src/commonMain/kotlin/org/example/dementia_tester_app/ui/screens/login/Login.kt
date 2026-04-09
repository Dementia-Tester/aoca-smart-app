package org.example.dementia_tester_app.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.dementia_tester_app.ui.components.*
import org.jetbrains.compose.resources.painterResource
import androidx.compose.runtime.Composable
import dementiatesterapp.composeapp.generated.resources.Res
import dementiatesterapp.composeapp.generated.resources.icon_transparent
import org.example.dementia_tester_app.auth.AuthResult
import org.example.dementia_tester_app.auth.AuthService
import org.example.dementia_tester_app.ui.components.LoadingSpinner
import org.example.dementia_tester_app.utils.validateFields

@Composable
fun LoginIcon() {
    Image(
        painter = painterResource(Res.drawable.icon_transparent),
        contentDescription = "Dementia Tester Logo",
        modifier = Modifier.size(200.dp)
    )
}

/**
 * Login screen with email and password fields, login button, and sign-up button
 * @param onLogin Callback to be invoked when the login button is clicked
 * @param onSignUp Callback to be invoked when the sign up button is clicked
 * @param onForgotPassword Callback to be invoked when the forgot password link is clicked
 */
@Composable
fun Login(
    onLogin: (String) -> Unit = {_ ->}, 
    onSignUp: () -> Unit = {}, 
    onForgotPassword: () -> Unit = {}
) {
    val EMAIL = "email"
    val PASSWORD = "password"

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Using a single map to track all field error states
    var fieldErrors by remember { mutableStateOf(mapOf<String, Boolean>()) }
    
    // Helper function to get error state for a field
    fun isFieldError(field: String): Boolean = fieldErrors[field] == true
    
    // Helper function to clear error for a field
    fun clearFieldError(field: String) {
        if (fieldErrors.containsKey(field)) {
            fieldErrors = fieldErrors - field
        }
    }

    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("Please enter all required fields") }
    
    val authService = remember { AuthService() }
    
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Title
        Text(
            text = "AoCA Smart App",
            fontSize = 24.sp,
        )

        // App Icon
        LoginIcon()

        // Email Field
        FormTextField(
            value = email,
            onValueChange = { 
                email = it
                clearFieldError(EMAIL)
                showErrorMessage = false
            },
            label = "Email",
            isError = isFieldError(EMAIL),
            keyboardType = KeyboardType.Email
        )

        // Password Field
        FormTextField(
            value = password,
            onValueChange = { 
                password = it
                clearFieldError(PASSWORD)
                showErrorMessage = false
            },
            label = "Password",
            isError = isFieldError(PASSWORD),
            keyboardType = KeyboardType.Password,
            isPassword = true
        )

        // Forgot Password Link
        TextButton(
            onClick = { onForgotPassword() },
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Forgot your password?",
                color = FormColors.green
            )
        }

        // Login Button
        Button(
            onClick = { 
                // Validate fields using the shared validation utility
                fieldErrors = validateFields(
                    mapOf(
                        EMAIL to email,
                        PASSWORD to password
                    )
                )

                if (fieldErrors.isNotEmpty()) {
                    
                    errorMessage = "Please enter all required fields"
                    showErrorMessage = true
                } else {
                    // All fields are filled, proceed with login
                    isLoading = true
                    authService.signIn(email, password) { result ->
                        isLoading = false
                        when (result) {
                            is AuthResult.Success -> {
                                onLogin(email)
                            }
                            is AuthResult.Error -> {
                                errorMessage = result.message
                                showErrorMessage = true
                            }
                        }
                    }
                }
            },
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
                Text("Login")
            }
        }

        // General Error Message - now below the login button
        ErrorMessage(show = showErrorMessage, message = errorMessage)

        Spacer(modifier = Modifier.height(16.dp))

        // Sign Up Button
        OutlinedButton(
            onClick = { onSignUp() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = FormColors.green
            ),
            enabled = !isLoading
        ) {
            Text("Sign Up")
        }
    }
}