package com.example.practice1

data class FormUIState(val email: String ="",
    val emailError: String?=null,
    val password: String = "",
    val passworderror: String? = null,
    val isformValid: Boolean = false,
    val isSubmitted: Boolean = false)
