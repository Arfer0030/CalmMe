package com.example.calmme.pages.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calmme.data.ChatMessage
import com.example.calmme.data.ChatRepository
import com.example.calmme.data.ChatRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.calmme.commons.Resource

class ChatViewModel(
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _chatRoom = MutableStateFlow<ChatRoom?>(null)
    val chatRoom: StateFlow<ChatRoom?> = _chatRoom

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId

    private var chatRoomId: String = ""

    init {
        _currentUserId.value = auth.currentUser?.uid ?: ""
    }

    fun initializeChatWithRoomId(roomId: String) {
        chatRoomId = roomId
        loadChatRoom()
        loadMessages()
    }

    private fun loadChatRoom() {
        viewModelScope.launch {
            Log.d("ChatViewModel", "Loading chat room: $chatRoomId")
            when (val result = chatRepository.getChatRoom(chatRoomId)) {
                is Resource.Success -> {
                    Log.d("ChatViewModel", "Chat room loaded: ${result.data}")
                    _chatRoom.value = result.data
                }
                is Resource.Error -> {
                    Log.e("ChatViewModel", "Failed to load chat room: ${result.message}")
                }
                else -> {}
            }
        }
    }


    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getChatMessages(chatRoomId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _messages.value = result.data
                    }
                    else -> {}
                }
            }
        }
    }

    fun sendMessage(messageText: String) {
        if (messageText.isBlank()) return

        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch
                val chatRoom = _chatRoom.value ?: return@launch

                Log.d("ChatViewModel", "ChatRoom data: $chatRoom")
                Log.d("ChatViewModel", "UserId array: ${chatRoom.userId}")
                Log.d("ChatViewModel", "Current user: $currentUserId")

                // ✅ PERBAIKAN: Gunakan userId field yang sesuai dengan Firestore
                if (chatRoom.userId.isEmpty()) {
                    Log.e("ChatViewModel", "No participants in chat room")
                    return@launch
                }

                // ✅ PERBAIKAN: Gunakan userId field
                val receiverId = chatRoom.userId.firstOrNull { it != currentUserId }
                if (receiverId == null) {
                    Log.e("ChatViewModel", "No receiver found in participants")
                    return@launch
                }

                // ✅ PERBAIKAN: Safe access untuk senderName
                val senderName = when {
                    chatRoom.userId.size >= 2 && currentUserId == chatRoom.userId[0] -> chatRoom.userName
                    chatRoom.userId.size >= 2 && currentUserId == chatRoom.userId[1] -> chatRoom.psychologistName
                    else -> "Unknown"
                }

                val message = ChatMessage(
                    chatRoomId = chatRoomId,
                    senderId = currentUserId,
                    receiverId = receiverId,
                    messageText = messageText,
                    timestamp = Timestamp.now(),
                    senderName = senderName
                )

                Log.d("ChatViewModel", "Sending message: $message")

                when (val result = chatRepository.sendMessage(chatRoomId, message)) {
                    is Resource.Success -> {
                        Log.d("ChatViewModel", "Message sent successfully")
                    }
                    is Resource.Error -> {
                        Log.e("ChatViewModel", "Failed to send message: ${result.message}")
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
            }
        }
    }
}
