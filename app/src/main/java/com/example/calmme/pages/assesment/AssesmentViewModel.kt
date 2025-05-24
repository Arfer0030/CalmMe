package com.example.calmme.pages.assesment

import androidx.lifecycle.ViewModel
import com.example.calmme.data.Questions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AssesmentViewModel : ViewModel() {
    private val _questions = MutableStateFlow(Questions)
    val questions: StateFlow<List<QuestionItem>> = _questions

    // Tambahkan StateFlow untuk status submit
    private val _canSubmit = MutableStateFlow(false)
    val canSubmit: StateFlow<Boolean> = _canSubmit

    fun updateAnswer(index: Int, answer: Int) {
        val updatedList = _questions.value.toMutableList()
        updatedList[index] = updatedList[index].copy(answer = answer)
        _questions.value = updatedList

        // Update status submit setiap kali ada jawaban yang berubah
        _canSubmit.value = areAllQuestionsAnswered()
    }

    // Fungsi untuk mengecek apakah semua soal sudah dijawab
    fun areAllQuestionsAnswered(): Boolean {
        return _questions.value.all { it.answer != -1 }
    }

    // Fungsi untuk mendapatkan jumlah soal yang belum dijawab
    fun getUnansweredQuestionsCount(): Int {
        return _questions.value.count { it.answer == -1 }
    }

    // Fungsi untuk reset semua jawaban
    fun resetAllAnswers() {
        val resetList = _questions.value.map { it.copy(answer = -1) }
        _questions.value = resetList
        _canSubmit.value = false
    }

    fun getProgress(): Float {
        val answered = _questions.value.count { it.answer != -1 }
        val total = _questions.value.size
        return if (total == 0) 0f else answered / total.toFloat()
    }

    fun getTotalScore(): Int = _questions.value.sumOf { it.answer.coerceAtLeast(0) }

    fun getResultCategory(score: Int): String = when (score) {
        in 0..4 -> "Kecemasan Normal"
        in 5..9 -> "Gejala Ringan"
        in 10..14 -> "Gejala Sedang"
        in 15..21 -> "Gejala Berat"
        else -> "Tidak diketahui"
    }
}
