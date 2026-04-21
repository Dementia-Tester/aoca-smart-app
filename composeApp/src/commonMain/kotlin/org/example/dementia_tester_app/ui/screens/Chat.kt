// Added click handling for chat items and new chat button to fix Issue #4
package org.example.dementia_tester_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.dementia_tester_app.ui.components.FormColors

/**
 * Chat screen with search bar, new chat button, and list of chats
 */
@Composable
fun Chat() {
    var searchQuery by remember { mutableStateOf("") }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var selectedChat by remember { mutableStateOf<ChatItem?>(null) }

    val chats = remember {
        listOf(
            ChatItem(
                name = "Dr. Smith",
                lastMessage = "Your next appointment is scheduled for tomorrow at 10:00 AM.",
                time = "10:30 AM",
                unreadCount = 1
            ),
            ChatItem(
                name = "Nurse Johnson",
                lastMessage = "How are you feeling today? Don't forget to take your medication.",
                time = "Yesterday",
                unreadCount = 0
            ),
            ChatItem(
                name = "Caregiver Support",
                lastMessage = "We've sent you the resources we discussed during our last conversation.",
                time = "Jul 19",
                unreadCount = 3
            ),
            ChatItem(
                name = "Memory Clinic",
                lastMessage = "Your test results have been uploaded to your profile.",
                time = "Jul 15",
                unreadCount = 0
            ),
            ChatItem(
                name = "Medication Reminder",
                lastMessage = "It's time to take your evening medication.",
                time = "Jul 10",
                unreadCount = 0
            )
        )
    }

    val filteredChats = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            chats
        } else {
            chats.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.lastMessage.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search chats...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = FormColors.green
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FormColors.green,
                unfocusedBorderColor = FormColors.green,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            singleLine = true
        )

        Button(
            onClick = { showNewChatDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FormColors.green
            )
        ) {
            Text(
                text = "Start New Chat",
                fontSize = 16.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            if (filteredChats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No chats found",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                filteredChats.forEach { chat ->
                    ChatListItem(
                        chat = chat,
                        onChatClick = {
                            selectedChat = chat
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            confirmButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("OK", color = FormColors.green)
                }
            },
            title = {
                Text("New Chat")
            },
            text = {
                Text("Start New Chat button is now working. Navigation can be added later.")
            }
        )
    }

    selectedChat?.let { chat ->
        AlertDialog(
            onDismissRequest = { selectedChat = null },
            confirmButton = {
                TextButton(onClick = { selectedChat = null }) {
                    Text("OK", color = FormColors.green)
                }
            },
            title = {
                Text(chat.name)
            },
            text = {
                Text("Chat item click is working.\n\nLast message: ${chat.lastMessage}")
            }
        )
    }
}

/**
 * Data class representing a chat item
 */
data class ChatItem(
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int
)

/**
 * Composable for displaying a chat list item
 */
@Composable
fun ChatListItem(
    chat: ChatItem,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onChatClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chat.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = chat.lastMessage,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 2
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = chat.time,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(FormColors.green)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}