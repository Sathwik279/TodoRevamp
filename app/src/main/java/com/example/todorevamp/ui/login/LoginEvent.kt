package com.example.todorevamp.ui.login

sealed class LoginEvent {
    object GoogleSignIn : LoginEvent()
}
