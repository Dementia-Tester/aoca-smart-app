package org.example.dementia_tester_app.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun GoogleSignInScreen(
    onSignInSuccess: () -> Unit,
    onSignInError: (String) -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    
    // Replace with the Web Client ID from your Firebase Console > Authentication > Google
    val webClientId = "YOUR_WEB_CLIENT_ID_HERE" 

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { idToken ->
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { authResult ->
                            if (authResult.isSuccessful) {
                                val user = auth.currentUser
                                val isNewUser = authResult.result?.additionalUserInfo?.isNewUser == true
                                
                                if (user != null && isNewUser) {
                                    // Comply with firestore.rules for new users
                                    val userProfile = hashMapOf(
                                        "email" to (user.email ?: ""),
                                        "userId" to user.uid,
                                        "userType" to "user" // Required by your security rules
                                    )
                                    
                                    db.collection("users").document(user.uid)
                                        .set(userProfile)
                                        .addOnSuccessListener {
                                            Log.d("GoogleAuth", "Firestore profile created")
                                            onSignInSuccess()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("GoogleAuth", "Failed to create profile", e)
                                            onSignInError("Failed to setup user profile.")
                                        }
                                } else {
                                    // Existing user
                                    onSignInSuccess()
                                }
                            } else {
                                Log.e("GoogleAuth", "Firebase auth failed", authResult.exception)
                                onSignInError(authResult.exception?.localizedMessage ?: "Auth failed")
                            }
                        }
                }
            } catch (e: ApiException) {
                Log.e("GoogleAuth", "Google sign in failed", e)
                onSignInError("Google sign in canceled or failed.")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { launcher.launch(googleSignInClient.signInIntent) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Continue with Google")
        }
    }
}
