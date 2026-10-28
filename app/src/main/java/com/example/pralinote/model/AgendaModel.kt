package com.example.pralinote.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uniffi.pronote_rs.Agenda

data class AgendaState(
    val isLoading: Boolean = false,
    val agenda: Agenda? = null,
    val error: Throwable? = null,
)

class AgendaModel : ViewModel() {
    private val _agenda = MutableStateFlow(AgendaState())
    val agenda: StateFlow<AgendaState> = _agenda.asStateFlow()

    fun loadAgenda() {
        if (_agenda.value.isLoading) {
            return
        }
        viewModelScope.launch {
            _agenda.update {
                it.copy(
                    isLoading = true,
                )
            }
            try {
                val client = AppClient.instance!!
                val agenda = if (MOCK) client.mockAgenda() else client.agenda()
                _agenda.update {
                    it.copy(agenda = agenda)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                _agenda.update {
                    it.copy(error = e)
                }
            }
            _agenda.update {
                it.copy(isLoading = false)
            }
        }
    }
}
