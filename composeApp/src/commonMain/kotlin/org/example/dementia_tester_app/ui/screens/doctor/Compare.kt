package org.example.dementia_tester_app.ui.screens.doctor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
 * Compare screen for doctors
 * Allows doctors to select two users and compare their results
 * 
 * @param userList List of users in the format "Users name - Users email"
 * @param userMap Mapping between formatted user strings and UserProfile objects
 */
@Composable
fun DoctorCompareScreen(
    userList: List<String>,
    userMap: Map<String, UserProfile>
) {
    // Create an instance of UserQuizService for Cognitive Assessment
    val cognitiveAssessmentService = remember { UserQuizService(UserQuizType.CognitiveAssessment) }

    // State for selected users
    var selectedUserString1 by remember { mutableStateOf<String?>(null) }
    var selectedUserString2 by remember { mutableStateOf<String?>(null) }
    var selectedUser1 by remember { mutableStateOf<UserProfile?>(null) }
    var selectedUser2 by remember { mutableStateOf<UserProfile?>(null) }

    // State for test results
    var hasScores1 by remember { mutableStateOf(false) }
    var hasScores2 by remember { mutableStateOf(false) }
    var isLoading1 by remember { mutableStateOf(false) }
    var isLoading2 by remember { mutableStateOf(false) }
    var testResults1 by remember { mutableStateOf<List<UserResults>>(emptyList()) }
    var testResults2 by remember { mutableStateOf<List<UserResults>>(emptyList()) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Title
        Text(
            text = "Compare Patient Results",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // User selection dropdowns
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // First user dropdown
            PatientSelect(
                userList = userList,
                userMap = userMap,
                selectedUserString = selectedUserString1,
                placeholder = "Select first patient",
                onUserSelected = { string, profile ->
                    selectedUserString1 = string
                    selectedUser1 = profile
                    hasScores1 = false
                    isLoading1 = false
                    testResults1 = emptyList()
                }
            )

            // Second user dropdown
            PatientSelect(
                userList = userList,
                userMap = userMap,
                selectedUserString = selectedUserString2,
                placeholder = "Select second patient",
                onUserSelected = { string, profile ->
                    selectedUserString2 = string
                    selectedUser2 = profile
                    hasScores2 = false
                    isLoading2 = false
                    testResults2 = emptyList()
                }
            )
        }

        // Display comparison if both users are selected
        if (selectedUser1 != null && selectedUser2 != null) {
            // Fetch user scores when users are selected
            LaunchedEffect(selectedUser1) {
                selectedUser1?.userId?.let { userId ->
                    isLoading1 = true

                    cognitiveAssessmentService.getUserScores(userId) { result ->
                        isLoading1 = false
                        when (result) {
                            is DatabaseResult.Success -> {
                                testResults1 = result.data
                                hasScores1 = result.data.isNotEmpty()
                            }
                            is DatabaseResult.Error -> {
                                hasScores1 = false
                                testResults1 = emptyList()
                            }
                        }
                    }
                }
            }

            LaunchedEffect(selectedUser2) {
                selectedUser2?.userId?.let { userId ->
                    isLoading2 = true

                    cognitiveAssessmentService.getUserScores(userId) { result ->
                        isLoading2 = false
                        when (result) {
                            is DatabaseResult.Success -> {
                                testResults2 = result.data
                                hasScores2 = result.data.isNotEmpty()
                            }
                            is DatabaseResult.Error -> {
                                hasScores2 = false
                                testResults2 = emptyList()
                            }
                        }
                    }
                }
            }


            // Display loading indicators if either user's data is loading
            if (isLoading1 || isLoading2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingSpinner()
                }
            }
            // Display comparative results if both users have scores
            else if (hasScores1 && hasScores2) {
                UserTestResults(
                    results = GraphableAttempts(testResults1),
                    results2 = testResults2,
                    user1Name = selectedUser1!!.name,
                    user2Name = selectedUser2!!.name
                )
            }
            // Display message if either user has no scores
            else if (!isLoading1 && !isLoading2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (!hasScores1 && !hasScores2) {
                            "No scores available for either patient"
                        } else if (!hasScores1) {
                            "No scores available for ${selectedUser1?.name}"
                        } else {
                            "No scores available for ${selectedUser2?.name}"
                        },
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Prompt to select both users
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Please select two patients to compare their results",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
