package org.example.dementia_tester_app.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.dementia_tester_app.auth.AuthService
import org.example.dementia_tester_app.data.DatabaseResult
import org.example.dementia_tester_app.data.GameType
import org.example.dementia_tester_app.data.GraphableAttempts
import org.example.dementia_tester_app.data.MiniGameScoresService
import org.example.dementia_tester_app.data.UserAttempts
import org.example.dementia_tester_app.data.UserQuizService
import org.example.dementia_tester_app.data.UserQuizType
import org.example.dementia_tester_app.ui.components.LoadingSpinner
import org.example.dementia_tester_app.ui.components.ProgressSummary
import org.example.dementia_tester_app.ui.components.UserTestResults
import org.example.dementia_tester_app.ui.components.FormColors

@Composable
fun ProgressView() {
    val authService = remember { AuthService() }
    val userId = authService.getCurrentUserId()

    val cognitiveAssessmentService = remember { UserQuizService(UserQuizType.CognitiveAssessment) }
    val healthSurveyService = remember { UserQuizService(UserQuizType.HealthSurvey) }
    val miniGameScoresService = remember { MiniGameScoresService() }

    var assessmentResults by remember { mutableStateOf<List<Any>>(emptyList()) }
    var surveyResults by remember { mutableStateOf<List<Any>>(emptyList()) }
    var gameResultsCA by remember { mutableStateOf<List<Any>>(emptyList()) }
    var gameResultsEF by remember { mutableStateOf<List<Any>>(emptyList()) }
    var gameResultsLM by remember { mutableStateOf<List<Any>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    
    // 0: Assessments, 1: Health Surveys, 2: Games
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedGameTab by remember { mutableStateOf(0) }
    
    val scrollState = rememberScrollState()

    LaunchedEffect(userId) {
        if (userId == null) {
            isLoading = false
            loadError = "User not logged in."
            return@LaunchedEffect
        }
        
        // Fetch all data
        var fetchedCount = 0
        val totalFetches = 5
        
        fun checkDone() {
            fetchedCount++
            if (fetchedCount >= totalFetches) isLoading = false
        }
        
        cognitiveAssessmentService.getUserScores(userId) { result ->
            if (result is DatabaseResult.Success) assessmentResults = result.data
            checkDone()
        }
        healthSurveyService.getUserScores(userId) { result ->
            if (result is DatabaseResult.Success) surveyResults = result.data
            checkDone()
        }
        miniGameScoresService.getUserGameAttempts(userId, GameType.COMPLEX_ATTENTION) { result ->
            if (result is DatabaseResult.Success) gameResultsCA = result.data
            checkDone()
        }
        miniGameScoresService.getUserGameAttempts(userId, GameType.EXECUTIVE_FUNCTION) { result ->
            if (result is DatabaseResult.Success) gameResultsEF = result.data
            checkDone()
        }
        miniGameScoresService.getUserGameAttempts(userId, GameType.LEARNING_AND_MEMORY) { result ->
            if (result is DatabaseResult.Success) gameResultsLM = result.data
            checkDone()
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
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Track your cognitive health and activity engagement over time.",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isLoading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                LoadingSpinner()
            }
        } else if (loadError != null) {
            Text(loadError!!, color = MaterialTheme.colorScheme.error)
        } else {
            // Overview Statistic Cards
            val totalAssessments = assessmentResults.size
            val totalSurveys = surveyResults.size
            val totalGames = gameResultsCA.size + gameResultsEF.size + gameResultsLM.size
            
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatisticCard("Assessments", totalAssessments.toString(), Modifier.weight(1f))
                StatisticCard("Surveys", totalSurveys.toString(), Modifier.weight(1f))
                StatisticCard("Games Played", totalGames.toString(), Modifier.weight(1f))
            }
            
            // Latest Assessment Summary Card if available
            val latestAssessment = assessmentResults.filterIsInstance<UserAttempts>().lastOrNull()
            if (latestAssessment != null) {
                ProgressSummary(latestAssessment)
                Spacer(Modifier.height(16.dp))
            }

            // Tabs for Charts
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = FormColors.green
            ) {
                Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }) {
                    Text("Assessments", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurface)
                }
                Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) {
                    Text("Health Surveys", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurface)
                }
                Tab(selected = selectedTabIndex == 2, onClick = { selectedTabIndex = 2 }) {
                    Text("Mini Games", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurface)
                }
            }
            
            Spacer(Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> {
                    if (assessmentResults.isNotEmpty()) {
                        UserTestResults(GraphableAttempts(assessmentResults))
                    } else {
                        NoDataMessage()
                    }
                }
                1 -> {
                    if (surveyResults.isNotEmpty()) {
                        UserTestResults(GraphableAttempts(surveyResults))
                    } else {
                        NoDataMessage()
                    }
                }
                2 -> {
                    // Game Sub-tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedGameTab,
                        containerColor = Color.Transparent,
                        edgePadding = 0.dp
                    ) {
                        Tab(selected = selectedGameTab == 0, onClick = { selectedGameTab = 0 }) {
                            Text("Complex Attention", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurface)
                        }
                        Tab(selected = selectedGameTab == 1, onClick = { selectedGameTab = 1 }) {
                            Text("Executive Function", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurface)
                        }
                        Tab(selected = selectedGameTab == 2, onClick = { selectedGameTab = 2 }) {
                            Text("Learning & Memory", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    
                    val currentGames = when(selectedGameTab) {
                        0 -> gameResultsCA
                        1 -> gameResultsEF
                        else -> gameResultsLM
                    }
                    
                    if (currentGames.isNotEmpty()) {
                        UserTestResults(GraphableAttempts(currentGames))
                    } else {
                        NoDataMessage()
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatisticCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = FormColors.green)
            Spacer(Modifier.height(4.dp))
            Text(title, fontSize = 12.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun NoDataMessage() {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text("No data available yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

