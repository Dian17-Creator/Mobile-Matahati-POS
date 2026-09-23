package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class PosUser(
    @SerializedName("id") val id: Int,
    @SerializedName("nid_user") val nidUser: Int,
    @SerializedName("nid_outlet") val nidOutlet: Int,
    @SerializedName("name") val name: String,
    @SerializedName("username") val username: String,
    @SerializedName("role") val role: String? = null,
    @SerializedName("role_owner") val roleOwner: Boolean? = false,
    @SerializedName("role_cashier") val roleCashier: Boolean? = false,
    @SerializedName("role_captain") val roleCaptain: Boolean? = false
) {
    val displayRole: String
        get() {
            if (!role.isNullOrBlank()) return role
            if (roleOwner == true) return "Owner"
            if (roleCaptain == true) return "Captain"
            if (roleCashier == true) return "Cashier"
            return "Cashier"
        }
}

data class PosUserResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<PosUser>
)
