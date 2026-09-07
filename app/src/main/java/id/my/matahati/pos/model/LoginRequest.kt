package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("cemail")
    val cemail: String,
    @SerializedName("cpassword")
    val cpassword: String
)
