package id.my.matahati.pos

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.my.matahati.pos.model.OutletItem
import id.my.matahati.pos.ui.screen.home.HomeScreen
import id.my.matahati.pos.ui.screen.login.LoginScreen
import id.my.matahati.pos.ui.screen.login.LoginViewModel
import id.my.matahati.pos.ui.theme.MobileMatahati_POSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPrefs = getSharedPreferences("pos_prefs", Context.MODE_PRIVATE)
        val gson = Gson()

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
                            val defaultOutletId = user.nidOutlet ?: user.outlets?.firstOrNull()?.id?.toString() ?: "1"
                            val defaultOutletName = user.outletName ?: user.outlets?.firstOrNull()?.name ?: ""
                            val outletsJson = gson.toJson(user.outlets ?: emptyList<OutletItem>())

                            sharedPrefs.edit()
                                .putString("auth_token", newToken)
                                .putString("user_id", user.id)
                                .putString("user_name", user.name)
                                .putString("nid_outlet", defaultOutletId)
                                .putString("outlet_name", defaultOutletName)
                                .putString("user_outlets", outletsJson)
                                .putBoolean("role_owner", user.roleOwner)
                                .putBoolean("role_spv", user.roleSpv)
                                .putBoolean("role_cashier", user.roleCashier)
                                .putBoolean("role_captain", user.roleCaptain)
                                .apply()
                            token = newToken
                            id.my.matahati.pos.data.remote.RetrofitClient.authToken = newToken
                        }
                    )
                } else {
                    id.my.matahati.pos.data.remote.RetrofitClient.authToken = token
                    
                    val userId = sharedPrefs.getString("user_id", null)
                    val userName = sharedPrefs.getString("user_name", "Kasir") ?: "Kasir"
                    val nidOutlet = sharedPrefs.getString("nid_outlet", null)
                    val savedOutletName = sharedPrefs.getString("outlet_name", null)
                    val roleOwner = sharedPrefs.getBoolean("role_owner", false)
                    val roleCashier = sharedPrefs.getBoolean("role_cashier", false)
                    val roleCaptain = sharedPrefs.getBoolean("role_captain", false)

                    val outletsJson = sharedPrefs.getString("user_outlets", null)
                    val userOutlets: List<OutletItem> = remember(outletsJson, nidOutlet, savedOutletName) {
                        if (!outletsJson.isNullOrBlank()) {
                            try {
                                val type = object : TypeToken<List<OutletItem>>() {}.type
                                gson.fromJson(outletsJson, type) ?: emptyList()
                            } catch (e: Exception) {
                                emptyList()
                            }
                        } else if (!nidOutlet.isNullOrBlank() && !savedOutletName.isNullOrBlank()) {
                            val idInt = nidOutlet.toIntOrNull() ?: 1
                            listOf(OutletItem(id = idInt, name = savedOutletName))
                        } else {
                            emptyList()
                        }
                    }

                    HomeScreen(
                        userId = userId,
                        userName = userName,
                        nidOutlet = nidOutlet,
                        outletName = savedOutletName,
                        userOutlets = userOutlets,
                        roleOwner = roleOwner,
                        roleCashier = roleCashier,
                        roleCaptain = roleCaptain,
                        onLogout = {
                            sharedPrefs.edit()
                                .remove("auth_token")
                                .remove("user_id")
                                .remove("user_name")
                                .remove("nid_outlet")
                                .remove("outlet_name")
                                .remove("user_outlets")
                                .remove("role_owner")
                                .remove("role_spv")
                                .remove("role_cashier")
                                .remove("role_captain")
                                .apply()
                            token = null
                        }
                    )
                }
            }
        }
    }
}
