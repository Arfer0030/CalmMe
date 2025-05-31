package com.example.calmme.data

import com.example.calmme.R
import com.example.calmme.pages.assesment.QuestionItem
import com.google.firebase.Timestamp

data class PsychologistData(
    val psychologistId: String = "",
    val userId: String = "",
    val name: String = "",
    val specialization: List<String> = emptyList(),
    val description: String = "",
    val experience: String = "",
    val education: String = "",
    val license: String = "",
    val isAvailable: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class TimeSlot(
    val startTime: String = "",
    val endTime: String = "",
    val isAvailable: Boolean = true
)

data class ScheduleData(
    val scheduleId: String = "",
    val psychologistId: String = "",
    val dayOfWeek: String = "",
    val timeSlots: List<TimeSlot> = emptyList(),
    val date: String = "",
    val isRecurring: Boolean = true,
    val createdAt: Timestamp? = null
)

// ChatMessage.kt
data class ChatMessage(
    val messageId: String = "",
    val chatRoomId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val messageText: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val senderName: String = ""
)

// ChatRoom.kt
// Di ChatRoom.kt - Sesuaikan dengan struktur Firestore
data class ChatRoom(
    val chatRoomId: String = "",
    val appointmentId: String = "",
    val userId: List<String> = emptyList(), // ✅ Ubah dari participantIds ke userId
    val psychologistId: String = "",
    val lastMessageText: String? = null,
    val lastMessageTimestamp: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val psychologistName: String = "",
    val userName: String = "",
    val isActive: Boolean = false,
    val startTime: String = "",
    val endTime: String = ""
) {
    // ✅ Tambahkan property untuk backward compatibility
    val participantIds: List<String>
        get() = userId
}



val Questions = listOf(
    QuestionItem("Feeling restless, anxious, or extremely tense"),
    QuestionItem("Unable to stop or control worry"),
    QuestionItem("Worrying too much about things"),
    QuestionItem("Hard to relax"),
    QuestionItem("Very restless making it hard to sit still"),
    QuestionItem("Being easily irritated or irritable"),
    QuestionItem("Feeling afraid as if something terrible might happen")
)
