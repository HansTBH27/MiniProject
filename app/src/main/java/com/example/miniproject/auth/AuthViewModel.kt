package com.example.miniproject.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel(){
    private val repository = AuthRepository()

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    fun signIn(email: String, password: String){
        viewModelScope.launch{
            try{
                val authResult = repository.signIn(email, password)
                println("Sign in successful")
                authResult.user?.uid?.let { fetchUserData(it) }
                // TODO: Handle successful sign in UI and UI state.
            }catch(e: Exception){
                println("Sign in failed: ${e.message}")
                // TODO: Handle failure sign in UI and UI state.
            }
        }
    }
    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                val authResult = repository.signUp(email, password, name)
                println("Sign-up successful!")
                authResult.user?.uid?.let { fetchUserData(it) }
                // TODO: Update UI state and maybe navigate.

            } catch (e: Exception) {
                println("Sign-up failed: ${e.message}")
                // TODO: Update UI state with the error.
            }
        }
    }


    private fun fetchUserData(uid: String) {
        viewModelScope.launch {
            try {
                _userData.value = repository.getUserData(uid)
                println("User data fetched: ${_userData.value}")
            } catch (e: Exception) {
                println("Failed to fetch user data: ${e.message}")
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _userData.value = null
        println("User signed out.")
        // TODO: Update UI state and navigate to the login screen.
    }


}
