package org.example.dementia_tester_app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.dementia_tester_app.ui.components.CollapsibleSection

/**
 * Help screen with collapsible sections for different help topics
 */
@Composable
fun Help() {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        // FAQ Section
        CollapsibleSection(
            title = "FAQ",
            content = {
                Column {
                    Text(
                        text = "Frequently Asked Questions",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Q: How do I reset my password?")
                    Text(
                        text = "A: Go to Settings -> Account -> Change Password.",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Q: How often should I take the cognitive tests?")
                    Text(
                        text = "A: We recommend taking tests once a week for consistent monitoring.",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Q: Can I share my results with my doctor?")
                    Text(
                        text = "A: Yes, you can export your results from the Profile section.",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Q: Is my data secure?")
                    Text(
                        text = "A: Yes, all your data is encrypted and stored securely.",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        )
        
        // User Manual Section
        CollapsibleSection(
            title = "User Manual",
            content = {
                Column {
                    Text(
                        text = "App User Guide",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Dashboard: View your progress and upcoming activities")
                    Text("Tests: Take cognitive assessments to monitor your health")
                    Text("Activities: Access brain exercises and memory games")
                    Text("Profile: Manage your personal information and settings")
                    Text("Appointments: Schedule and manage healthcare appointments")
                    Text("Settings: Customize app appearance and notifications")
                }
            }
        )
        
        // Emergency Help Section
        CollapsibleSection(
            title = "Emergency Help",
            content = {
                Column {
                    Text(
                        text = "Emergency Assistance",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Emergency Services: 000")
                    Text("Dementia Support Hotline: 1800 100 500")
                    Text("Crisis Support: 13 11 14")
                    Text("Lifeline: 13 43 57")
                    Text("If you're experiencing a medical emergency, please call 000 immediately.")
                }
            }
        )
        
        // Feedback/Comments Section
        CollapsibleSection(
            title = "Feedback/Comments",
            content = {
                Column {
                    Text(
                        text = "Share Your Thoughts",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("We value your feedback to improve our app.")
                    Text("Email: feedback@dementia-tester.com")
                    Text("In-app: Go to Settings > Send Feedback")
                    Text("App Store: Leave a review on your app store")
                    Text("Your suggestions help us make the app better for everyone.")
                }
            }
        )
        
        // Contact Section
        CollapsibleSection(
            title = "Contact",
            content = {
                Column {
                    Text(
                        text = "Contact Information",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Technical Support: support@dementia-tester.com")
                    Text("Phone: +61 2 1234 5678")
                    Text("Hours: Monday to Friday, 9am - 5pm AEST")
                    Text("Address: 123 Health Street, Sydney NSW 2000")
                    Text("Website: www.dementia-tester.com")
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}