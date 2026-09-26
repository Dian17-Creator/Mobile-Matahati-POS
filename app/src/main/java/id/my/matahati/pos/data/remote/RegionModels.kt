package id.my.matahati.pos.data.remote

import com.google.gson.annotations.SerializedName

data class Province(
    @SerializedName("id") val id: String,
    @SerializedName(value = "name", alternate = ["nama"]) val name: String
)

data class Regency(
    @SerializedName("id") val id: String,
    @SerializedName(value = "province_id", alternate = ["id_provinsi", "provinceId"]) val provinceId: String? = null,
    @SerializedName(value = "name", alternate = ["nama"]) val name: String
)

data class District(
    @SerializedName("id") val id: String,
    @SerializedName(value = "regency_id", alternate = ["id_kabupaten", "regencyId"]) val regencyId: String? = null,
    @SerializedName(value = "name", alternate = ["nama"]) val name: String
)
