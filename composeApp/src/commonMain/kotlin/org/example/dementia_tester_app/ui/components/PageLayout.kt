package org.example.dementia_tester_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A reusable component for page layout with header
 * @param drawerState The drawer state for the navigation drawer
 * @param title The title to display in the header
 * @param content The content to display in the page
 */
@Composable
fun PageLayout(
    drawerState: DrawerState,
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(top = 0.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Display the header
        AppHeader(drawerState = drawerState, title = title)
        
        // Display the content
        content()
    }
}