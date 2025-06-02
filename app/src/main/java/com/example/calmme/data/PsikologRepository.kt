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

    // Ambil semua data psikolog buat consultation
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

    // Ambil waktu konsul yang tersedia buat booking
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

    // Buat handle ketersediaan waktu konsul
    suspend fun handlePostAppointmentCreation(
        psychologistId: String,
        date: String,
        timeSlot: TimeSlot
    ): Resource<Unit> {
        return try {
            updateTimeSlotAvailability(psychologistId, date, timeSlot, false)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update time slot availability")
        }
    }

    // Buat update ketersediaan waktu konsul
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

    // Fungsi buat convert tanggal ke hari
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
            "monday"
        }
    }

    // Fungsi buat ambil data psikolog saat ini
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

    // Fungsi buat ambil schedule dari psikolog
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

    // Realtime litener buat update psikolog
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

    // Update data psikolog
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
            // Ambil data psikolog saat ini
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
                // Buat dokumen baru
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
                // Uodate dokumen yang ada
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

            // Check schedule ada atau belom
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
                // Update shceule yang sudah ada
                val existingDoc = existingScheduleSnapshot.documents.first()
                firestore.collection("schedules")
                    .document(existingDoc.id)
                    .update(scheduleData)
                    .await()
            } else {
                // Buat schedule baru
                val newData = scheduleData + mapOf("createdAt" to Timestamp.now())
                firestore.collection("schedules").add(newData).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update schedule")
        }
    }
}