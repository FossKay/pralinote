package com.example.pralinote.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uniffi.pronote_rs.Lesson

data class TimetableState(
    val loading: List<UByte> = emptyList(),
    val timetable: Map<UByte, List<List<Lesson>>> = emptyMap(),
    val error: Map<UByte, Throwable?> = emptyMap(),
)

class TimetableModel : ViewModel() {
    private val _timetable = MutableStateFlow(TimetableState())
    val timetable: StateFlow<TimetableState> = _timetable.asStateFlow()

    fun loadWeek(week: UByte) {
        if (week in _timetable.value.loading) {
            return
        }
        viewModelScope.launch {
            _timetable.update {
                it.copy(loading = _timetable.value.loading + week)
            }
            try {
                val client = AppClient.instance!!
                val lessons = if (MOCK) client.mockTimetable(week) else client.timetable(week)
                _timetable.update {
                    it.copy(timetable = _timetable.value.timetable + (week to lessons))
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                _timetable.update {
                    it.copy(error = _timetable.value.error + (week to e))
                }
            }
            _timetable.update {
                it.copy(loading = _timetable.value.loading - week)
            }
        }
    }
}
