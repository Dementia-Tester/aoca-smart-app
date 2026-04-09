package org.example.dementia_tester_app.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.dementia_tester_app.auth.AuthService
import org.example.dementia_tester_app.data.*
import org.example.dementia_tester_app.data.UserQuizType.CognitiveAssessment
import org.example.dementia_tester_app.ui.components.AttemptsListScreen
import org.example.dementia_tester_app.ui.components.LoadingSpinner
import org.example.dementia_tester_app.ui.components.QuestionComponent
import org.example.dementia_tester_app.ui.components.QuizSummary
import org.example.dementia_tester_app.data.Question
import org.example.dementia_tester_app.ui.components.FormColors


/**
 * Introduction screen for the cognitive assessment/test page.
 */
@Composable
fun CognitiveIntroductionScreen(
    onBackToDashboard: () -> Unit,
    onStartQuestions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "About this cognitive assessment",
                    style = MaterialTheme.typography.titleLarge,
                    color = FormColors.green,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This cognitive assessment is designed to assess and track various aspects of your mental health.",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "The assessment consists of 24 questions, assessing the following psychiatric domains:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bullet points as a column for better alignment
                val domains = listOf(
                    "Anger",
                    "Anxiety",
                    "Depression",
                    "Dissociation",
                    "Mania",
                    "Memory",
                    "Personality functioning",
                    "Psychosis",
                    "Repetitive thoughts and behaviours",
                    "Sleep problems",
                    "Somatic symptoms",
                    "Substance use",
                    "Suicidal ideation"
                )

                Column(modifier = Modifier.padding(start = 8.dp)) {
                    domains.forEach { domain ->
                        Text(
                            text = "• $domain",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your responses will help us provide personalized recommendations for maintaining and improving your cognitive health.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "All information provided is confidential and will only be used to support your health journey.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onBackToDashboard,
                modifier = Modifier
                    .height(48.dp)
                    .width(120.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray,
                    contentColor = Color.Black
                )
            ) {
                Text("Back")
            }

            Button(
                onClick = onStartQuestions,
                modifier = Modifier
                    .height(48.dp)
                    .width(120.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FormColors.green,
                    contentColor = Color.White
                )
            ) {
                Text("Start test")
            }
        }
    }
}

/**
 * Cognitive assessment page with question cards.
 */
