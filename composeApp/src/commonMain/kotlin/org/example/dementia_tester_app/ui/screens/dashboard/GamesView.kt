package org.example.dementia_tester_app.ui.screens.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.dementia_tester_app.Minigame

val games = arrayOf(
    Minigame("FocusFlick", "Focus Flicker - Complex Attention", "Tap on the red circle to increase your score"),
    Minigame("TaskSwitch", "Task Switcher - Executive Function", "Quickly match shapes or colours"),
    Minigame("WordRecall", "Word Recall - Learning and Memory", "Recall the words shown on screen"),
)

@Composable
fun GamesView(gamecallback: (String) -> Unit) {
    var currentgame by remember { mutableStateOf("") }
    Column(){
        Row(){
            Text(
                text = "Memory Games",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(){
            Text(
                text = "Train your brain with memory games.",
                fontSize = 16.sp
            )
        }
        for(game in games){
            Row(modifier = Modifier.padding(8.dp)){
                game.GetCard({currentgame = game.minigameName})
            }
        }
        gamecallback(currentgame)
    }
}