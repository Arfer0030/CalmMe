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

data class ChatMessage(
    val messageId: String = "",
    val chatRoomId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val messageText: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val senderName: String = ""
)

data class ChatRoom(
    val chatRoomId: String = "",
    val appointmentId: String = "",
    val userId: List<String> = emptyList(),
    val psychologistId: String = "",
    val lastMessageText: String? = null,
    val lastMessageTimestamp: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val psychologistName: String = "",
    val userName: String = "",
    val isActive: Boolean = false,
    val startTime: String = "",
    val endTime: String = ""
)

data class AppointmentData(
    val appointmentId: String = "",
    val userId: String = "",
    val psychologistId: String = "",
    val appointmentDate: String = "",
    val appointmentTime: String = "",
    val status: String = "scheduled",
    val paymentStatus: String = "pending",
    val paymentMethod: String = "",
    val consultationMethod: String = "",
    val chatRoomId: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)


val Questions = listOf(
    QuestionItem("Feeling restless, anxious, or extremely tense"),
    QuestionItem("Unable to stop or control worry"),
    QuestionItem("Worrying too much about things"),
    QuestionItem("Hard to relax"),
    QuestionItem("Very restless making it hard to sit still"),
    QuestionItem("Being easily irritated or irritable"),
    QuestionItem("Feeling afraid as if something terrible might happen")
)
