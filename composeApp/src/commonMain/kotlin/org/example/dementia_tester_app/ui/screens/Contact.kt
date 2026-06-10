package org.example.dementia_tester_app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.dementia_tester_app.ui.components.CollapsibleSection

/**
 * Contact screen with collapsible sections for different contact types
 */
@Composable
fun Contact() {
    val scrollState = rememberScrollState()
    
    var isAppExpanded by remember { mutableStateOf(true) }
    var isEmergencyExpanded by remember { mutableStateOf(false) }
    var isCarerExpanded by remember { mutableStateOf(false) }
    var isDoctorExpanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        // App Support Section
        CollapsibleSection(
            title = "App",
            isExpanded = isAppExpanded,
            onHeaderClick = { isAppExpanded = !isAppExpanded },
            content = {
                Column {
                    Text(
                        text = "App Support",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Email: dementiatester@gmail.com")
                    Text("Phone: 0402743250")
                    Text("Hours: Monday to Friday, 9am - 5pm AEST")
                    Text("For technical issues, feature requests, or general app inquiries")
                }
            }
        )
        
        // Emergency Section
        CollapsibleSection(
            title = "Emergency",
            isExpanded = isEmergencyExpanded,
            onHeaderClick = { isEmergencyExpanded = !isEmergencyExpanded },
            content = {
                Column {
                    Text(
                        text = "Emergency Contacts",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Emergency Services: 000")
                    Text("Dementia Support Hotline: 1800 100 500")
                    Text("Crisis Support: 13 11 14")
                    Text("Available 24/7 for urgent assistance")
                }
            }
        )
        
        // Carer Section
        CollapsibleSection(
            title = "Carer",
            isExpanded = isCarerExpanded,
            onHeaderClick = { isCarerExpanded = !isCarerExpanded },
            content = {
                Column {
                    Text(
                        text = "Carer Support",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Carer Gateway: 1800 422 737")
                    Text("Email: dementiatester@gmail.com")
                    Text("Website: www.carergateway.gov.au")
                    Text("Support groups, respite care information, and resources for carers")
                }
            }
        )
        
        // Doctor Section
        CollapsibleSection(
            title = "Doctor",
            isExpanded = isDoctorExpanded,
            onHeaderClick = { isDoctorExpanded = !isDoctorExpanded },
            content = {
                Column {
                    Text(
                        text = "Medical Support",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("Telehealth: 1800 987 654")
                    Text("Email: dementiatester@gmail.com")
                    Text("Hours: Monday to Sunday, 8am - 8pm")
                    Text("Connect with specialists, schedule appointments, or get medical advice")
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
