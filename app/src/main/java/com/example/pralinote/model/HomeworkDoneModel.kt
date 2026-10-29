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

data class HomeworkDoneState(
    val loading: List<String> = emptyList(),
    val done: Map<String, Boolean> = emptyMap(),
    val error: Map<String, Throwable?> = emptyMap(),
)

class HomeworkDoneModel : ViewModel() {
    private val _homeworkDone = MutableStateFlow(HomeworkDoneState())
    val homeworkDone: StateFlow<HomeworkDoneState> = _homeworkDone.asStateFlow()

    fun setDone(id: String, done: Boolean) {
        if (id in _homeworkDone.value.loading) {
            return
        }
        viewModelScope.launch {
            _homeworkDone.update {
                it.copy(loading = it.loading + id)
            }
            try {
                val client = AppClient.instance!!
                withContext(Dispatchers.IO) {
                    if (MOCK) client.mockSetDone(id, done)
                    else client.setDone(id, done)
                }
                _homeworkDone.update {
                    it.copy(done = it.done + (id to done))
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                _homeworkDone.update {
                    it.copy(error = it.error + (id to e))
                }
            }
            _homeworkDone.update {
                it.copy(loading = it.loading - id)
            }
        }
    }
}