package org.example.dementia_tester_app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Admin Dashboard screen
 * This dashboard is shown to users with userType = ADMIN
 */
@Composable
fun AdminDashboard() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Admin Control Center",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "System Overview", style = MaterialTheme.typography.titleMedium)
                Text(text = "Total Users: --")
                Text(text = "Active Sessions: --")
            }
        }

        Button(
            onClick = { /* TODO: Implement user management */ },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text("Manage All Users")
        }

        Button(
            onClick = { /* TODO: Implement system settings */ },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text("System Configuration")
        }
    }
}
