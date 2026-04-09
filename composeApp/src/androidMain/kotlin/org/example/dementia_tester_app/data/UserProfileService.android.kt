package org.example.dementia_tester_app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * Android implementation of UserProfileService using Firebase Realtime Database
 */
actual class UserProfileService {
    private val auth = FirebaseAuth.getInstance()
    private val database = Firebase.database.reference
    private val dbPath = "UserProfiles"

    /**
     * Get the current user's profile
     */
    actual fun getCurrentUserProfile(callback: (DatabaseResult<UserProfile>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }
        // Fetch the user profile
        database.child(dbPath).child(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    callback(DatabaseResult.Error("Profile not found. Please create a profile."))
                    return@addOnSuccessListener
                }
                
                try {
                    // Convert snapshot to Map and use UserProfile.fromMap to create the profile
                    val data = snapshot.value as? Map<*, *>
                    if (data != null) {
                        val profile = UserProfile.fromMap(data, userId)
                        callback(DatabaseResult.Success(profile))
                    } else {
                        callback(DatabaseResult.Error("Profile data is empty or in wrong format"))
                    }
                } catch (e: Exception) {
                    callback(DatabaseResult.Error("Failed to parse user profile: ${e.message}"))
                }
            }
            .addOnFailureListener { e ->
                callback(DatabaseResult.Error("Failed to get user profile: ${e.message}"))
            }
    }

    /**
     * Update the current user's profile
     */
    actual fun updateUserProfile(userProfile: UserProfile, callback: (DatabaseResult<Unit>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }

        val profileData = userProfile.toMap()
        
        // Update the user profile
        database.child(dbPath).child(userId)
            .updateChildren(profileData)
            .addOnSuccessListener {
                callback(DatabaseResult.Success(Unit))
            }
            .addOnFailureListener { e ->
                callback(DatabaseResult.Error("Failed to update user profile: ${e.message}"))
            }
    }

    /**
     * Get all users with userType = User
     * Only doctors are authorized to access this method
     */
    actual fun getAllUsers(callback: (DatabaseResult<List<UserProfile>>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }
        
        // First check if the current user is a doctor
        database.child(dbPath).child(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    callback(DatabaseResult.Error("Profile not found. Please create a profile."))
                    return@addOnSuccessListener
                }
                
                try {
                    // Convert snapshot to Map and use UserProfile.fromMap to create the profile
                    val data = snapshot.value as? Map<*, *>
                    if (data != null) {
                        val currentUserProfile = UserProfile.fromMap(data, userId)
                        
                        // Check if the current user is a doctor
                        if (currentUserProfile.userType != UserType.DOCTOR) {
                            callback(DatabaseResult.Error("Not authorized. Only doctors can access user data."))
                            return@addOnSuccessListener
                        }
                        
                        // If the user is a doctor, proceed to fetch all users
                        fetchAllUsers(callback)
                    } else {
                        callback(DatabaseResult.Error("Profile data is empty or in wrong format"))
                    }
                } catch (e: Exception) {
                    callback(DatabaseResult.Error("Failed to parse user profile: ${e.message}"))
                }
            }
            .addOnFailureListener { e ->
                callback(DatabaseResult.Error("Failed to get user profile: ${e.message}"))
            }
    }
    
    /**
     * Helper method to fetch all users with userType = User
     */
    private fun fetchAllUsers(callback: (DatabaseResult<List<UserProfile>>) -> Unit) {
        // Query all user profiles
        database.child(dbPath).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val userProfiles = mutableListOf<UserProfile>()
                    
                    // Iterate through all user profiles
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.key
                        val userData = userSnapshot.value as? Map<*, *>
                        if (userData != null && userId != null) {
                            val userProfile = UserProfile.fromMap(userData, userId)
                            
                            // Only include users with userType = User
                            if (userProfile.userType == UserType.USER) {
                                userProfiles.add(userProfile)
                            }
                        }
                    }
                    
                    callback(DatabaseResult.Success(userProfiles))
                } catch (e: Exception) {
                    callback(DatabaseResult.Error("Failed to fetch users: ${e.message}"))
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                callback(DatabaseResult.Error("Failed to fetch users: ${error.message}"))
            }
        })
    }
}
