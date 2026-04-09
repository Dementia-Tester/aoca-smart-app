package org.example.dementia_tester_app
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class Minigame(val minigameName: String, val displayName: String, val description: String){
    // Function adapted from https://developer.android.com/develop/ui/compose/components/card
    @Composable
    fun GetCard(onClick: () -> Unit = {}){
        OutlinedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 5.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable{onClick()}
        ){
            Column(modifier = Modifier.padding(16.dp)){
                Text(
                    text = displayName,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(2.dp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = description,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(2.dp),
                    textAlign = TextAlign.Center,
                )
            }

        }
    }
}