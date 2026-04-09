package org.example.dementia_tester_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.dementia_tester_app.data.Question

/**
 * Summary view component to display all questions and answers (Reusable for any quiz)
 */
@Composable
fun QuizSummary(
    questions: List<Question>,
    onBackToDashboard: () -> Unit
) {
    val totalScore = questions.sumOf { it.score }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenHeight = maxHeight
        val contentHeight = screenHeight - 72.dp // Reserve less space for the button

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(contentHeight)
                    .padding(8.dp)
                    .padding(bottom = 56.dp) // Smaller bottom padding to reduce the gap
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Attempt Summary",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Total Score: $totalScore",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = FormColors.green
                )

                Spacer(modifier = Modifier.height(16.dp))

                questions.forEach { question ->
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
                                text = question.domain,
                                style = MaterialTheme.typography.bodySmall,
                                color = FormColors.green
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = question.questionText,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (question.allowMultipleAnswers) {
                                Column {
                                    Text(
                                        text = "Your answers:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (question.selectedAnswers.isEmpty()) {
                                        Text(
                                            text = "Not answered",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    } else {
                                        question.selectedAnswers.forEach { answer ->
                                            Text(
                                                text = "• $answer",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Score: ${question.score}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            } else {
                                Column {
                                    Row {
                                        Text(
                                            text = "Your answer: ",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = question.selectedAnswer ?: "Not answered",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Score: ${question.score}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Fixed button at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Button(
                    onClick = onBackToDashboard,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FormColors.green,
                        contentColor = Color.White
                    )
                ) {
                    Text("Finish")
                }
            }
        }
    }
}
