package com.example.calmme.pages.chat

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.calmme.R
import com.example.calmme.commons.LocalNavController
import com.example.calmme.data.ChatMessage
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun ChatScreen(
    chatRoomId: String,
    chatViewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()
    val messages by chatViewModel.messages.collectAsState()
    val chatRoom by chatViewModel.chatRoom.collectAsState()
    val currentUserId by chatViewModel.currentUserId.collectAsState()
    val canSendMessage by chatViewModel.canSendMessage.collectAsState()
    val chatTimeStatus by chatViewModel.chatTimeStatus.collectAsState()
    val otherUserProfilePicture by chatViewModel.otherUserProfilePicture.collectAsState()

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chatRoomId) {
        chatViewModel.initializeChatWithRoomId(chatRoomId)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60000) // 1 menit
            chatViewModel.refreshChatTimeStatus()
        }
    }

    // Auto scroll ke bawah ketika ada pesan baru
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                Brush.linearGradient(
                    listOf(Color(0xFFB8F7FD),Color(0xFFCEBFE6))
                )
            ),
            color = Color.Transparent,

        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.popBackStack() }
                )

                Spacer(modifier = Modifier.width(12.dp))

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(otherUserProfilePicture)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Other User Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(id = R.drawable.profile),
                    error = painterResource(id = R.drawable.profile),
                    fallback = painterResource(id = R.drawable.profile),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Display nama
                Column {
                    Text(
                        text = chatRoom?.let { room ->
                            if (currentUserId == room.userId.firstOrNull()) {
                                room.psychologistName
                            } else {
                                room.userName
                            }
                        } ?: "Loading...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "online",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Date header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getCurrentDateFormatted(),
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier
                    .background(
                        Color.White,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        if (!canSendMessage && chatTimeStatus.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFE0B2)
                )
            ) {
                Text(
                    text = chatTimeStatus,
                    modifier = Modifier.padding(12.dp),
                    color = Color(0xFFE65100),
                    fontSize = 14.sp
                )
            }
        }

        // Pesan
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { message ->
                ChatItem(
                    message = message,
                    isCurrentUser = message.senderId == currentUserId,
                    profilePictureUrl = otherUserProfilePicture
                )
            }
        }

        // Input pesan
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            if (canSendMessage) "Type your message..."
                            else "Chat not available outside appointment time"
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    enabled = canSendMessage,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8E44AD),
                        unfocusedBorderColor = Color.LightGray,
                        disabledBorderColor = Color.LightGray,
                        disabledTextColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (messageText.isNotBlank() && canSendMessage) {
                            chatViewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    enabled = canSendMessage,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canSendMessage) Color(0xFF8E44AD) else Color.LightGray
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_message),
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    message: ChatMessage,
    isCurrentUser: Boolean,
    profilePictureUrl: String? = null
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isCurrentUser) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(profilePictureUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Sender Profile",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                placeholder = painterResource(id = R.drawable.profile),
                error = painterResource(id = R.drawable.profile),
                fallback = painterResource(id = R.drawable.profile),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isCurrentUser) Color(0xFF8E44AD) else Color(0xFFE8F5E8),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                            bottomEnd = if (isCurrentUser) 4.dp else 16.dp
                        )
                    )
                    .padding(12.dp)
                    .widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.messageText,
                    color = if (isCurrentUser) Color.White else Color.Black,
                    fontSize = 14.sp
                )
            }
            Text(
                text = formatTimestamp(message.timestamp),
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            )
            // Checkmark buat pesan yang dikirim
            if (isCurrentUser) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "Sent",
                    modifier = Modifier
                        .size(12.dp)
                        .padding(top = 2.dp),
                    tint = Color.Gray
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

private fun getCurrentDateFormatted(): String {
    val sdf = SimpleDateFormat("EEEE, dd/MM/yy", Locale.getDefault())
    return sdf.format(Date())
}

