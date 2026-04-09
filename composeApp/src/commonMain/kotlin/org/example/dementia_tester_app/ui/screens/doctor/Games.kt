package org.example.dementia_tester_app.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
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
import org.example.dementia_tester_app.data.DatabaseResult
import org.example.dementia_tester_app.data.GameResult
import org.example.dementia_tester_app.data.MiniGameScoresService
import org.example.dementia_tester_app.data.UserGameResults
import org.example.dementia_tester_app.data.UserProfile
import org.example.dementia_tester_app.ui.components.PatientSelect

/**
 * Games screen for doctors
 * Allows doctors to select a user and view their game scores
 * 
 * @param userList List of users in the format "Users name - Users email"
 * @param userMap Mapping between formatted user strings and UserProfile objects
 */
@Composable
fun DoctorGamesScreen(
    userList: List<String>,
    userMap: Map<String, UserProfile>
) {
    // Create an instance of MiniGameScoresService
    val miniGameScoresService = remember { MiniGameScoresService() }
    
    // State for selected user
    var selectedUserString by remember { mutableStateOf<String?>(null) }
    var selectedUser by remember { mutableStateOf<UserProfile?>(null) }
    
    // State for game results
    var userGameResults by remember { mutableStateOf<UserGameResults?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch game results when user is selected
    LaunchedEffect(selectedUser) {
        selectedUser?.userId?.let { userId ->
            isLoading = true
            errorMessage = null
            userGameResults = null
            
            miniGameScoresService.getUserGameScores(userId) { result ->
                isLoading = false
                when (result) {
                    is DatabaseResult.Success -> {
                        userGameResults = result.data
                    }
                    is DatabaseResult.Error -> {
                        errorMessage = result.message
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Patient Game Scores",
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
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Display selected user's game scores if a user is selected
        if (selectedUser != null) {
            // Game scores
            Text(
                text = "Game Scores",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
            )

            when {
                isLoading -> {
                    // Show loading indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingSpinner()
                    }
                }
                errorMessage != null -> {
                    // Show error message
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error loading game scores: $errorMessage",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Red
                        )
                    }
                }
                userGameResults != null -> {
                    // Show game scores
                    LazyColumn {
                        // Focus Flicker
                        item {
                            val focusFlicker = userGameResults?.focusFlicker
                            if (focusFlicker != null) {
                                GameResultsCard(focusFlicker)
                            } else {
                                NoScoresCard("Focus Flicker - Complex Attention")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Task Switcher
                        item {
                            val taskSwitcher = userGameResults?.taskSwitcher
                            if (taskSwitcher != null) {
                                GameResultsCard(taskSwitcher)
                            } else {
                                NoScoresCard("Task Switcher - Executive Function")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Word Recall
                        item {
                            val wordRecall = userGameResults?.wordRecall
                            if (wordRecall != null) {
                                GameResultsCard(wordRecall)
                            } else {
                                NoScoresCard("Word Recall - Learning and Memory")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                else -> {
                    // No game results
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No game scores available for this patient",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Prompt to select a user
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Please select a patient to view their game scores",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


/**
 * Card to display a game results from the database
 */
@Composable
private fun GameResultsCard(gameResults: GameResult) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Green header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF66BB23))
                    .padding(8.dp)
            ) {
                Text(
                    text = gameResults.gameName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            // Card content
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Game statistics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Total Attempts:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Last Played:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Average Score:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Minimum Score:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Maximum Score:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${gameResults.totalAttempts}",
                            fontSize = 14.sp
                        )
                        Text(
                            text = gameResults.lastPlayed,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "${gameResults.averageScore}",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "${gameResults.minScore}",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "${gameResults.maxScore}",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card to display when a game has no scores
 */
@Composable
private fun NoScoresCard(gameName: String) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Green header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF66BB23))
                    .padding(8.dp)
            ) {
                Text(
                    text = gameName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            // Card content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No scores available for this game",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}