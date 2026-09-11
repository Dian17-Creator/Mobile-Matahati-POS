package id.my.matahati.pos

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.matahati.pos.ui.screen.home.HomeScreen
import id.my.matahati.pos.ui.screen.login.LoginScreen
import id.my.matahati.pos.ui.screen.login.LoginViewModel
import id.my.matahati.pos.ui.theme.MobileMatahati_POSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPrefs = getSharedPreferences("pos_prefs", Context.MODE_PRIVATE)

        setContent {
            MobileMatahati_POSTheme {
                var token by remember {
                    mutableStateOf(sharedPrefs.getString("auth_token", null))
                }

                if (token.isNullOrBlank()) {
                    val loginViewModel: LoginViewModel = viewModel()
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = { newToken, user ->
                            sharedPrefs.edit()
                                .putString("auth_token", newToken)
                                .putString("user_name", user.name)
                                .putString("nid_outlet", user.nidOutlet)
                                .putBoolean("role_owner", user.roleOwner)
                                .putBoolean("role_cashier", user.roleCashier)
                                .putBoolean("role_captain", user.roleCaptain)
                                .apply()
                            token = newToken
                            id.my.matahati.pos.data.remote.RetrofitClient.authToken = newToken
                        }
                    )
                } else {
                    id.my.matahati.pos.data.remote.RetrofitClient.authToken = token
                    
                    val userName = sharedPrefs.getString("user_name", "Kasir") ?: "Kasir"
                    val nidOutlet = sharedPrefs.getString("nid_outlet", null)
                    val roleOwner = sharedPrefs.getBoolean("role_owner", false)
                    val roleCashier = sharedPrefs.getBoolean("role_cashier", false)
                    val roleCaptain = sharedPrefs.getBoolean("role_captain", false)

                    HomeScreen(
                        userName = userName,
                        nidOutlet = nidOutlet,
                        roleOwner = roleOwner,
                        roleCashier = roleCashier,
                        roleCaptain = roleCaptain,
                        onLogout = {
                            sharedPrefs.edit().clear().apply()
                            token = null
                        }
                    )
                }
            }
        }
    }
}
