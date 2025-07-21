package com.example.todorevamp.util

sealed class UiEvent {
    object PopBackStack: UiEvent()

    data class Navigate(val route: String): UiEvent()

    data class ShowSnackBar(
        val message: String, // here there is no ? means if this object exist there must be a message compulsory
        val action: String? = null
    ): UiEvent()
}