package com.example.todorevamp.ui.login

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todorevamp.repository.AuthRepository
import com.example.todorevamp.util.UiEvent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.GoogleSignIn -> {
                // This will be handled in the UI when launching the Google Sign-In intent
            }
        }
    }

    fun handleGoogleSignInResult(result: ActivityResult) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                
                account.idToken?.let { idToken ->
                    val authResult = authRepository.signInWithGoogle(idToken)
                    
                    if (authResult.isSuccess) {
                        _uiEvent.send(UiEvent.Navigate("todo_list"))
                    } else {
                        val errorMessage = authResult.exceptionOrNull()?.message ?: "Sign in failed"
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Authentication failed: $errorMessage"
                        )
                    }
                } ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No ID token received from Google"
                    )
                }
            } catch (e: ApiException) {
                val errorMessage = when (e.statusCode) {
                    7 -> "Network error - Check your internet connection"
                    12501 -> "Sign in cancelled"
                    12502 -> "Sign in currently in progress"
                    12500 -> "Sign in failed - Check your configuration"
                    else -> "Google Sign-In failed: ${e.message} (Code: ${e.statusCode})"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    fun getGoogleSignInClient() = authRepository.getGoogleSignInClient()

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
