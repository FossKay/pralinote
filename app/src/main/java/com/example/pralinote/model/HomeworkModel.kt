package com.example.pralinote.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pralinote.view.dateToString
import com.example.pralinote.view.getDate
import com.example.pralinote.view.nextWorkDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.pronote_rs.Homework
import java.time.LocalDate

data class HomeworkState(
    val isLoading: Boolean = false,
    val homework: List<Homework>? = null,
    val error: Throwable? = null,
)

class HomeworkModel : ViewModel() {
    private val _homework = MutableStateFlow<HomeworkState>(HomeworkState())
    val homework: StateFlow<HomeworkState> = _homework.asStateFlow()

    fun loadHomework(lastDate: LocalDate) {
        if (_homework.value.isLoading) {
            return
        }
        viewModelScope.launch {
            _homework.update {
                it.copy(isLoading = true)
            }
            try {
                val client = AppClient.instance!!
                val nextDate = nextWorkDate(lastDate)
                val firstWeek = client.getWeek(dateToString(nextDate))
                val homework = withContext(Dispatchers.IO) {
                    if (MOCK) client.mockHomework(
                        firstWeek, client.params().lastWeek
                    ) else client.homework(firstWeek, client.params().lastWeek)
                }
                _homework.update {
                    it.copy(homework = homework.filter { homework -> getDate(homework.dueDate) > lastDate })
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                _homework.update {
                    it.copy(error = e)
                }
            }
            _homework.update {
                it.copy(isLoading = false)
            }
        }
    }
}
