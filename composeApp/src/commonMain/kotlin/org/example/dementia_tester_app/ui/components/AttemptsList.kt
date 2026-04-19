package org.example.dementia_tester_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.dementia_tester_app.data.AttemptSummary

/**
 * A screen that displays a list of previous survey attempts
 */
@Composable
fun AttemptsListScreen(
    attempts: List<AttemptSummary>,
    onBackToDashboard: () -> Unit,
    onStartNewAttempt: () -> Unit,
    onViewAttempt: (Int) -> Unit,
    onContinueAttempt: (Int) -> Unit
) {
    Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing
                .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            containerColor = Color(0xFFF5F5F5),
            bottomBar = {
                Surface(color = Color(0xFFF5F5F5), tonalElevation = 3.dp) {
                    Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackToDashboard,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FormColors.green)
                    ) { Text("Back") }

                    Button(
                        onClick = onStartNewAttempt,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FormColors.green,
                            contentColor = Color.White
                        )
                    ) { Text("Start New Attempt") }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Previous Attempts:",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (attempts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No previous attempts found", color = Color.Gray)
                    }
                }
            } else {
                items(attempts, key = { it.attemptNumber }) { attempt ->
                    AttemptItemCard(
                        attempt = attempt,
                        onViewClicked = { onViewAttempt(attempt.attemptNumber) },
                        onContinueClicked = { onContinueAttempt(attempt.attemptNumber) }
                    )
                }
            }
        }
    }
}


/**
 * A card that displays information about a single survey attempt
 */
@Composable
private fun AttemptItemCard(
    attempt: AttemptSummary,
    onViewClicked: () -> Unit,
    onContinueClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Attempt number and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Attempt ${attempt.attemptNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = attempt.timestamp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status and progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (attempt.surveyComplete) {
                    Column {
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FormColors.green,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Score: ${attempt.totalScore} / ${attempt.totalQuestions * 4}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Column {
                        Text(
                            text = "Incomplete",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Progress: ${attempt.getCompletionText()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Action buttons
                if (attempt.surveyComplete) {
                    // For completed attempts, show only View button
                    Button(
                        onClick = onViewClicked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FormColors.green,
                            contentColor = Color.White
                        )
                    ) {
                        Text("View")
                    }
                } else {
                    // For incomplete attempts, show both View and Continue buttons
                    Row {
                        OutlinedButton(
                            onClick = onViewClicked,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("View")
                        }
                        
                        Button(
                            onClick = onContinueClicked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FormColors.green,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Continue")
                        }
                    }
                }
            }
            
            // Progress indicator for incomplete attempts
            if (!attempt.surveyComplete) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { attempt.getCompletionPercentage() / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = FormColors.green,
                    trackColor = Color.LightGray
                )
            }
        }
    }
}