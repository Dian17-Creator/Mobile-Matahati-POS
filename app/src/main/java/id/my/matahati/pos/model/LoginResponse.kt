package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: LoginData?
)

data class LoginData(
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val user: UserDto
)

data class UserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("role_owner")
    val roleOwner: Boolean,
    @SerializedName("role_cashier")
    val roleCashier: Boolean,
    @SerializedName("role_captain")
    val roleCaptain: Boolean
)
