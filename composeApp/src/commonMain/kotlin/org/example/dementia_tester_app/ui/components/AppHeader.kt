package org.example.dementia_tester_app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.dementia_tester_app.data.DatabaseResult
import org.example.dementia_tester_app.data.UserProfileService
import org.example.dementia_tester_app.data.UserType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import org.example.dementia_tester_app.utils.calculateAgeFromDateOfBirth

@Composable
fun AppHeader(
    drawerState: DrawerState,
    title: String
) {
    val scope = rememberCoroutineScope()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF66BB23)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.padding(start = 5.dp, end = 5.dp, top = 50.dp, bottom = 10.dp)
        ) {
            TextButton(
                onClick = {
                    scope.launch {
                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                    }
                },
                modifier = Modifier.padding(end = 30.dp)
            ) {
                Text(
                    text = "☰",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )
        }
    }
}

@Composable
fun AppMenuContent(
    onMenuItemClick: (String) -> Unit,
    refreshKey: Int = 0
) {
    // Create UserProfileService instance
    val userProfileService = remember { UserProfileService() }
    
    // State variables for user profile data
    var userName by remember { mutableStateOf("") }
    var userAge by remember { mutableStateOf<Int?>(null) }
    var userType by remember { mutableStateOf(UserType.USER) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Fetch user profile when component is first rendered
    LaunchedEffect(refreshKey) {
        userProfileService.getCurrentUserProfile { result ->
            isLoading = false
            when (result) {
                is DatabaseResult.Success -> {
                    val profile = result.data
                    userName = profile.name.ifEmpty { "User" }
                    userAge = calculateAgeFromDateOfBirth(profile.dateOfBirth)
                    userType = profile.userType
                    errorMessage = null
                }
                is DatabaseResult.Error -> {
                    errorMessage = "Failed to load profile"
                }
            }
        }
    }
    
    ModalDrawerSheet(
        modifier = Modifier.width(220.dp)
    ) {
        Text(
            "Menu",
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.padding(8.dp))

        // Profile section - clickable
        TextButton(
            onClick = { onMenuItemClick("Profile") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Profile icon with gender-based color
                Icon(
                    imageVector     = Icons.Default.Person,
                    contentDescription = "Profile Icon",
                    tint            = Color.Black,
                    modifier        = Modifier.size(70.dp)
                )
                
                if (isLoading) {
                    // Show loading indicator
                    LoadingSpinner()
                } else if (errorMessage != null) {
                    // Show error message
                    Text(
                        "Tap to set up profile",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                } else {
                    // User name
                    Text(
                        userName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    // User age
                    Text(
                        if (userAge != null) "Age: $userAge" else "Age: -",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Divider between profile and menu items
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        // Different menu items for different user types
        val items = if (userType == UserType.DOCTOR) {
            listOf(
                "📊" to "Dashboard",
                "📞" to "Contact",
                "⚙️" to "Settings",
                "❓" to "Help"
            )
        } else {
            // Menu items for regular users
            listOf(
                "📊" to "Dashboard",
                "🩺" to "Health Survey",
                "🏃" to "Activities",
                "📅" to "Book Appointment",
                "📜" to "Appointment History",
                "📞" to "Contact",
                "💬" to "Chat",
                "⚙️" to "Settings",
                "❓" to "Help"
            )
        }
        items.forEach { (icon, label) ->
            TextButton(
                onClick = { onMenuItemClick(label) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(icon, fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                    Text(label, fontSize = 18.sp)
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )
        TextButton(
            onClick = { onMenuItemClick("logout") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🚪", fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                Text("Logout", fontSize = 18.sp)
            }
        }
    }
}

