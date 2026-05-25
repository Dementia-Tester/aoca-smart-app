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
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "getCurrentUserProfile: No user signed in")
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }
        
        Log.d(TAG, "Fetching profile for user: $userId (Email: ${currentUser.email})")
        firestore.collection(collectionPath).document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Log.w(TAG, "Profile document does not exist for UID: $userId")
                    callback(DatabaseResult.Error("Profile not found. Please create a profile."))
                    return@addOnSuccessListener
                }
                
                try {
                    val data = document.data
                    if (data != null) {
                        Log.d(TAG, "Successfully retrieved profile data for $userId. userType: ${data["userType"]}")
                        val profile = UserProfile.fromMap(data, userId)
                        callback(DatabaseResult.Success(profile))
                    } else {
                        Log.e(TAG, "Profile document exists but data is null for $userId")
                        callback(DatabaseResult.Error("Profile data is empty"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing UserProfile for $userId", e)
                    callback(DatabaseResult.Error("Failed to parse user profile: ${e.message}"))
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore GET failed for UserProfiles/$userId. Error: ${e.message}", e)
                callback(DatabaseResult.Error("Failed to get user profile: ${e.message}"))
            }
    }

    /**
     * Update the current user's profile in Firestore
     */
    actual fun updateUserProfile(userProfile: UserProfile, callback: (DatabaseResult<Unit>) -> Unit) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "updateUserProfile: No user signed in")
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }

        Log.d(TAG, "Updating profile for UID: $userId. New data: ${userProfile.toMap()}")
        // Fetch existing profile first to preserve sensitive fields (role/email)
        firestore.collection(collectionPath).document(userId).get()
            .addOnSuccessListener { document ->
                val updates = userProfile.toMap().toMutableMap()
                
                if (document.exists()) {
                    val existingUserType = document.getString("userType") ?: "user"
                    val existingEmail = document.getString("email") ?: ""
                    Log.d(TAG, "Existing profile found for $userId. userType: $existingUserType, email: $existingEmail")
                    
                    // Force keep original role and email to prevent unauthorized escalation or corruption
                    updates["userType"] = existingUserType
                    updates["email"] = existingEmail
                } else {
                    Log.i(TAG, "No existing profile for $userId. Performing initial creation.")
                    // If it's the first time, we ensure the email from Auth is used
                    if (updates["email"] == null || (updates["email"] as String).isEmpty()) {
                        updates["email"] = currentUser.email ?: ""
                    }
                }
                
                firestore.collection(collectionPath).document(userId)
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.i(TAG, "Profile for $userId updated/created successfully in Firestore")
                        callback(DatabaseResult.Success(Unit))
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Firestore SET/MERGE failed for UserProfiles/$userId. Error: ${e.message}", e)
                        callback(DatabaseResult.Error("Failed to update user profile: ${e.message}"))
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Pre-update fetch failed for UserProfiles/$userId. This often indicates a Security Rules issue. Error: ${e.message}", e)
                callback(DatabaseResult.Error("Security Check Failed: ${e.message}"))
            }
    }

    /**
     * Get all users with userType = User
     */
    actual fun getAllUsers(callback: (DatabaseResult<List<UserProfile>>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "getAllUsers: No user signed in")
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }
        
        Log.d(TAG, "Doctor $userId is requesting list of all users")
        // First check if current user is authorized (doctor)
        firestore.collection(collectionPath).document(userId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Log.w(TAG, "Doctor profile not found for UID: $userId")
                    callback(DatabaseResult.Error("Profile not found"))
                    return@addOnSuccessListener
                }
                
                val userType = document.getString("userType")
                Log.d(TAG, "Requesting user $userId has userType: $userType")
                
                if (userType != "doctor") {
                    Log.e(TAG, "Access Denied: User $userId is not a doctor (userType=$userType)")
                    callback(DatabaseResult.Error("Not authorized. Only doctors can access user data."))
                    return@addOnSuccessListener
                }
                
                fetchAllUsers(callback)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Authorization check failed for getAllUsers. Error: ${e.message}", e)
                callback(DatabaseResult.Error("Authorization check failed: ${e.message}"))
            }
    }
    
    private fun fetchAllUsers(callback: (DatabaseResult<List<UserProfile>>) -> Unit) {
        Log.d(TAG, "Fetching all users with userType='user'")
        firestore.collection(collectionPath)
            .whereEqualTo("userType", "user")
            .get()
            .addOnSuccessListener { snapshot ->
                try {
                    val userProfiles = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { UserProfile.fromMap(it, doc.id) }
                    }
                    Log.d(TAG, "Successfully fetched ${userProfiles.size} users")
                    callback(DatabaseResult.Success(userProfiles))
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing user list", e)
                    callback(DatabaseResult.Error("Failed to fetch users: ${e.message}"))
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore query failed for fetchAllUsers. Error: ${e.message}", e)
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
