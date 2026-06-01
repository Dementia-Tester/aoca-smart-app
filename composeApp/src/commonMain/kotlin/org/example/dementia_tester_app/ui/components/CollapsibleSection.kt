package org.example.dementia_tester_app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CollapsibleSection(
    title: String,
    content: @Composable () -> Unit,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onHeaderClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF66BB45)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color.White,
                    modifier = Modifier.rotate(rotationState)
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    CompositionLocalProvider(
                        LocalTextStyle provides LocalTextStyle.current.copy(
                            fontSize = 18.sp
                        )
                    ) {
                        content()
                    }
                }
            }
        }
    }
}