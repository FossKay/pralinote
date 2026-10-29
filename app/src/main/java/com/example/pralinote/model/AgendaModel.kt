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
        _agenda.update {
            it.copy(
                isLoading = true,
            )
        }
        viewModelScope.launch {
            try {
                val client = AppClient.instance!!
                // https://github.com/mozilla/uniffi-rs/issues/2953
                // Without this, ANR when launching 2 parallel requests
                val agenda = withContext(Dispatchers.IO) {
                    if (MOCK) client.mockAgenda() else client.agenda()
                }
                _agenda.update {
                    it.copy(
                        agenda = agenda,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _agenda.update {
                    it.copy(
                        isLoading = false,
                        error = e
                    )
                }
            }
            _agenda.update {
                it.copy(isLoading = false)
            }
        }
    }
}
