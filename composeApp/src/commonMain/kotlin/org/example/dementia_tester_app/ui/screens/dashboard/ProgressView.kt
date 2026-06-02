package org.example.dementia_tester_app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import org.example.dementia_tester_app.auth.AuthService
import org.example.dementia_tester_app.data.*
import org.example.dementia_tester_app.ui.components.LoadingSpinner
import org.example.dementia_tester_app.ui.components.ProgressSummary
import org.example.dementia_tester_app.ui.components.UserTestResults
import org.example.dementia_tester_app.ui.components.FormColors

@Composable
fun ProgressView() {
    val tabs = listOf("Assessments", "Health Surveys", "Mini Games")
    var selectedTab by remember { mutableIntStateOf(0) }

    val authService = remember { AuthService() }
    val userId = authService.getCurrentUserId()
    
    val cognitiveService = remember { UserQuizService(UserQuizType.CognitiveAssessment) }
    val healthService = remember { UserQuizService(UserQuizType.HealthSurvey) }
    val gameService = remember { MiniGameScoresService() }

    var assessmentResults by remember { mutableStateOf<List<UserResults>>(emptyList()) }
    var healthResults by remember { mutableStateOf<List<UserResults>>(emptyList()) }
    var gameResults by remember { mutableStateOf<Map<GameType, List<GameAttempts>>>(emptyMap()) }
    
    val activityService = remember { ActivityService() }
    var recentActivities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId == null) return@LaunchedEffect
        isLoading = true
        
        // Concurrent-like fetching (using callbacks)
        cognitiveService.getUserScores(userId) { res ->
            if (res is DatabaseResult.Success) assessmentResults = res.data
            
            healthService.getUserScores(userId) { hRes ->
                if (hRes is DatabaseResult.Success) healthResults = hRes.data
                
                // Fetch Activities
                // We'll use a simpler way to get the flow data into our state for the dashboard
                // In a full implementation, we'd collect from activityService.getActivitiesFlow()
                
                // Fetch games
                val gamesMap = mutableMapOf<GameType, List<GameAttempts>>()
                var gamesLoaded = 0
                val totalGames = GameType.entries.size
                
                GameType.entries.forEach { type ->
                    gameService.getUserGameAttempts(userId, type) { gRes ->
                        if (gRes is DatabaseResult.Success) {
                            gamesMap[type] = gRes.data
                        }
                        gamesLoaded++
                        if (gamesLoaded == totalGames) {
                            gameResults = gamesMap
                            isLoading = false
                        }
                    }
                }
            }
        }
    }

    // Separate collection for Activities flow
    LaunchedEffect(userId) {
        if (userId == null) return@LaunchedEffect
        activityService.getActivitiesFlow().collect { activities ->
            recentActivities = activities.take(5) // Just show last 5
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Progress Analytics",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Statistic Cards Overview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard("Tests", assessmentResults.flatMap { it.attempts }.size.toString(), Modifier.weight(1f))
            StatCard("Surveys", healthResults.flatMap { it.attempts }.size.toString(), Modifier.weight(1f))
            StatCard("Games", gameResults.values.flatten().flatMap { it.attempts }.size.toString(), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Activities Preview
        if (recentActivities.isNotEmpty()) {
            Text("Recent Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            recentActivities.forEach { activity ->
                RecentActivityItem(activity)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = FormColors.green,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                LoadingSpinner()
            }
        } else {
            when (selectedTab) {
                0 -> AssessmentTab(assessmentResults)
                1 -> HealthSurveyTab(healthResults)
                2 -> GamesTab(gameResults)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun RecentActivityItem(activity: Activity) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(FormColors.green)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(activity.title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(activity.type.capitalize(), fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FormColors.green)
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AssessmentTab(results: List<UserResults>) {
    val attempts = results.flatMap { it.attempts }
    if (attempts.isEmpty()) {
        EmptyState("No assessments completed yet.")
    } else {
        ProgressSummary(attempts.last())
        Spacer(modifier = Modifier.height(16.dp))
        UserTestResults(GraphableAttempts(results))
    }
}

@Composable
fun HealthSurveyTab(results: List<UserResults>) {
    val attempts = results.flatMap { it.attempts }
    if (attempts.isEmpty()) {
        EmptyState("No health surveys completed yet.")
    } else {
        ProgressSummary(attempts.last())
        Spacer(modifier = Modifier.height(16.dp))
        UserTestResults(GraphableAttempts(results))
    }
}

@Composable
fun GamesTab(gameResults: Map<GameType, List<GameAttempts>>) {
    val allAttempts = gameResults.values.flatten()
    if (allAttempts.isEmpty()) {
        EmptyState("No games played yet.")
    } else {
        var selectedGame by remember { mutableStateOf(GameType.COMPLEX_ATTENTION) }
        
        ScrollableTabRow(
            selectedTabIndex = GameType.entries.indexOf(selectedGame),
            containerColor = Color.Transparent,
            edgePadding = 0.dp
        ) {
            GameType.entries.forEach { type ->
                Tab(
                    selected = selectedGame == type,
                    onClick = { selectedGame = type },
                    text = { Text(type.name.replace("_", " ").lowercase().capitalize()) }
                )
            }
        }
        
        val specificResults = gameResults[selectedGame] ?: emptyList()
        if (specificResults.isEmpty()) {
            EmptyState("No data for this game.")
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            UserTestResults(GraphableAttempts(specificResults))
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
        Text(message, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

fun String.capitalize() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
