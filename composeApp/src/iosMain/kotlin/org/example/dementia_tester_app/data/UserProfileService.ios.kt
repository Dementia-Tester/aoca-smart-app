package org.example.dementia_tester_app.data

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import platform.Foundation.NSDictionary
import platform.Foundation.NSError
import platform.Foundation.NSNull
import cocoapods.FirebaseStorage.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

private fun Map<String, Any?>.toObjcMap(): Map<Any?, Any?> =
    this.entries.associate { (k: String, v: Any?) ->
        (k as Any?) to (v ?: NSNull())
    }

actual class UserProfileService {
    private val dbPath = "UserProfiles"

    /**
     * Get the current user's profile
     * @param callback Callback to be invoked with the result of the operation
     */
    actual fun getCurrentUserProfile(callback: (DatabaseResult<UserProfile>) -> Unit) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) {
            callback(DatabaseResult.Error("Firebase not initialized"))
            return
        }

        ref.child(dbPath).child(userId).getDataWithCompletionBlock { error: NSError?, snapshot: FIRDataSnapshot? ->
            if (error != null) {
                callback(DatabaseResult.Error("Failed to get user profile: ${'$'}{error.localizedDescription}"))
                return@getDataWithCompletionBlock
            }
            if (snapshot == null || !snapshot.exists()) {
                callback(DatabaseResult.Error("Profile not found. Please create a profile."))
                return@getDataWithCompletionBlock
            }
            try {
                val data = snapshotToMap(snapshot)
                if (data != null) {
                    val profile = UserProfile.fromMap(data, userId as String)
                    callback(DatabaseResult.Success(profile))
                } else {
                    callback(DatabaseResult.Error("Profile data is empty or in wrong format"))
                }
            } catch (t: Throwable) {
                callback(DatabaseResult.Error("Failed to parse user profile: ${'$'}{t.message}"))
            }
        }
    }
    
    /**
     * Update the current user's profile
     * @param userProfile The updated user profile
     * @param callback Callback to be invoked with the result of the operation
     */
    actual fun updateUserProfile(userProfile: UserProfile, callback: (DatabaseResult<Unit>) -> Unit) {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) {
            callback(DatabaseResult.Error("Firebase not initialized"))
            return
        }
        
        val profileData: Map<Any?, Any?> = userProfile.toMap().toObjcMap()
        
        ref.child(dbPath).child(userId).updateChildValues(profileData) { error: NSError?, _ ->
            if (error == null) {
                callback(DatabaseResult.Success(Unit))
            } else {
                callback(DatabaseResult.Error("Failed to update user profile: ${'$'}{error.localizedDescription}"))
            }
        }
    }

    /**
     * Get all users with userType = User
     * @param callback Callback to be invoked with the result of the operation
     */
    actual fun getAllUsers(callback: (DatabaseResult<List<UserProfile>>) -> Unit)  {
        val userId = FIRAuth.auth()?.currentUser()?.uid()
        if (userId == null) {
            callback(DatabaseResult.Error("No user is signed in"))
            return
        }
        val ref = FIRDatabase.database()?.reference()
        if (ref == null) {
            callback(DatabaseResult.Error("Firebase not initialized"))
            return
        }

        // First check if the current user is a doctor
        ref.child(dbPath).child(userId).getDataWithCompletionBlock { error: NSError?, snapshot: FIRDataSnapshot? ->
            if (error != null) {
                callback(DatabaseResult.Error("Failed to get user profile: ${'$'}{error.localizedDescription}"))
                return@getDataWithCompletionBlock
            }
            if (snapshot == null || !snapshot.exists()) {
                callback(DatabaseResult.Error("Profile not found. Please create a profile."))
                return@getDataWithCompletionBlock
            }

            try {
                val data = snapshotToMap(snapshot)
                if (data == null) {
                    callback(DatabaseResult.Error("Profile data is empty or in wrong format"))
                    return@getDataWithCompletionBlock
                }
                val currentUserProfile = UserProfile.fromMap(data, userId)

                if (currentUserProfile.userType != UserType.DOCTOR) {
                    callback(DatabaseResult.Error("Not authorized. Only doctors can access user data."))
                    return@getDataWithCompletionBlock
                }

                // If the user is a doctor, proceed to fetch all users
                ref.child(dbPath).getDataWithCompletionBlock { err2: NSError?, snapAll: FIRDataSnapshot? ->
                    if (err2 != null) {
                        callback(DatabaseResult.Error("Failed to fetch users: ${'$'}{err2.localizedDescription}"))
                        return@getDataWithCompletionBlock
                    }
                    if (snapAll == null || !snapAll.exists()) {
                        callback(DatabaseResult.Success(emptyList()))
                        return@getDataWithCompletionBlock
                    }

                    try {
                        val value = snapAll.value
                        val userProfiles = mutableListOf<UserProfile>()
                        when (value) {
                            is Map<*, *> -> {
                                for ((k, v) in value) {
                                    val uid = k?.toString() ?: continue
                                    val userData = when (v) {
                                        is Map<*, *> -> v
                                        is NSDictionary -> nsDictionaryToKotlinMap(v)
                                        else -> null
                                    } ?: continue
                                    val userProfile = UserProfile.fromMap(userData, uid)
                                    if (userProfile.userType == UserType.USER) {
                                        userProfiles.add(userProfile)
                                    }
                                }
                            }
                            is NSDictionary -> {
                                val dict = value
                                val keys = dict.allKeys as List<*>
                                for (k in keys) {
                                    val uid = k?.toString() ?: continue
                                    val v = dict.objectForKey(k)
                                    val userData = when (v) {
                                        is Map<*, *> -> v
                                        is NSDictionary -> nsDictionaryToKotlinMap(v)
                                        else -> null
                                    } ?: continue
                                    val userProfile = UserProfile.fromMap(userData, uid)
                                    if (userProfile.userType == UserType.USER) {
                                        userProfiles.add(userProfile)
                                    }
                                }
                            }
                            else -> {
                                // Unexpected type
                            }
                        }
                        callback(DatabaseResult.Success(userProfiles))
                    } catch (t: Throwable) {
                        callback(DatabaseResult.Error("Failed to fetch users: ${'$'}{t.message}"))
                    }
                }
            } catch (t: Throwable) {
                callback(DatabaseResult.Error("Failed to parse user profile: ${'$'}{t.message}"))
            }
        }
    }

    private fun snapshotToMap(snapshot: FIRDataSnapshot): Map<*, *>? {
        val value = snapshot.value
        return when (value) {
            is Map<*, *> -> value
            is NSDictionary -> nsDictionaryToKotlinMap(value)
            else -> null
        }
    }

    private fun nsDictionaryToKotlinMap(dict: NSDictionary): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        val keys = dict.allKeys as List<*>
        for (k in keys) {
            val keyStr = k?.toString() ?: continue
            val v = dict.objectForKey(k)
            result[keyStr] = when (v) {
                is NSDictionary -> nsDictionaryToKotlinMap(v)
                else -> v
            }
        }
        return result
    }

    /**
     * Upload a profile image to Firebase Storage
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
                    callback(DatabaseResult.Success(url.absoluteString!!))
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
