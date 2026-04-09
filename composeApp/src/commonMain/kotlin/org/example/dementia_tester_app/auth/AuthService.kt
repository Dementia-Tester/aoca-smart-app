package org.example.dementia_tester_app.auth

/**
 * Result class to handle authentication operations
 */
sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Interface for authentication service
 */
expect class AuthService() {
    /**
     * Sign in with email and password
     * @param email User's email
     * @param password User's password
     * @param callback Callback to be invoked with the result of the operation
     */
    fun signIn(email: String, password: String, callback: (AuthResult) -> Unit)

    /**
     * Sign up with email and password
     * @param email User's email
     * @param password User's password
     * @param callback Callback to be invoked with the result of the operation
     */
    fun signUp(email: String, password: String, callback: (AuthResult) -> Unit)

    /**
     * Send password reset email
     * @param email User's email
     * @param callback Callback to be invoked with the result of the operation
     */
    fun sendPasswordResetEmail(email: String, callback: (AuthResult) -> Unit)

    /**
     * Send email verification to the current user
     * @param callback Callback to be invoked with the result of the operation
     */
    fun sendEmailVerification(callback: (AuthResult) -> Unit)

    /**
     * Check if the current user's email is verified
     * @return true if the email is verified, false otherwise
     */
    fun isEmailVerified(): Boolean

    /**
     * Reload the current user's data to get the latest status
     * @param callback Callback to be invoked with the result of the operation
     */
    fun reloadUser(callback: (AuthResult) -> Unit)

    /**
     * Sign out the current user
     */
    fun signOut()

    /**
     * Check if a user is currently signed in
     * @return true if a user is signed in, false otherwise
     */
    fun isUserSignedIn(): Boolean

    /**
     * Get the current user's ID
     * @return the user's ID if signed in, null otherwise
     */
    fun getCurrentUserId(): String?
}