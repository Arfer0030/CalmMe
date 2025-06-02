package com.example.calmme.data

import android.util.Log
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

    // Fungsi buat atau ambil chatroom
    suspend fun createOrGetChatRoom(
        appointmentId: String,
        userId: String,
        psychologistId: String
    ): Resource<String> {
        return try {
            // Cek kalo appointment dahh ada chatroom
            val existingChatRoom = firestore.collection("chat_rooms")
                .whereEqualTo("appointmentId", appointmentId)
                .limit(1)
                .get()
                .await()

            if (!existingChatRoom.isEmpty) {
                val chatRoomId = existingChatRoom.documents.first().getString("chatRoomId") ?: ""
                Resource.Success(chatRoomId)
            } else {
                // Bikin chatroom baru kalo belom ada
                val chatRoomId = UUID.randomUUID().toString()

                // Ambil data apppointment
                val appointmentDoc = firestore.collection("appointments").document(appointmentId).get().await()
                val appointmentData = appointmentDoc.data ?: return Resource.Error("Appointment not found")

                val patientId = appointmentData["userId"] as? String ?: ""
                val appointmentDate = appointmentData["appointmentDate"] as? String ?: ""
                val appointmentTime = appointmentData["appointmentTime"] as? String ?: ""

                // Ambil psychologist data untuk userId dan name
                val psychologistDoc = firestore.collection("psychologists").document(psychologistId).get().await()
                val psychologistData = psychologistDoc.data ?: return Resource.Error("Psychologist not found")

                val psychologistUserId = psychologistData["userId"] as? String ?: ""
                val psychologistName = psychologistData["name"] as? String ?: "Psychologist"

                if (psychologistUserId.isEmpty()) {
                    return Resource.Error("Psychologist user ID not found")
                }

                // Ambil user data untuk mendapatkan username
                val userDoc = firestore.collection("users").document(patientId).get().await()
                val userData = userDoc.data
                val userName = userData?.get("username") as? String ?: "User"

                // Parse time untuk startTime dan endTime
                val timeRange = appointmentTime.split("-")
                val startTime = if (timeRange.isNotEmpty()) "${appointmentDate}T${timeRange[0]}:00Z" else ""
                val endTime = if (timeRange.size >= 2) "${appointmentDate}T${timeRange[1]}:00Z" else ""

                // Buat chatroom isi data
                val chatRoomData = hashMapOf(
                    "chatRoomId" to chatRoomId,
                    "appointmentId" to appointmentId,
                    "userId" to listOf(patientId, psychologistUserId),
                    "psychologistId" to psychologistId,
                    "isActive" to false,
                    "startTime" to startTime,
                    "endTime" to endTime,
                    "createdAt" to Timestamp.now(),
                    "userName" to userName,
                    "psychologistName" to psychologistName
                )

                // Update chatroom
                firestore.collection("chat_rooms").document(chatRoomId)
                    .set(chatRoomData)
                    .await()

                // Update appointment dengan chatRoomId
                firestore.collection("appointments").document(appointmentId)
                    .update("chatRoomId", chatRoomId)
                    .await()

                Resource.Success(chatRoomId)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create chat room")
        }
    }

    // Fungsi buat kirim pesan
    suspend fun sendMessage(chatRoomId: String, chatMessage: ChatMessage): Resource<Unit> {
        return try {
            val messageId = UUID.randomUUID().toString()
            val messageWithId = chatMessage.copy(messageId = messageId)

            // Tambah message jadi subcollection chatroom
            firestore.collection("chat_rooms")
                .document(chatRoomId)
                .collection("messages")
                .document(messageId)
                .set(messageWithId)
                .await()

            // Update last message di chatroom
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

    // Fungsi buat ambil pesan
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

    // Fungsi buat ambil chatroom
    suspend fun getChatRoom(chatRoomId: String): Resource<ChatRoom> {
        return try {
            val doc = firestore.collection("chat_rooms").document(chatRoomId).get().await()

            if (doc.exists()) {
                val chatRoom = doc.toObject(ChatRoom::class.java)
                if (chatRoom != null) {
                    Resource.Success(chatRoom)
                } else {
                    Resource.Error("Failed to parse chat room")
                }
            } else {
                Resource.Error("Chat room not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load chat room")
        }
    }
}