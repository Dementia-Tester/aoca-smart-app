package org.example.dementia_tester_app.data

/**
 * Result class to handle database operations
 */
sealed class DatabaseResult<out T> {
    data class Success<T>(val data: T) : DatabaseResult<T>()
    data class Error(val message: String) : DatabaseResult<Nothing>()
}

/**
 * Interface for user profile service
 * This will be implemented differently for Android and iOS
 */
expect class UserProfileService() {
    /**
     * Get the current user's profile
     * @param callback Callback to be invoked with the result of the operation
     */
    fun getCurrentUserProfile(callback: (DatabaseResult<UserProfile>) -> Unit)
    
    /**
     * Update the current user's profile
     * @param userProfile The updated user profile
     * @param callback Callback to be invoked with the result of the operation
     */
    fun updateUserProfile(userProfile: UserProfile, callback: (DatabaseResult<Unit>) -> Unit)
    
    /**
     * Get all users with userType = User
     * @param callback Callback to be invoked with the result of the operation
     */
    fun getAllUsers(callback: (DatabaseResult<List<UserProfile>>) -> Unit)

    /**
     * Upload a profile image
     * @param imageBytes The image data as a ByteArray
     * @param callback Callback to be invoked with the result of the operation, containing the URL of the uploaded image
     */
    fun uploadProfileImage(imageBytes: ByteArray, callback: (DatabaseResult<String>) -> Unit)
}