package com.example.calmme.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import com.example.calmme.commons.Resource
import java.text.SimpleDateFormat
import java.util.*

class PsikologRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Get all psychologists for consultation
    fun getAllPsychologists(): Flow<Resource<List<PsychologistData>>> = flow {
        try {
            emit(Resource.Loading())

            val querySnapshot = firestore.collection("psychologists")
                .whereEqualTo("isAvailable", true)
                .get()
                .await()

            val psychologists = querySnapshot.documents.mapNotNull { document ->
                document.toObject(PsychologistData::class.java)?.copy(
                    psychologistId = document.id
                )
            }

            emit(Resource.Success(psychologists))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load psychologists"))
        }
    }

    // Get psychologist by ID - TAMBAHAN BARU
    fun getPsychologistById(psychologistId: String): Flow<Resource<PsychologistData?>> = flow {
        try {
            emit(Resource.Loading())

            if (psychologistId.isEmpty()) {
                emit(Resource.Error("Psychologist ID is empty"))
                return@flow
            }

            val document = firestore.collection("psychologists")
                .document(psychologistId)
                .get()
                .await()

            val psychologist = if (document.exists()) {
                document.toObject(PsychologistData::class.java)?.copy(
                    psychologistId = document.id
                )
            } else {
                null
            }

            emit(Resource.Success(psychologist))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load psychologist"))
        }
    }

    // Get available time slots for psychologist - TAMBAHAN BARU
    fun getAvailableTimeSlots(
        psychologistId: String,
        date: String
    ): Flow<Resource<List<TimeSlot>>> = flow {
        try {
            emit(Resource.Loading())

            if (psychologistId.isEmpty()) {
                emit(Resource.Error("Psychologist ID is empty"))
                return@flow
            }

            // Get day of week from date
            val dayOfWeek = getDayOfWeekFromDate(date)

            val querySnapshot = firestore.collection("schedules")
                .whereEqualTo("psychologistId", psychologistId)
                .whereEqualTo("dayOfWeek", dayOfWeek)
                .get()
                .await()

            val timeSlots = if (!querySnapshot.isEmpty) {
                val schedule = querySnapshot.documents.first()
                    .toObject(ScheduleData::class.java)
                schedule?.timeSlots?.filter { it.isAvailable } ?: emptyList()
            } else {
                emptyList()
            }

            emit(Resource.Success(timeSlots))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load time slots"))
        }
    }

    // Book appointment - TAMBAHAN BARU
    // Di PsikologRepository.kt
    suspend fun handlePostAppointmentCreation( // Nama diubah agar lebih jelas
        psychologistId: String,
        date: String,
        timeSlot: TimeSlot
        // Anda mungkin tidak butuh userId dan consultationMethod di sini lagi
        // jika hanya untuk update ketersediaan slot
    ): Resource<Unit> { // Mungkin tidak perlu return ID lagi
        return try {
            // HANYA update time slot availability
            updateTimeSlotAvailability(psychologistId, date, timeSlot, false)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update time slot availability")
        }
    }

    // Helper function untuk update availability time slot
    private suspend fun updateTimeSlotAvailability(
        psychologistId: String,
        date: String,
        timeSlot: TimeSlot,
        isAvailable: Boolean
    ) {
        try {
            val dayOfWeek = getDayOfWeekFromDate(date)

            val querySnapshot = firestore.collection("schedules")
                .whereEqualTo("psychologistId", psychologistId)
                .whereEqualTo("dayOfWeek", dayOfWeek)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first()
                val schedule = document.toObject(ScheduleData::class.java)

                val updatedTimeSlots = schedule?.timeSlots?.map { slot ->
                    if (slot.startTime == timeSlot.startTime && slot.endTime == timeSlot.endTime) {
                        slot.copy(isAvailable = isAvailable)
                    } else {
                        slot
                    }
                } ?: emptyList()

                firestore.collection("schedules")
                    .document(document.id)
                    .update("timeSlots", updatedTimeSlots.map { slot ->
                        mapOf(
                            "startTime" to slot.startTime,
                            "endTime" to slot.endTime,
                            "isAvailable" to slot.isAvailable
                        )
                    })
                    .await()
            }
        } catch (e: Exception) {
            println("Failed to update time slot availability: ${e.message}")
        }
    }

    // Helper function untuk convert date ke day of week
    private fun getDayOfWeekFromDate(date: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateObj = sdf.parse(date)
            val calendar = Calendar.getInstance()
            calendar.time = dateObj!!

            when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "monday"
                Calendar.TUESDAY -> "tuesday"
                Calendar.WEDNESDAY -> "wednesday"
                Calendar.THURSDAY -> "thursday"
                Calendar.FRIDAY -> "friday"
                Calendar.SATURDAY -> "saturday"
                Calendar.SUNDAY -> "sunday"
                else -> "monday"
            }
        } catch (e: Exception) {
            "monday" // Default fallback
        }
    }

    // Get current user's psychologist data
    fun getCurrentPsychologistData(): Flow<Resource<PsychologistData?>> = flow {
        try {
            emit(Resource.Loading())

            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            val querySnapshot = firestore.collection("psychologists")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val psychologist = if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first()
                document.toObject(PsychologistData::class.java)?.copy(
                    psychologistId = document.id
                )
            } else {
                null
            }

            emit(Resource.Success(psychologist))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load psychologist data"))
        }
    }

    // Get schedules for a psychologist
    fun getSchedules(psychologistId: String): Flow<Resource<List<ScheduleData>>> = flow {
        try {
            emit(Resource.Loading())

            if (psychologistId.isEmpty()) {
                emit(Resource.Error("Psychologist ID is empty"))
                return@flow
            }

            val querySnapshot = firestore.collection("schedules")
                .whereEqualTo("psychologistId", psychologistId)
                .get()
                .await()

            val schedules = querySnapshot.documents.mapNotNull { document ->
                document.toObject(ScheduleData::class.java)?.copy(
                    scheduleId = document.id
                )
            }

            emit(Resource.Success(schedules))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load schedules"))
        }
    }

    // Real-time listener for psychologists (for consultation screen)
    fun getPsychologistsRealtime(): Flow<Resource<List<PsychologistData>>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = firestore.collection("psychologists")
            .whereEqualTo("isAvailable", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to listen for psychologists"))
                    return@addSnapshotListener
                }

                val psychologists = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(PsychologistData::class.java)?.copy(
                        psychologistId = document.id
                    )
                } ?: emptyList()

                trySend(Resource.Success(psychologists))
            }

        awaitClose { listener.remove() }
    }

    // Update psychologist data
    suspend fun updatePsychologistData(
        name: String,
        specialization: List<String>,
        description: String,
        experience: String,
        education: String,
        license: String
    ): Resource<PsychologistData> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            // Get current psychologist data
            val currentPsychologist = getCurrentPsychologistData().let { flow ->
                var result: PsychologistData? = null
                flow.collect { resource ->
                    if (resource is Resource.Success) {
                        result = resource.data
                    }
                }
                result
            }

            val updateData = mapOf(
                "name" to name,
                "specialization" to specialization,
                "description" to description,
                "experience" to experience,
                "education" to education,
                "license" to license,
                "updatedAt" to Timestamp.now()
            )

            val updatedPsychologist = if (currentPsychologist?.psychologistId?.isEmpty() != false) {
                // Create new psychologist document
                val newData = updateData + mapOf(
                    "userId" to userId,
                    "isAvailable" to true,
                    "createdAt" to Timestamp.now()
                )

                val docRef = firestore.collection("psychologists").add(newData).await()
                PsychologistData(
                    psychologistId = docRef.id,
                    userId = userId,
                    name = name,
                    specialization = specialization,
                    description = description,
                    experience = experience,
                    education = education,
                    license = license,
                    isAvailable = true
                )
            } else {
                // Update existing document
                firestore.collection("psychologists")
                    .document(currentPsychologist.psychologistId)
                    .update(updateData)
                    .await()

                currentPsychologist.copy(
                    name = name,
                    specialization = specialization,
                    description = description,
                    experience = experience,
                    education = education,
                    license = license
                )
            }

            Resource.Success(updatedPsychologist)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update psychologist data")
        }
    }

    // Update schedule
    suspend fun updateSchedule(
        psychologistId: String,
        dayOfWeek: String,
        timeSlots: List<TimeSlot>
    ): Resource<Unit> {
        return try {
            if (psychologistId.isEmpty()) {
                return Resource.Error("Psychologist data not found")
            }

            // Check if schedule exists for this day
            val existingScheduleSnapshot = firestore.collection("schedules")
                .whereEqualTo("psychologistId", psychologistId)
                .whereEqualTo("dayOfWeek", dayOfWeek)
                .get()
                .await()

            val scheduleData = mapOf(
                "psychologistId" to psychologistId,
                "dayOfWeek" to dayOfWeek,
                "timeSlots" to timeSlots.map { slot ->
                    mapOf(
                        "startTime" to slot.startTime,
                        "endTime" to slot.endTime,
                        "isAvailable" to slot.isAvailable
                    )
                },
                "isRecurring" to true,
                "updatedAt" to Timestamp.now()
            )

            if (!existingScheduleSnapshot.isEmpty) {
                // Update existing schedule
                val existingDoc = existingScheduleSnapshot.documents.first()
                firestore.collection("schedules")
                    .document(existingDoc.id)
                    .update(scheduleData)
                    .await()
            } else {
                // Create new schedule
                val newData = scheduleData + mapOf("createdAt" to Timestamp.now())
                firestore.collection("schedules").add(newData).await()
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update schedule")
        }
    }
}