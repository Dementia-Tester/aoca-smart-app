package org.example.dementia_tester_app.ui.screens

import org.example.dementia_tester_app.data.MiniGameScoresService
import org.example.dementia_tester_app.data.UserProfileService
import org.example.dementia_tester_app.data.GameType
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import org.example.dementia_tester_app.auth.AuthService
import kotlin.math.max
import kotlin.random.Random



@Composable
fun FocusFlick(onReturn: () -> Unit){
    var showbox by remember{mutableStateOf(false)}
    var timeleft by remember{mutableStateOf(30)}
    var score by remember{mutableStateOf(0)}
    var x by remember{mutableStateOf(0.5)}
    var y by remember{mutableStateOf(0.5)}
    val authService = remember { AuthService() }

    fun submit(){
        val s = MiniGameScoresService()
        val userId = authService.getCurrentUserId()
        (userId?.let{s.addUserGameAttempt(it, GameType.COMPLEX_ATTENTION, score, {})})
    }
    // Timer
    LaunchedEffect(timeleft) {
        for (i in timeleft downTo 0) {
            timeleft = i
            delay(1000)
        }
        showbox = true
    }
    if(showbox){
        AlertDialog(
            onDismissRequest = {
                submit()
                onReturn()
            },
            title = { Text("Submit your score") },
            text = { Text("Your score is $score. Submit score?") },
            confirmButton = {
                TextButton(onClick = {
                    submit()
                    onReturn()
                    }
                ){
                    Text("OK")
                }
            }
        )
    }


// When a circle is clicked, the score increases. This function detects the increase in score and then finds a new random location for the circle
    LaunchedEffect(score) {
        x = Random.nextDouble()
        y = Random.nextDouble()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Row(){
            // Time display
            Text(
                text = "Time: ",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "$timeleft",
                fontWeight= FontWeight.Bold,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.weight(1f))
            // Quit button
            Button(
                onClick = onReturn,
                modifier = Modifier.size(110.dp, 35.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ){Text("Quit")}
        }
        Row(){
            // Score display
            Text(
                text = "Score: ",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "$score",
                fontWeight= FontWeight.Bold,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
            )
        }
        // Code for getting the size of this row adapted from the solution by Gabriele Mariotti on StackOverflow
        // https://stackoverflow.com/questions/67138343/jetpack-compose-find-parents-width-length
        var size by remember { mutableStateOf(Size.Zero)}
        Row(modifier = Modifier
            .padding(6.dp, 16.dp)
            .fillMaxSize()
            .background(Color.LightGray)
            .onGloballyPositioned { coordinates ->
                size = coordinates.size.toSize()
            }){
            val density = LocalDensity.current.density
            // Use padding to move the column with the button to the correct spot
            // using the random numbers to calculate how much padding to use
            Column(Modifier.padding(max(0.0,(size.width/density*x-64)).dp, max(0.0,(size.height/density*y-64)).dp, 0.dp, 0.dp)){
                Button(
                    onClick = {
                        if (timeleft > 0) {
                            score += 1
                        }
                    },
                    modifier = Modifier.size(64.dp, 64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {}
            }
        }
    }
}