@Composable
fun QuestionsPage(
    userId: String,
    attemptNumber: Int,
    onSaveAndExit: (Map<String, List<String>>) -> Unit,
    onFinish: (Map<String, List<String>>) -> Unit,
    prefilledAnswers: Map<String, List<String>> = emptyMap(),
    startQuestionIndex: Int = 0,
    readOnly: Boolean = false
) {
    // Cognitive question services:
    val userQuizService = remember { UserQuizService(CognitiveAssessment) }

    // Cognitive questions state:
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentQuestionIndex by remember { mutableStateOf(startQuestionIndex) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // User attempts/answers storage:
    // Local storage for answers:
    val answersMap = remember { mutableStateMapOf<String, List<String>>() }
    // Prefill previous answers:
    prefilledAnswers.forEach { (questionId, answerIds) ->
        answersMap[questionId] = answerIds
    }

    // Load/fetch questions when page is shown:
    LaunchedEffect(attemptNumber, userId) {
        userQuizService.getAttemptDetails(userId, attemptNumber) { result ->
            when (result) {
                // Get Questions (or not):
                is DatabaseResult.Success ->  questions = result.data
                is DatabaseResult.Error ->  errorMessage = result.message
            }
        }
    }

    // Show Loading, if required:
    if (errorMessage == null && questions.isEmpty()) {
        LoadingSpinner()
        return
    }
    // Show error message if there is one:
    errorMessage?.let {
        Text(
            text = it,
            color = Color.Red,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
    // If there are no questions:
    if (questions.isEmpty()) return

    // Current question index:
    val currentQuestion = questions.getOrNull(currentQuestionIndex) ?: return

    // Cognitive questions page setup:
    Column() {
        // Question Card: QuestionComponent wrapped in a card:
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            QuestionComponent(
                question = currentQuestion,
                questionNumber = currentQuestionIndex + 1,
                totalQuestions = questions.size,
                onAnswerSelected = { answer ->
                    // Update in-memory answer map and question list immutably
                    answersMap[currentQuestion.id.toString()] = listOf(answer)
                    val updated = questions.toMutableList()
                    updated[currentQuestionIndex] = currentQuestion.copy(selectedAnswer = answer)
                    questions = updated
                },
                onBackClicked = {
                    if (currentQuestionIndex > 0) {
                        currentQuestionIndex--
                    }
                },
                onSaveAndExitClicked = {
                    onSaveAndExit(answersMap)
                },
                onNextClicked = {
                    val q = questions[currentQuestionIndex]
                    if (!readOnly) {
                        userQuizService.saveUserAnswer(
                            userId = userId,
                            attemptNumber = attemptNumber,
                            question = q
                        ) { res ->
                            when (res) {
                                is DatabaseResult.Success -> {
                                    if (currentQuestionIndex < questions.lastIndex) {
                                        currentQuestionIndex++
                                    } else {
                                        onFinish(answersMap)
                                    }
                                }
                                is DatabaseResult.Error -> {
                                    println("CognitiveAssessment: Failed to save answer: ${res.message}")
                                }
                            }
                        }
                    } else {
                        if (currentQuestionIndex < questions.lastIndex) {
                            currentQuestionIndex++
                        } else {
                            onFinish(answersMap)
                        }
                    }
                }
            )
        }
    }

}

/**
 * TestView screen to display cognitive assessment introduction, assessment questions/test, and attempt list.
 */
@Composable
fun TestView() {
    // Cognitive question authorisation services:
    val authService: AuthService = remember { AuthService() }
    val userQuizService = remember { UserQuizService(CognitiveAssessment) }
    val userId = authService.getCurrentUserId()

    // Local state to control which view is shown within TestView page:
    var showQuestionsPage by remember { mutableStateOf(false) }
    var questionReadOnly by remember { mutableStateOf(false) }
    var answersMap by remember { mutableStateOf(mapOf<String, List<String>>()) }
    var attemptSummaries by remember { mutableStateOf<List<AttemptSummary>>(emptyList()) }
    var currentAttemptNumber by remember { mutableStateOf<Int?>(null) }
    var inProgressAttemptNumber by remember { mutableStateOf<Int?>(null) }
    var startQuestionIndex by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showSummary by remember { mutableStateOf(false) }
    var showCognitiveIntroduction by remember { mutableStateOf(false) }
    var summaryQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }

    // Load data for latest attempts:
    LaunchedEffect(userId) {
        if (userId != null) {
            userQuizService.getLatestAttemptNumber(userId) { result ->
                when (result) {
                    is DatabaseResult.Success -> {
                        currentAttemptNumber = result.data
                    }

                    is DatabaseResult.Error -> {
                        currentAttemptNumber = 1
                        errorMessage = "Failed to fetch latest attempt: ${result.message}"
                    }
                }
            }
            // Load attempt summaries (like Health Survey):
            userQuizService.getUserAttempts(userId) { result ->
                when (result) {
                    is DatabaseResult.Success -> {
                        attemptSummaries = result.data
                        isLoading = false

                        // If there are no previous attempts, load introduction page:
                        if (result.data.isEmpty()) {
                            showCognitiveIntroduction = true
                            showQuestionsPage = false
                        }
                    }
                    is DatabaseResult.Error -> {
                        errorMessage = "Failed to fetch attempts: ${result.message}"
                        isLoading = false
                    }
                }
            }
        }
        else { errorMessage = "User not logged in" }
    }

    // Load Questions Page (Cognitive Test) page:
    if (showQuestionsPage && userId != null) {
        val attemptToLoad = inProgressAttemptNumber ?: currentAttemptNumber
        if (attemptToLoad != null) {
            Column() {
                // Title above the card:
                Text(
                    text = "Cognitive Assessment",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                // Card-like component with question:
                QuestionsPage(
                    userId = userId,
                    attemptNumber = attemptToLoad,
                    onSaveAndExit = { savedAnswers ->
                        // Save progress and return to TestView:
                        answersMap = savedAnswers
                        if (!questionReadOnly) {
                            userQuizService.markAttemptIncomplete(userId, attemptToLoad) { _ ->
                                // Ignore result; refresh attempts list regardless:
                                userQuizService.getUserAttempts(userId) { res ->
                                    if (res is DatabaseResult.Success) attemptSummaries = res.data
                                    inProgressAttemptNumber = attemptToLoad
                                    showQuestionsPage = false
                                }
                            }
                        } else {
                            inProgressAttemptNumber = null
                            showQuestionsPage = false
                        }
                    },
                    onFinish = { savedAnswers ->
                        // Finalise attempt and then show summary for this attempt:
                        answersMap = savedAnswers
                        if (!questionReadOnly) {
                            userQuizService.finalizeAttempt(userId, attemptToLoad) { res ->
                                when (res) {
                                    is DatabaseResult.Success -> {
                                        inProgressAttemptNumber = null
                                        currentAttemptNumber = res.data // next attempt number
                                        showQuestionsPage = false
                                        // Load the completed attempt's details to show in summary:
                                        userQuizService.getAttemptDetails(userId, attemptToLoad) { det ->
                                            when (det) {
                                                is DatabaseResult.Success -> {
                                                    summaryQuestions = det.data
                                                    showSummary = true
                                                }
                                                is DatabaseResult.Error -> {
                                                    // If failed to load details, just fall back to attempts list
                                                    showSummary = false
                                                }
                                            }
                                            // Refresh attempts regardless:
                                            userQuizService.getUserAttempts(userId) { r ->
                                                if (r is DatabaseResult.Success) attemptSummaries = r.data
                                            }
                                        }
                                    }
                                    is DatabaseResult.Error -> {
                                        // If finalize fails, still close page but keep attempt number:
                                        inProgressAttemptNumber = null
                                        showQuestionsPage = false
                                    }
                                }
                            }
                        } else {
                            inProgressAttemptNumber = null
                            showQuestionsPage = false
                        }
                    },
                    startQuestionIndex = startQuestionIndex,
                    prefilledAnswers = answersMap,
                    readOnly = questionReadOnly
                )
            }
            return
        }
    }

    // Display the main content of TestView:
    if (isLoading || currentAttemptNumber == null) {
        LoadingSpinner()
    }
    // Show introduction page:
    else if (showCognitiveIntroduction) {
        CognitiveIntroductionScreen(
            onBackToDashboard = { }, //No-op for now
            onStartQuestions = {
                // Start new attempt/test:
                questionReadOnly = false
                startQuestionIndex = 0
                answersMap = emptyMap()
                val newAttempt = (attemptSummaries.maxOfOrNull { it.attemptNumber } ?: 0) + 1
                inProgressAttemptNumber = newAttempt
                // Switch screens:
                showCognitiveIntroduction = false
                showQuestionsPage = true
            }
        )
    }
    else if (showSummary) {
        QuizSummary(
            questions = summaryQuestions,
            onBackToDashboard = {
                showSummary = false
                // Refresh attempts when closing summary:
                userId?.let { uid ->
                    userQuizService.getUserAttempts(uid) { r ->
                        if (r is DatabaseResult.Success) attemptSummaries = r.data
                    }
                }
            }
        )
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Your Assessments",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
            Text(
                text = "View your assessment results or start a new assessment.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            // Show error if present:
            errorMessage?.let { msg ->
                org.example.dementia_tester_app.ui.components.ErrorMessage(
                    show = true,
                    message = msg
                )
            }
            // Show previous test attempts:
            AttemptsListScreen(
                attempts = attemptSummaries,
                onBackToDashboard = {
                    // Return to cognitive introduction screen (until dashboard overview/homepage is created):
                    showCognitiveIntroduction = true
                },
                onStartNewAttempt = {
                    questionReadOnly = false
                    startQuestionIndex = 0
                    answersMap = emptyMap()
                    val newAttempt = (attemptSummaries.maxOfOrNull { it.attemptNumber } ?: 0) + 1
                    inProgressAttemptNumber = newAttempt
                    showQuestionsPage = true
                },
                onViewAttempt = { attemptNum ->
                    if (userId != null) {
                        userQuizService.getAttemptDetails(userId, attemptNum) { res ->
                            when (res) {
                                is DatabaseResult.Success -> {
                                    // Load the selected attempt details into summary view
                                    summaryQuestions = res.data
                                    showSummary = true
                                    // Ensure we are not on the QuestionsPage
                                    showQuestionsPage = false
                                }
                                is DatabaseResult.Error -> {
                                    errorMessage = "Failed to load attempt: ${res.message}"
                                }
                            }
                        }
                    }
                },
                onContinueAttempt = { attemptNum ->
                    if (userId != null) {
                        userQuizService.getAttemptDetails(userId, attemptNum) { res ->
                            when (res) {
                                is DatabaseResult.Success -> {
                                    val list = res.data
                                    val map = list.associate { q -> q.id.toString() to (q.selectedAnswer?.let { listOf(it) } ?: emptyList()) }
                                    answersMap = map
                                    userQuizService.getNextUnansweredQuestionIndex(
                                        userId,
                                        attemptNum,
                                        list.size
                                    ) { idxRes ->
                                        val idx = when (idxRes) {
                                            is DatabaseResult.Success -> idxRes.data
                                            is DatabaseResult.Error -> 0
                                        }
                                        startQuestionIndex = idx
                                        inProgressAttemptNumber = attemptNum
                                        questionReadOnly = false
                                        showQuestionsPage = true
                                    }
                                }
                                is DatabaseResult.Error -> {
                                    errorMessage = "Failed to continue attempt: ${res.message}"
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}
