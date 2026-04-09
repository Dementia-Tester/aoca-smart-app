package org.example.dementia_tester_app.ui.screens.doctor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import org.example.dementia_tester_app.ui.components.LoadingSpinner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.dementia_tester_app.data.DatabaseResult
import org.example.dementia_tester_app.data.UserProfile
import org.example.dementia_tester_app.data.UserProfileService
import org.example.dementia_tester_app.ui.components.HorizontalMenu

/**
 * Doctor Dashboard screen
 * This dashboard is shown to users with userType = DOCTOR
 */
@Composable
fun DoctorDashboard() {
    val menuItems = listOf("View", "Compare", "Games")
    var selectedMenuItem by remember { mutableStateOf(menuItems[0]) }
    val headerColor = Color(0xFF66BB23)
    val activeMenuColor = headerColor
    
    // State for user data
    var userProfiles by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var formattedUserList by remember { mutableStateOf<List<String>>(emptyList()) }
    var userMap by remember { mutableStateOf<Map<String, UserProfile>>(emptyMap()) }
    
    // State for loading and error
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Create UserProfileService instance
    val userProfileService = remember { UserProfileService() }
    
    // Fetch users when the component is first rendered
    LaunchedEffect(Unit) {
        userProfileService.getAllUsers { result ->
            isLoading = false
            when (result) {
                is DatabaseResult.Success -> {
                    userProfiles = result.data
                    
                    // Format users to display only name, or email if name is undefined
                    formattedUserList = userProfiles.map {
                        it.name.ifBlank {
                            it.email
                        }
                    }
                    
                    // Create a mapping between formatted strings and UserProfile objects
                    userMap = userProfiles.associateBy {
                        it.name.ifBlank {
                            it.email
                        }
                    }
                }
                is DatabaseResult.Error -> {
                    errorMessage = result.message
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HorizontalMenu(
            menuItems = menuItems,
            selectedMenuItem = selectedMenuItem,
            activeColor = activeMenuColor,
            onMenuItemSelected = { selectedMenuItem = it }
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    LoadingSpinner()
                }
                // Show error message
                errorMessage != null -> {
                    Text(
                        text = "Error loading users: $errorMessage",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                // Show empty state
                userProfiles.isEmpty() -> {
                    Text(
                        text = "No users found. Please add some users with User type to the system.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                // Show selected screen with user data
                else -> {
                    when (selectedMenuItem) {
                        "View" -> DoctorViewScreen(formattedUserList, userMap)
                        "Compare" -> DoctorCompareScreen(formattedUserList, userMap)
                        "Games" -> DoctorGamesScreen(formattedUserList, userMap)
                    }
                }
            }
        }
    }
}
