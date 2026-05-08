package org.example.dementia_tester_app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.dementia_tester_app.data.UserAttempts
import org.example.dementia_tester_app.data.UserQuizType
import org.example.dementia_tester_app.ui.components.FormColors

@Composable
fun ProgressSummary(latestAttempt: UserAttempts) {
    val totalMaxScore = latestAttempt.totalQuestions * 4
    val totalPercentage = if (totalMaxScore > 0) {
        (latestAttempt.totalScore.toFloat() / totalMaxScore.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = if (latestAttempt.type == UserQuizType.CognitiveAssessment) "Assessment Summary" else "Survey Summary",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Overall progress bar
            CircularProgressSection(
                percentage = totalPercentage,
                label = "Total Score: ${latestAttempt.totalScore} / $totalMaxScore"
            )

            if (latestAttempt.type == UserQuizType.CognitiveAssessment) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Domain Breakdown",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Breakdown by domain
                DomainProgressBar(
                    label = "Complex Attention",
                    score = latestAttempt.domainScores.complexAttentions,
                    maxScore = 20
                )
                DomainProgressBar(
                    label = "Executive Function",
                    score = latestAttempt.domainScores.executiveFunction,
                    maxScore = 20
                )
                DomainProgressBar(
                    label = "Language",
                    score = latestAttempt.domainScores.language,
                    maxScore = 20
                )
                DomainProgressBar(
                    label = "Learning & Memory",
                    score = latestAttempt.domainScores.learningAndMemory,
                    maxScore = 20
                )
                DomainProgressBar(
                    label = "Perceptual Motor",
                    score = latestAttempt.domainScores.perceptualMotor,
                    maxScore = 20
                )
                DomainProgressBar(
                    label = "Social Cognition",
                    score = latestAttempt.domainScores.socialCognition,
                    maxScore = 20
                )
            }
        }
    }
}

@Composable
fun CircularProgressSection(percentage: Float, label: String) {
    val animatedProgress by animateFloatAsState(targetValue = percentage)
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 8.dp,
                color = FormColors.green,
                trackColor = FormColors.green.copy(alpha = 0.2f),
            )
            Text(
                text = "${(percentage * 100).toInt()}%",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = FormColors.green
            )
        }
        
        Spacer(modifier = Modifier.width(24.dp))
        
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DomainProgressBar(label: String, score: Int, maxScore: Int) {
    val percentage = if (maxScore > 0) (score.toFloat() / maxScore.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = percentage)

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 14.sp)
            Text(text = "$score / $maxScore (${(percentage * 100).toInt()}%)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = FormColors.green,
            trackColor = FormColors.green.copy(alpha = 0.2f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
