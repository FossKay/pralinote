package com.example.pralinote.model

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.pralinote.view.getPraliColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ThemeState(
    val color: Color = getPraliColor(null)
)

class ThemeModel : ViewModel() {
    private val _state = MutableStateFlow(ThemeState())
    val state = _state.asStateFlow()

    fun setColor(color: String?) {
        _state.update { it.copy(color = getPraliColor(color)) }
    }
}