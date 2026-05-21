package org.example.dementia_tester_app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import android.util.Log

/**
 * Android implementation of UserProfileService using Firebase Firestore and Storage
 */
actual class UserProfileService {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage
    private val collectionPath = "UserProfiles"
    private val TAG = "UserProfileService"

    /**
     * Get the current user's profile from Firestore
     */
    actual fun getCurrentUserProfile(callback: (DatabaseResult<UserProfile>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }
        
        Log.d(TAG, "Fetching profile for user: $userId")
        firestore.collection(collectionPath).document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Log.w(TAG, "Profile not found for $userId")
                    callback(DatabaseResult.Error("Profile not found. Please create a profile."))
                    return@addOnSuccessListener
                }
                
                try {
                    val data = document.data
                    if (data != null) {
                        val profile = UserProfile.fromMap(data, userId)
                        callback(DatabaseResult.Success(profile))
                    } else {
                        callback(DatabaseResult.Error("Profile data is empty"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Parsing error", e)
                    callback(DatabaseResult.Error("Failed to parse user profile: ${e.message}"))
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore fetch error", e)
                callback(DatabaseResult.Error("Failed to get user profile: ${e.message}"))
            }
    }

    /**
     * Update the current user's profile in Firestore
     */
    actual fun updateUserProfile(userProfile: UserProfile, callback: (DatabaseResult<Unit>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }

        Log.d(TAG, "Updating profile for user: $userId")
        // Fetch existing profile first to preserve sensitive fields (role/email)
        firestore.collection(collectionPath).document(userId).get()
            .addOnSuccessListener { document ->
                val updates = userProfile.toMap().toMutableMap()
                
                if (document.exists()) {
                    // Force keep original role and email
                    updates["userType"] = document.getString("userType") ?: "user"
                    updates["email"] = document.getString("email") ?: ""
                }
                
                firestore.collection(collectionPath).document(userId)
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "Profile updated successfully")
                        callback(DatabaseResult.Success(Unit))
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Update failure", e)
                        callback(DatabaseResult.Error("Failed to update user profile: ${e.message}"))
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Security check fetch failure", e)
                callback(DatabaseResult.Error("Security Check Failed: ${e.message}"))
            }
    }

    /**
     * Get all users with userType = User
     */
    actual fun getAllUsers(callback: (DatabaseResult<List<UserProfile>>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }
        
        // First check if current user is authorized (doctor)
        firestore.collection(collectionPath).document(userId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    callback(DatabaseResult.Error("Profile not found"))
                    return@addOnSuccessListener
                }
                
                val userType = document.getString("userType")
                if (userType != "doctor") {
                    callback(DatabaseResult.Error("Not authorized. Only doctors can access user data."))
                    return@addOnSuccessListener
                }
                
                fetchAllUsers(callback)
            }
            .addOnFailureListener { e ->
                callback(DatabaseResult.Error("Authorization check failed: ${e.message}"))
            }
    }
    
    private fun fetchAllUsers(callback: (DatabaseResult<List<UserProfile>>) -> Unit) {
        firestore.collection(collectionPath)
            .whereEqualTo("userType", "user")
            .get()
            .addOnSuccessListener { snapshot ->
                try {
                    val userProfiles = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { UserProfile.fromMap(it, doc.id) }
                    }
                    callback(DatabaseResult.Success(userProfiles))
                } catch (e: Exception) {
                    callback(DatabaseResult.Error("Failed to fetch users: ${e.message}"))
                }
            }
            .addOnFailureListener { e ->
                callback(DatabaseResult.Error("Failed to fetch users: ${e.message}"))
            }
    }

    /**
     * Upload a profile image to Firebase Storage and return the download URL
     */
    actual fun uploadProfileImage(imageBytes: ByteArray, callback: (DatabaseResult<String>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }

        Log.d(TAG, "Uploading profile image for user: $userId")
        val profileImageRef = storage.reference.child("profile_images/${userId}.jpg")

        profileImageRef.putBytes(imageBytes)
            .addOnSuccessListener { _ ->
                Log.d(TAG, "Image uploaded successfully, getting download URL")
                profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                    val url = uri.toString()
                    Log.d(TAG, "Download URL: $url")
                    
                    // Automatically update the Firestore document with the new URL
                    firestore.collection(collectionPath).document(userId)
                        .update("profileImageUrl", url)
                        .addOnSuccessListener {
                            callback(DatabaseResult.Success(url))
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to update profileImageUrl in Firestore", e)
                            // We still return success for the upload, but inform about the URL
                            callback(DatabaseResult.Success(url))
                        }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Failed to get download URL", e)
                    callback(DatabaseResult.Error("Failed to get download URL: ${e.message}"))
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Upload failed", e)
                callback(DatabaseResult.Error("Failed to upload image: ${e.message}"))
            }
    }
}
