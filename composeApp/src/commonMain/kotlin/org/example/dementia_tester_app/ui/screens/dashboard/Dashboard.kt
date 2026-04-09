package org.example.dementia_tester_app.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.dementia_tester_app.ui.components.HorizontalMenu
import org.example.dementia_tester_app.ui.screens.FocusFlick
import org.example.dementia_tester_app.ui.screens.TaskSwitch
import org.example.dementia_tester_app.ui.screens.WordRecall

/**
 * User Dashboard screen
 * This dashboard is shown to users with userType = USER
 */
@Composable
fun Dashboard() {
    var currentgame by remember { mutableStateOf("") }
    // Define the menu items
    val menuItems = listOf("Reminders", "Test", "Games", "Progress")
    var selectedMenuItem by remember { mutableStateOf(menuItems[0]) }
    val headerColor = Color(0xFF66BB23)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HorizontalMenu(
            menuItems = menuItems,
            selectedMenuItem = selectedMenuItem,
            activeColor = headerColor,
            onMenuItemSelected = { selectedMenuItem = it }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            when (selectedMenuItem) {
                "Reminders" -> {
                    currentgame=""
                    RemindersView()
                }

                "Test" -> {
                    currentgame=""
                    TestView()
                }
                "Games" -> {
                    if (currentgame == ""){
                        GamesView{g->currentgame=g}
                    }else{
                        when (currentgame){
                            "FocusFlick" -> FocusFlick({currentgame = ""})
                            "TaskSwitch" -> TaskSwitch({currentgame = ""})
                            "WordRecall" -> WordRecall({currentgame = ""})
                        }
                    }
                }
                "Progress" -> {
                    currentgame=""
                    ProgressView()
                }
            }
        }
    }
}
