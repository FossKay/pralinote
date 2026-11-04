package com.example.pralinote.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.pronote_rs.Grades

data class GradesState(
    val loading: List<String> = emptyList(),
    val grades: Map<String, Grades> = emptyMap(),
    val error: Map<String, Throwable?> = emptyMap(),
)

class GradesModel : ViewModel() {
    private val _grades = MutableStateFlow(GradesState())
    val grades: StateFlow<GradesState> = _grades.asStateFlow()

    fun loadPeriod(period: String) {
        if (period in _grades.value.loading) {
            return
        }
        viewModelScope.launch {
            _grades.update {
                it.copy(loading = _grades.value.loading + period)
            }
            try {
                val client = AppClient.instance!!
                val periodObject = AppClient.userParams!!.student.periods.periods.first {
                    it.id == period
                }
                val grades = withContext(Dispatchers.IO) {
                    if (MOCK) client.mockGrades(periodObject) else client.grades(periodObject)
                }
                _grades.update {
                    it.copy(grades = _grades.value.grades + (period to grades))
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
