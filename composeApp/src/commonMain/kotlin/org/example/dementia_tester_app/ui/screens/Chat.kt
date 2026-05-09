// Local working chat UI - conversation screen with contact picker and last message update
package org.example.dementia_tester_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.dementia_tester_app.ui.components.FormColors

@Composable
fun Chat() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedChat by remember { mutableStateOf<ChatItem?>(null) }
    var showContactPicker by remember { mutableStateOf(false) }

    val chats = remember {
        mutableStateListOf(
            ChatItem("Dr. Smith", "Your next appointment is scheduled for tomorrow at 10:00 AM.", "10:30 AM", 1),
            ChatItem("Nurse Johnson", "How are you feeling today? Don't forget to take your medication.", "Yesterday", 0),
            ChatItem("Caregiver Support", "We've sent you the resources we discussed during our last conversation.", "Jul 19", 3),
            ChatItem("Memory Clinic", "Your test results have been uploaded to your profile.", "Jul 15", 0),
            ChatItem("Medication Reminder", "It's time to take your evening medication.", "Jul 10", 0)
        )
    }

    fun updateLastMessage(chatName: String, newMessage: String) {
        val index = chats.indexOfFirst { it.name == chatName }

        if (index != -1) {
            chats[index] = chats[index].copy(
                lastMessage = newMessage,
                time = "Now",
                unreadCount = 0
            )
            selectedChat = chats[index]
        } else {
            val newChat = ChatItem(
                name = chatName,
                lastMessage = newMessage,
                time = "Now",
                unreadCount = 0
            )
            chats.add(0, newChat)
            selectedChat = newChat
        }
    }

    if (selectedChat != null) {
        ChatConversationScreen(
            chat = selectedChat!!,
            onBack = { selectedChat = null },
            onMessageSent = { chatName, message ->
                updateLastMessage(chatName, message)
            }
        )
    } else {
        ChatListScreen(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            chats = chats.filter {
                searchQuery.isBlank() ||
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.lastMessage.contains(searchQuery, ignoreCase = true)
            },
            onChatClick = { selectedChat = it },
            onNewChatClick = { showContactPicker = true }
        )
    }

    if (showContactPicker) {
        AlertDialog(
            onDismissRequest = { showContactPicker = false },
            title = {
                Text("Start New Chat")
            },
            text = {
                Column {
                    Text("Who do you want to chat with?")

                    Spacer(modifier = Modifier.height(12.dp))

                    listOf(
                        "Dr. Smith",
                        "Nurse Johnson",
                        "Caregiver Support",
                        "Family Caregiver",
                        "Memory Clinic"
                    ).forEach { contactName ->
                        Text(
                            text = contactName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val existingChat = chats.find { it.name == contactName }

                                    selectedChat = existingChat ?: ChatItem(
                                        name = contactName,
                                        lastMessage = "",
                                        time = "Now",
                                        unreadCount = 0
                                    )

                                    showContactPicker = false
                                }
                                .padding(vertical = 12.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showContactPicker = false }) {
                    Text("Cancel", color = FormColors.green)
                }
            }
        )
    }
}

@Composable
fun ChatListScreen(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    chats: List<ChatItem>,
    onChatClick: (ChatItem) -> Unit,
    onNewChatClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
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
            onClick = onNewChatClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FormColors.green
            )
        ) {
            Text("Start New Chat", fontSize = 16.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            if (chats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No chats found", color = Color.Gray)
                }
            } else {
                chats.forEach { chat ->
                    ChatListItem(
                        chat = chat,
                        onChatClick = { onChatClick(chat) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ChatConversationScreen(
    chat: ChatItem,
    onBack: () -> Unit,
    onMessageSent: (String, String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    val messages = remember(chat.name) {
        mutableStateListOf(
            ChatMessage(chat.lastMessage.ifBlank { "Start a new conversation." }, false)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(8.dp),
                tint = FormColors.green
            )

            Text(
                text = chat.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            messages.forEach { message ->
                ChatBubble(message)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FormColors.green,
                    unfocusedBorderColor = FormColors.green,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        val sentMessage = messageText.trim()
                        messages.add(ChatMessage(sentMessage, true))
                        onMessageSent(chat.name, sentMessage)
                        messageText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = FormColors.green
                )
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (message.isFromUser) FormColors.green else Color(0xFFEFEFEF))
                .padding(12.dp)
                .widthIn(max = 260.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isFromUser) Color.White else Color.Black,
                fontSize = 14.sp
            )
        }
    }
}

data class ChatItem(
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int
)

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean
)

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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = chat.lastMessage.ifBlank { "No messages yet" },
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 2
                )
            }

            Column(horizontalAlignment = Alignment.End) {
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