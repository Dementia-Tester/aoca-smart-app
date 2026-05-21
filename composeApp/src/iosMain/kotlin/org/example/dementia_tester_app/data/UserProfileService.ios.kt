package org.example.dementia_tester_app.data

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseFirestore.*
import cocoapods.FirebaseStorage.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.refTo
import platform.darwin.NSObject

private fun Map<String, Any?>.toObjcMap(): Map<Any?, Any?> =
    this.entries.associate { (k: String, v: Any?) ->
        (k as Any?) to (v ?: NSNull())
    }

actual class UserProfileService {
    private val collectionPath = "UserProfiles"

    /**
     * Get the current user's profile from Firestore
     */
    actual fun getCurrentUserProfile(callback: (DatabaseResult<UserProfile>) -> Unit) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }
        
        val db = FIRFirestore.firestore()
        db.collectionWithPath(collectionPath).documentWithID(userId).getDocumentWithCompletion { snapshot, error ->
            if (error != null) {
                callback(DatabaseResult.Error("Failed to get user profile: ${error.localizedDescription}"))
                return@getDocumentWithCompletion
            }
            
            if (snapshot == null || !snapshot.exists()) {
                callback(DatabaseResult.Error("Profile not found. Please create a profile."))
                return@getDocumentWithCompletion
            }
            
            try {
                val data = snapshot.data() as? Map<String, Any?>
                if (data != null) {
                    val profile = UserProfile.fromMap(data, userId)
                    callback(DatabaseResult.Success(profile))
                } else {
                    callback(DatabaseResult.Error("Profile data is empty"))
                }
            } catch (e: Exception) {
                callback(DatabaseResult.Error("Failed to parse user profile: ${e.message}"))
            }
        }
    }

    /**
     * Update the current user's profile in Firestore
     */
    actual fun updateUserProfile(userProfile: UserProfile, callback: (DatabaseResult<Unit>) -> Unit) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }

        val db = FIRFirestore.firestore()
        val docRef = db.collectionWithPath(collectionPath).documentWithID(userId)
        
        docRef.getDocumentWithCompletion { snapshot, error ->
            if (error != null) {
                callback(DatabaseResult.Error("Security check failed: ${error.localizedDescription}"))
                return@getDocumentWithCompletion
            }
            
            val updates = userProfile.toMap().toMutableMap()
            if (snapshot != null && snapshot.exists()) {
                val currentData = snapshot.data() as? Map<String, Any?>
                if (currentData != null) {
                    updates["userType"] = currentData["userType"] ?: "user"
                    updates["email"] = currentData["email"] ?: ""
                }
            }
            
            docRef.setData(updates.toObjcMap(), true) { err ->
                if (err != null) {
                    callback(DatabaseResult.Error("Failed to update user profile: ${err.localizedDescription}"))
                } else {
                    callback(DatabaseResult.Success(Unit))
                }
            }
        }
    }

    /**
     * Get all users with userType = User
     */
    actual fun getAllUsers(callback: (DatabaseResult<List<UserProfile>>) -> Unit) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }
        
        val db = FIRFirestore.firestore()
        db.collectionWithPath(collectionPath).documentWithID(userId).getDocumentWithCompletion { snapshot, error ->
            if (error != null) {
                callback(DatabaseResult.Error("Authorization check failed: ${error.localizedDescription}"))
                return@getDocumentWithCompletion
            }
            
            val userType = snapshot?.data()?.get("userType") as? String
            if (userType != "doctor") {
                callback(DatabaseResult.Error("Not authorized. Only doctors can access user data."))
                return@getDocumentWithCompletion
            }
            
            db.collectionWithPath(collectionPath)
                .queryWhereField("userType", isEqualTo = "user")
                .getDocumentsWithCompletion { querySnapshot, queryError ->
                    if (queryError != null) {
                        callback(DatabaseResult.Error("Failed to fetch users: ${queryError.localizedDescription}"))
                        return@getDocumentsWithCompletion
                    }
                    
                    try {
                        val profiles = querySnapshot?.documents?.mapNotNull { doc ->
                            val docSnapshot = doc as FIRDocumentSnapshot
                            val data = docSnapshot.data() as? Map<String, Any?>
                            data?.let { UserProfile.fromMap(it, docSnapshot.documentID) }
                        } ?: emptyList()
                        callback(DatabaseResult.Success(profiles))
                    } catch (e: Exception) {
                        callback(DatabaseResult.Error("Failed to parse users: ${e.message}"))
                    }
                }
        }
    }

    /**
     * Upload a profile image to Firebase Storage and return the download URL
     */
    actual fun uploadProfileImage(imageBytes: ByteArray, callback: (DatabaseResult<String>) -> Unit) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }

        val storage = FIRStorage.storage()
        val storageRef = storage.reference()
        val profileImageRef = storageRef.child("profile_images/${userId}.jpg")

        val data = imageBytes.toNSData()
        val metadata = FIRStorageMetadata()
        metadata.contentType = "image/jpeg"

        profileImageRef.putData(data, metadata) { _, error ->
            if (error != null) {
                callback(DatabaseResult.Error("Failed to upload image: ${error.localizedDescription}"))
                return@putData
            }
            
            profileImageRef.downloadURLWithCompletion { url, downloadError ->
                if (downloadError != null) {
                    callback(DatabaseResult.Error("Failed to get download URL: ${downloadError.localizedDescription}"))
                } else if (url != null) {
                    val downloadUrl = url.absoluteString!!
                    
                    // Automatically update the Firestore document with the new URL
                    val db = FIRFirestore.firestore()
                    db.collectionWithPath(collectionPath).documentWithID(userId)
                        .updateData(mapOf("profileImageUrl" to downloadUrl)) { updateError ->
                            if (updateError != null) {
                                // We still return success for the upload, but log or inform
                                callback(DatabaseResult.Success(downloadUrl))
                            } else {
                                callback(DatabaseResult.Success(downloadUrl))
                            }
                        }
                } else {
                    callback(DatabaseResult.Error("Download URL is null"))
                }
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ByteArray.toNSData(): NSData {
        if (isEmpty()) return NSData()
        return usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), size.toULong())
        }
    }
}
