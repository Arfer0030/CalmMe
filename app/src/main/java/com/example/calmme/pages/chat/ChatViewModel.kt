package com.example.calmme.pages.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import java.util.*
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
import java.text.SimpleDateFormat

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

    private val _canSendMessage = MutableStateFlow(true)
    val canSendMessage: StateFlow<Boolean> = _canSendMessage

    private val _chatTimeStatus = MutableStateFlow("")
    val chatTimeStatus: StateFlow<String> = _chatTimeStatus

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
            when (val result = chatRepository.getChatRoom(chatRoomId)) {
                is Resource.Success -> {
                    _chatRoom.value = result.data
                    checkChatTime()
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
        if (!_canSendMessage.value) {
            Log.e("ChatViewModel", "Cannot send message: Outside chat time")
            return
        }

        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch
                val chatRoom = _chatRoom.value ?: return@launch

                if (chatRoom.userId.isEmpty()) {
                    Log.e("ChatViewModel", "No participants in chat room")
                    return@launch
                }

                val receiverId = chatRoom.userId.firstOrNull { it != currentUserId }
                if (receiverId == null) {
                    Log.e("ChatViewModel", "No receiver found in participants")
                    return@launch
                }

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

                when (val result = chatRepository.sendMessage(chatRoomId, message)) {
                    is Resource.Success -> {
                        Log.d("ChatViewModel", "Message sent successfully")
                    }
                    is Resource.Error -> {
                        if (result.message.contains("appointment time")) {
                            _canSendMessage.value = false
                            _chatTimeStatus.value = result.message
                        }
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
            }
        }
    }

    private fun checkChatTime() {
        viewModelScope.launch {
            val chatRoom = _chatRoom.value ?: return@launch
            val canSend = isWithinChatTime(chatRoom.startTime, chatRoom.endTime)
            _canSendMessage.value = canSend

            if (!canSend) {
                _chatTimeStatus.value = "Chat is only available during appointment time: ${formatChatTime(chatRoom.startTime, chatRoom.endTime)}"
            } else {
                _chatTimeStatus.value = ""
            }
        }
    }

    private fun isWithinChatTime(startTime: String?, endTime: String?): Boolean {
        return try {
            if (startTime.isNullOrEmpty() || endTime.isNullOrEmpty()) {
                return true
            }
            val currentTime = Date()
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

            val start = sdf.parse(startTime)
            val end = sdf.parse(endTime)

            if (start != null && end != null) {
                currentTime.time in start.time..end.time
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error parsing chat time", e)
            true
        }
    }

    private fun formatChatTime(startTime: String?, endTime: String?): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val displaySdf = SimpleDateFormat("HH:mm", Locale.getDefault())

            val start = startTime?.let { sdf.parse(it) }
            val end = endTime?.let { sdf.parse(it) }

            if (start != null && end != null) {
                "${displaySdf.format(start)} - ${displaySdf.format(end)}"
            } else {
                "Not specified"
            }
        } catch (e: Exception) {
            "Invalid time format"
        }
    }
    fun refreshChatTimeStatus() {
        checkChatTime()
    }
}
