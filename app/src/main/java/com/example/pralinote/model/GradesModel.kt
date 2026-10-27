package com.example.pralinote.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uniffi.pronote_rs.Grades
import uniffi.pronote_rs.Period

data class GradesState(
    val loading: List<Period> = emptyList(),
    val timetable: Map<Period, Grades> = emptyMap(),
    val error: Map<Period, Throwable?> = emptyMap(),
)

class GradesModel : ViewModel() {
    private val _grades = MutableStateFlow(GradesState())
    val grades: StateFlow<GradesState> = _grades.asStateFlow()

    fun loadPeriod(period: Period) {
        if (period in _grades.value.loading) {
            return
        }
        viewModelScope.launch {
            _grades.update {
                it.copy(loading = _grades.value.loading + period)
            }
            try {
                val client = AppClient.instance!!
                val grades = if (MOCK) client.mockGrades(period) else client.grades(period)
                _grades.update {
                    it.copy(timetable = _grades.value.timetable + (period to grades))
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                _grades.update {
                    it.copy(error = _grades.value.error + (period to e))
                }
            }
            _grades.update {
                it.copy(loading = _grades.value.loading - period)
            }
        }
    }
}
