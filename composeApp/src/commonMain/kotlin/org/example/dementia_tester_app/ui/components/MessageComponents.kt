package org.example.dementia_tester_app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



/**
 * A reusable error message component for form validation
 *
 * @param show Whether to show the error message
 * @param message Custom error message
 */
@Composable
fun ErrorMessage(
    show: Boolean,
    message: String
) {
    if (show) {
        Text(
            text = message,
            color = FormColors.errorColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp)
        )
    }
}

/**
 * A reusable success message component with animation and styling
 *
 * @param message The success message to display
 * @param isVisible Whether the success message should be visible
 * @param modifier Additional modifier for the component
 */
@Composable
fun SuccessMessage(
    message: String,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 }, // Start from middle
            animationSpec = tween(durationMillis = 600, easing = EaseOutQuad)
        ) + fadeIn(
            animationSpec = tween(durationMillis = 600)
        ),
        exit = fadeOut(
            animationSpec = tween(durationMillis = 600)
        )
    ) {
        Card(
            modifier = modifier
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(5.dp))
                .border(
                    width = 2.5.dp,
                    color = FormColors.green.copy(alpha = 0.8f), // Dark green color
                    shape = RoundedCornerShape(8.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = FormColors.green.copy(alpha = 0.08f)
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Small success icon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FormColors.green)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Success message text
                Text(
                    text = message,
                    color = FormColors.green,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}