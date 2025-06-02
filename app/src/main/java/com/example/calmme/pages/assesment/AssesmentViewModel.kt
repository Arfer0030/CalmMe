package com.example.calmme.pages.assesment

import androidx.lifecycle.ViewModel
import com.example.calmme.data.Questions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AssesmentViewModel : ViewModel() {
    private val _questions = MutableStateFlow(Questions)
    val questions: StateFlow<List<QuestionItem>> = _questions

    // StateFlow untuk status submit
    private val _canSubmit = MutableStateFlow(false)
    val canSubmit: StateFlow<Boolean> = _canSubmit

    fun updateAnswer(index: Int, answer: Int) {
        val updatedList = _questions.value.toMutableList()
        updatedList[index] = updatedList[index].copy(answer = answer)
        _questions.value = updatedList

        // Update status submit setiap ada jawaban yang berubah
        _canSubmit.value = areAllQuestionsAnswered()
    }

    // Fungsi untuk mengecek semua soal sudah dijawab atau belom
    fun areAllQuestionsAnswered(): Boolean {
        return _questions.value.all { it.answer != -1 }
    }

    // Fungsi untuk dapet jumlah soal yang belum dijawab
    fun getUnansweredQuestionsCount(): Int {
        return _questions.value.count { it.answer == -1 }
    }

    // Fungsi untuk reset jawaban
    fun resetAllAnswers() {
        val resetList = _questions.value.map { it.copy(answer = -1) }
        _questions.value = resetList
        _canSubmit.value = false
    }

    // Fungsi untuk bagian progressbar
    fun getProgress(): Float {
        val answered = _questions.value.count { it.answer != -1 }
        val total = _questions.value.size
        return if (total == 0) 0f else answered / total.toFloat()
    }

    // Fungsi untuk total skor
    fun getTotalScore(): Int = _questions.value.sumOf { it.answer.coerceAtLeast(0) }

    // Perhitungan skor dan kategorinya
    fun getResultCategory(score: Int): String = when (score) {
        in 0..4 -> "Minimal Anxiety"
        in 5..9 -> "Mild Anxiety"
        in 10..14 -> "Moderate Anxiety"
        in 15..21 -> "Severe Anxiety"
        else -> "Error"
    }
}
