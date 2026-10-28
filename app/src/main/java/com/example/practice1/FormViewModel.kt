package com.example.practice1

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormViewModel: ViewModel() {
    private val uistatemutable = MutableStateFlow(FormUIState())
    val uistate = uistatemutable.asStateFlow()

    fun onEmailChanged(inputemail: String){
        val emailerror = if(inputemail.isNotBlank() && !inputemail.contains("@") && !inputemail.contains(".")){
            "email is not correct"
        }else{
            null
        }

        uistatemutable.update { currenstate ->
            val updatedstate = currenstate.copy(email = inputemail, emailError = emailerror)
            updatedstate.copy(isformValid =  isFormValid(updatedstate))
        }

    }


    fun onPasswordUpdated(inputpassword: String){
        val passworderror = if(inputpassword.isNotBlank() && inputpassword.length < 6){
            "password error"
        }else{
            null
        }

        uistatemutable.update { currentstate ->
            val updatedstate = currentstate.copy(password = inputpassword, passworderror = passworderror)
            updatedstate.copy(isformValid = isFormValid(updatedstate))
        }
    }

    fun isFormValid(formuistate: FormUIState): Boolean{
        val isemailvalid = formuistate.email.isNotBlank() && formuistate.emailError != null
        val ispasswordvalid = formuistate.password.isNotBlank() && formuistate.passworderror != null
        return isemailvalid && ispasswordvalid
    }

    fun submitForm(){
        if(uistatemutable.value.isformValid){
          uistatemutable.update { currentstatee ->
              currentstatee.copy(isSubmitted = true)
          }
        }
    }
}