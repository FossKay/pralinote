package com.example.pralinote.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pralinote.view.dateToString
import com.example.pralinote.view.toWorkDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uniffi.pronote_rs.Home
import java.time.LocalDate

data class HomeState(
    val isLoading: Boolean = false,
    val home: Home? = null,
    val error: Throwable? = null,
)

class HomeModel : ViewModel() {
    private val _home = MutableStateFlow(HomeState())
    val home: StateFlow<HomeState> = _home.asStateFlow()

    fun loadHome() {
        if (_home.value.isLoading) {
            return
        }
        viewModelScope.launch {
            _home.update {
                it.copy(
                    isLoading = true,
                )
            }
            try {
                val client = AppClient.instance!!
                val date = dateToString(toWorkDate(LocalDate.now()))
                val home = if (MOCK) client.mockHome(date) else client.home(date)
                _home.update {
                    it.copy(home = home)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                _home.update {
                    it.copy(error = e)
                }
            }
            _home.update {
                it.copy(isLoading = false)
            }
        }
    }
}