package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class OutletItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
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
    val email: String? = null,
    @SerializedName("nid_outlet")
    val nidOutlet: String? = null,
    @SerializedName("outlet_name")
    val outletName: String? = null,
    @SerializedName("role_owner")
    val roleOwner: Boolean = false,
    @SerializedName("role_spv")
    val roleSpv: Boolean = false,
    @SerializedName("role_cashier")
    val roleCashier: Boolean = false,
    @SerializedName("role_captain")
    val roleCaptain: Boolean = false,
    @SerializedName("outlets")
    val outlets: List<OutletItem>? = emptyList()
)
