package org.example.dementia_tester_app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.dementia_tester_app.data.UserProfile

/**
 * A reusable patient selection dropdown component
 * 
 * @param userList List of users in the format "Users name" or "Users email" if name is undefined
 * @param userMap Mapping between formatted user strings and UserProfile objects
 * @param selectedUserString Currently selected user string
 * @param placeholder Text to display when no user is selected
 * @param onUserSelected Callback when a user is selected, provides both the string and UserProfile
 * @param modifier Optional modifier for the component
 */
@Composable
fun PatientSelect(
    userList: List<String>,
    userMap: Map<String, UserProfile>,
    selectedUserString: String?,
    placeholder: String = "Select a patient",
    onUserSelected: (String, UserProfile?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    
    // Filter and sort the user list based on search text
    val filteredUserList = remember(searchText, userList) {
        if (searchText.isEmpty()) {
            userList.sorted()
        } else {
            userList.filter { it.contains(searchText, ignoreCase = true) }.sorted()
        }
    }
    
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Button to open dropdown
        OutlinedButton(
            onClick = { isDropdownExpanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface), // Applied change
            border = BorderStroke(2.dp, Color.Gray),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface, // Applied change
                contentColor = MaterialTheme.colorScheme.onSurface // Applied change
            )
        ) {
            Text(
                text = selectedUserString ?: placeholder,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface, // Applied change
            )
        }

        // Dropdown menu
        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight(0.7f)
                .background(MaterialTheme.colorScheme.surface) // Applied change
        ) {
            // Search field at the top of the dropdown
            Column(modifier = Modifier.padding(8.dp)) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search patients") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface), // Applied change
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            
            // List of users
            filteredUserList.forEach { userString ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = userString,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface // Applied change
                        ) 
                    },
                    onClick = {
                        onUserSelected(userString, userMap[userString])
                        isDropdownExpanded = false
                        searchText = ""
                    },
                    modifier = Modifier.height(36.dp)
                )
            }
        }
    }
}
