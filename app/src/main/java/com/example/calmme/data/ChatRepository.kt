package com.example.calmme.data

import android.util.Log
import com.example.calmme.data.ChatMessage
import com.example.calmme.data.ChatRoom
import com.example.calmme.commons.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun createOrGetChatRoom(
        appointmentId: String,
        userId: String,
        psychologistId: String
    ): Resource<String> {
        return try {
            // Check if chat room already exists
            val existingChatRoom = firestore.collection("chat_rooms")
                .whereEqualTo("appointmentId", appointmentId)
                .limit(1)
                .get()
                .await()

            if (!existingChatRoom.isEmpty) {
                val chatRoomId = existingChatRoom.documents.first().getString("chatRoomId") ?: ""
                Resource.Success(chatRoomId)
            } else {
                // Create new chat room
                val chatRoomId = UUID.randomUUID().toString()

                // Get user and psychologist names
                val userDoc = firestore.collection("users").document(userId).get().await()
                val psychologistDoc = firestore.collection("psychologists").document(psychologistId).get().await()

                val userName = userDoc.getString("username") ?: "User"
                val psychologistName = psychologistDoc.getString("name") ?: "Psychologist"

                val chatRoom = ChatRoom(
                    chatRoomId = chatRoomId,
                    appointmentId = appointmentId,
                    userId = listOf(userId, psychologistId),
                    createdAt = Timestamp.now(),
                    psychologistName = psychologistName,
                    userName = userName
                )

                firestore.collection("chat_rooms").document(chatRoomId)
                    .set(chatRoom)
                    .await()

                // Update appointment with chatRoomId
                firestore.collection("appointments").document(appointmentId)
                    .update("chatRoomId", chatRoomId)
                    .await()

                Resource.Success(chatRoomId)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create chat room")
        }
    }

    suspend fun sendMessage(chatRoomId: String, chatMessage: ChatMessage): Resource<Unit> {
        return try {
            val messageId = UUID.randomUUID().toString()
            val messageWithId = chatMessage.copy(messageId = messageId)

            // Add message to subcollection
            firestore.collection("chat_rooms")
                .document(chatRoomId)
                .collection("messages")
                .document(messageId)
                .set(messageWithId)
                .await()

            // Update last message in chat room
            firestore.collection("chat_rooms").document(chatRoomId)
                .update(
                    mapOf(
                        "lastMessageText" to chatMessage.messageText,
                        "lastMessageTimestamp" to chatMessage.timestamp
                    )
                )
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send message")
        }
    }

    fun getChatMessages(chatRoomId: String): Flow<Resource<List<ChatMessage>>> = callbackFlow {
        val listener = firestore.collection("chat_rooms")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load messages"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)
                    }
                    trySend(Resource.Success(messages))
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun getChatRoom(chatRoomId: String): Resource<ChatRoom> {
        return try {
            Log.d("ChatRepository", "Getting chat room: $chatRoomId")
            val doc = firestore.collection("chat_rooms").document(chatRoomId).get().await()

            if (doc.exists()) {
                val chatRoom = doc.toObject(ChatRoom::class.java)
                Log.d("ChatRepository", "Chat room found: $chatRoom")
                if (chatRoom != null) {
                    Resource.Success(chatRoom)
                } else {
                    Log.e("ChatRepository", "Failed to parse chat room")
                    Resource.Error("Failed to parse chat room")
                }
            } else {
                Log.e("ChatRepository", "Chat room document not found")
                Resource.Error("Chat room not found")
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error getting chat room", e)
            Resource.Error(e.message ?: "Failed to load chat room")
        }
    }
}

// ❌ HAPUS INI - Karena sudah ada di PsikologRepository
/*
sealed class Resource<T> {
    data class Loading<T>(val isLoading: Boolean = true) : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
}
*/
