package org.example.dementia_tester_app.ui.screens.doctor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.dementia_tester_app.data.UserQuizService
import org.example.dementia_tester_app.data.UserQuizType
import org.example.dementia_tester_app.data.DatabaseResult
import org.example.dementia_tester_app.data.GraphableAttempts
import org.example.dementia_tester_app.data.UserProfile
import org.example.dementia_tester_app.data.UserResults
import org.example.dementia_tester_app.ui.components.PatientSelect
import org.example.dementia_tester_app.ui.components.UserTestResults

/**
 * View screen for doctors
 * Allows doctors to select a user and view their results
 * 
 * @param userList List of users in the format "Users name - Users email"
 * @param userMap Mapping between formatted user strings and UserProfile objects
 */
@Composable
fun DoctorViewScreen(
    userList: List<String>,
    userMap: Map<String, UserProfile>
) {
    // Create an instance of UserQuizService for Cognitive Assessment
    val cognitiveAssessmentService = remember { UserQuizService(UserQuizType.CognitiveAssessment) }

    var selectedUserString by remember { mutableStateOf<String?>(null) }
    var selectedUser by remember { mutableStateOf<UserProfile?>(null) }

    var hasScores by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var testResults by remember { mutableStateOf<List<UserResults>>(emptyList()) }

    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Title
        Text(
            text = "Patient Results",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // User selection dropdown
        PatientSelect(
            userList = userList,
            userMap = userMap,
            selectedUserString = selectedUserString,
            onUserSelected = { string, profile ->
                selectedUserString = string
                selectedUser = profile
                hasScores = false
                isLoading = false
                testResults = emptyList()
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Display selected user's results if a user is selected
        if (selectedUser != null) {

            // Fetch user scores when a user is selected
            LaunchedEffect(selectedUser) {
                selectedUser?.userId?.let { userId ->
                    isLoading = true

                    cognitiveAssessmentService.getUserScores(userId) { result ->
                        isLoading = false
                        when (result) {
                            is DatabaseResult.Success -> {
                                testResults = result.data
                                hasScores = result.data.isNotEmpty()
                            }
                            is DatabaseResult.Error -> {
                                hasScores = false
                                testResults = emptyList()
                            }
                        }
                    }
                }
            }

            // Display loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingSpinner()
                }
            }
            // Display scores if available
            else if (hasScores) {
                UserTestResults(GraphableAttempts(testResults))
            }
            // Display message if no scores are available
            else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No scores available for this patient",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Please select a patient to view their results",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}