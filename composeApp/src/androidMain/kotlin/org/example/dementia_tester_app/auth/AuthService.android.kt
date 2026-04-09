package org.example.dementia_tester_app.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/**
 * Android implementation of AuthService using Firebase Auth
 */
actual class AuthService {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Sign in with email and password
     * @param email User's email
     * @param password User's password
     * @param callback Callback to be invoked with the result of the operation
     */
    actual fun signIn(email: String, password: String, callback: (AuthResult) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    callback(AuthResult.Success)
                } else {
                    // Sign in failed
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> "Invalid credentials."
                        is FirebaseAuthInvalidCredentialsException -> "Invalid credentials."
                        else -> "Authentication failed: ${task.exception?.message ?: "Unknown error"}"
                    }
                    callback(AuthResult.Error(errorMessage))
                }
            }
    }

    /**
     * Sign up with email and password
     * @param email User's email
     * @param password User's password
     * @param callback Callback to be invoked with the result of the operation
     */
    actual fun signUp(email: String, password: String, callback: (AuthResult) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign up success
                    callback(AuthResult.Success)
                } else {
                    // Sign up failed
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthWeakPasswordException -> "Password is too weak."
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                        is FirebaseAuthUserCollisionException -> "User with this email already exists."
                        else -> "Registration failed: ${task.exception?.message ?: "Unknown error"}"
                    }
                    callback(AuthResult.Error(errorMessage))
                }
            }
    }

    /**
     * Send password reset email
     * @param email User's email
     * @param callback Callback to be invoked with the result of the operation
     */
    actual fun sendPasswordResetEmail(email: String, callback: (AuthResult) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Email sent
                    callback(AuthResult.Success)
                } else {
                    // Email not sent
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> "No user found with this email."
                        else -> "Failed to send password reset email: ${task.exception?.message ?: "Unknown error"}"
                    }
                    callback(AuthResult.Error(errorMessage))
                }
            }
    }

    /**
     * Send email verification to the current user
     * @param callback Callback to be invoked with the result of the operation
     */
    actual fun sendEmailVerification(callback: (AuthResult) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(AuthResult.Success)
                    } else {
                        val errorMessage = "Failed to send verification email: ${task.exception?.message ?: "Unknown error"}"
                        callback(AuthResult.Error(errorMessage))
                    }
                }
        } else {
            callback(AuthResult.Error("No user is currently signed in"))
        }
    }

    /**
     * Check if the current user's email is verified
     * @return true if the email is verified, false otherwise
     */
    actual fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified ?: false
    }

    /**
     * Reload the current user's data to get the latest status
     * @param callback Callback to be invoked with the result of the operation
     */
    actual fun reloadUser(callback: (AuthResult) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.reload()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(AuthResult.Success)
                    } else {
                        val errorMessage = "Failed to reload user: ${task.exception?.message ?: "Unknown error"}"
                        callback(AuthResult.Error(errorMessage))
                    }
                }
        } else {
            callback(AuthResult.Error("No user is currently signed in"))
        }
    }

    /**
     * Sign out the current user
     */
    actual fun signOut() {
        auth.signOut()
    }

    /**
     * Check if a user is currently signed in
     * @return true if a user is signed in, false otherwise
     */
    actual fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Get the current user's ID
     * @return the user's ID if signed in, null otherwise
     */
    actual fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}