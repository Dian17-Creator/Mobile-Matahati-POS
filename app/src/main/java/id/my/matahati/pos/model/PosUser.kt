package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class PosUser(
    @SerializedName("id") val id: Int,
    @SerializedName("nid_user") val nidUser: Int,
    @SerializedName("nid_outlet") val nidOutlet: Int,
    @SerializedName("name") val name: String,
    @SerializedName("username") val username: String
)

data class PosUserResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<PosUser>
)
