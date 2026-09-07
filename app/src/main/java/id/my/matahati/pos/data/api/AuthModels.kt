package id.my.matahati.pos.data.api

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    @SerializedName("cemail") val email: String,
    @SerializedName("cpassword") val password: String
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    @SerializedName("role_owner") val roleOwner: Boolean,
    @SerializedName("role_cashier") val roleCashier: Boolean,
    @SerializedName("role_captain") val roleCaptain: Boolean
)

data class AuthData(
    val token: String,
    val user: User
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData?
)
