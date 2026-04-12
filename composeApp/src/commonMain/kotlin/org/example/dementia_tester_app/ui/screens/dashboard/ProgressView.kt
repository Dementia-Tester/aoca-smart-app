package org.example.dementia_tester_app.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.dementia_tester_app.auth.AuthService
import org.example.dementia_tester_app.data.DatabaseResult
import org.example.dementia_tester_app.data.GameType
import org.example.dementia_tester_app.data.GraphableAttempts
import org.example.dementia_tester_app.data.MiniGameScoresService
import org.example.dementia_tester_app.data.UserGameResults
import org.example.dementia_tester_app.data.UserQuizService
import org.example.dementia_tester_app.data.UserQuizType
import org.example.dementia_tester_app.ui.components.LoadingSpinner
import org.example.dementia_tester_app.ui.components.UserTestResults

/**
 * Progress view for users. Displays information about the user's results from the
 * cognitive assessment test, health survey and minigames.
 */
@Composable
fun ProgressView() {

    var scoresExpanded by remember({ mutableStateOf(false) })
    var selectedScoreType by remember({ mutableStateOf("") })
    // "Language", "Perceptual-motor" and "Social Cognition" are excluded until their
    // corresponding minigames are implemented.
    val scoreTypes = listOf(
        "Total Scores",
        "Complex Attention",
        "Executive Function",
        "Learning and Memory",
        "Health Survey"
    )

    var placeholder  by remember({ mutableStateOf("Select Results to Display") })
    var isLoading by remember { mutableStateOf(false) }
    val cognitiveAssessmentService = remember { UserQuizService(UserQuizType.CognitiveAssessment) }
    val healthSurveyService = remember { UserQuizService(UserQuizType.HealthSurvey) }
    val miniGameScoresService = remember { MiniGameScoresService() }

    val authService = remember { AuthService() }
    val userId = authService.getCurrentUserId()

    var results by remember { mutableStateOf<List<Any>>(emptyList()) }
    var hasScores by remember { mutableStateOf(false) }
    var errorMessage by remember({ mutableStateOf("No data available") })
    val scrollState = rememberScrollState()

    // Helper function to handle the database result
    fun handleResult(result: DatabaseResult<List<Any>>) {
        isLoading = false
        when (result) {
            is DatabaseResult.Success -> {
                results = result.data
                hasScores = result.data.isNotEmpty()
            }
            is DatabaseResult.Error -> {
                hasScores = false
                results = emptyList()
                errorMessage = result.message
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)

    ) {
        Text(
            text = "Your Progress",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "View your progress",
            fontSize = 16.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Select Data:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)

        )

        // Button to open dropdown
        OutlinedButton(
            onClick = { scoresExpanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
            border = BorderStroke(2.dp, Color.Gray),
            shape = RoundedCornerShape(8.dp),

            ) {
            Text(
                text = placeholder,
                fontSize = 16.sp,
                color = Color.Black,
            )
        }

        // Dropdown menu
        DropdownMenu(
            expanded = scoresExpanded,
            onDismissRequest = { scoresExpanded = false },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight(0.7f)
                .background(Color.White)
        ) {
            scoreTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        selectedScoreType = type
                        scoresExpanded = false
                    }
                )
            }
        }

        if (selectedScoreType.isNotEmpty()) {
            // Fetch user scores when a user is selected
            LaunchedEffect(selectedScoreType) {
                placeholder = selectedScoreType
                userId?.let { userId ->
                    isLoading = true

                    when (selectedScoreType) {
                        "Total Scores" -> {
                            cognitiveAssessmentService.getUserScores(userId) { result ->
                                handleResult(result)
                            }
                        }
                        "Health Survey" -> {
                            healthSurveyService.getUserScores(userId) {result ->
                                handleResult(result)
                            }
                        }
                        "Complex Attention" -> {
                            miniGameScoresService.getUserGameAttempts(userId, GameType.COMPLEX_ATTENTION) { result ->
                                handleResult(result)
                            }
                        }
                        "Executive Function" -> {
                            miniGameScoresService.getUserGameAttempts(userId, GameType.EXECUTIVE_FUNCTION) { result ->
                                handleResult(result)
                            }
                        }
                        "Learning and Memory" -> {
                            miniGameScoresService.getUserGameAttempts(userId, GameType.LEARNING_AND_MEMORY) { result ->
                                handleResult(result)
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
                UserTestResults(GraphableAttempts(results))
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
                        text = errorMessage,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}