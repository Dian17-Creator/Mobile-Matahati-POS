package id.my.matahati.pos.ui.screen.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.pos.data.remote.RetrofitClient
import id.my.matahati.pos.model.LoginRequest
import id.my.matahati.pos.model.UserDto
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val token: String, val user: UserDto) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var uiState by mutableStateOf<LoginUiState>(LoginUiState.Idle)
        private set

    fun login(onSuccess: (String, UserDto) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            uiState = LoginUiState.Error("Email dan password tidak boleh kosong")
            return
        }

        uiState = LoginUiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        uiState = LoginUiState.Success(body.data.token, body.data.user)
                        onSuccess(body.data.token, body.data.user)
                    } else {
                        uiState = LoginUiState.Error(body?.message ?: "Login gagal")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (!errorBody.isNullOrBlank()) {
                        try {
                            val json = JSONObject(errorBody)
                            json.optString("message", "Login gagal")
                        } catch (e: Exception) {
                            "Login gagal (${response.code()})"
                        }
                    } else {
                        "Login gagal (${response.code()})"
                    }
                    uiState = LoginUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                uiState = LoginUiState.Error("Koneksi gagal: ${e.localizedMessage}")
            }
        }
    }
}
