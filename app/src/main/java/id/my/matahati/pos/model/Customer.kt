package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class CustomerTypeResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<CustomerTypeDto>
)

data class CustomerTypeDto(
    @SerializedName("nid")
    val nid: Int,
    @SerializedName("cname")
    val cname: String
)

data class CustomerResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<CustomerDto>
)

data class SingleCustomerResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: CustomerDto? = null
)

data class CustomerDto(
    @SerializedName("nid")
    val nid: Int? = null,
    @SerializedName("nid_type")
    val nidType: Int? = null,
    @SerializedName("type")
    val type: CustomerTypeDto? = null,
    @SerializedName("cname")
    val cname: String,
    @SerializedName("cphone")
    val cphone: String? = null,
    @SerializedName("cemail")
    val cemail: String? = null,
    @SerializedName("caddress")
    val caddress: String? = null,
    @SerializedName("customer_type")
    val customerType: String? = null,
    @SerializedName("cgender")
    val gender: String? = null,
    @SerializedName("cmembership_no")
    val membershipNo: String? = null,
    @SerializedName("dbirth")
    val birthDate: String? = null,
    @SerializedName("cnotes")
    val notes: String? = null,
    @SerializedName("cpostal_code")
    val postalCode: String? = null,
    @SerializedName("ccountry")
    val country: String? = null,
    @SerializedName("cprovince")
    val province: String? = null,
    @SerializedName("ccity")
    val city: String? = null,
    @SerializedName("cdistrict")
    val district: String? = null
) {
    fun toCustomer(): Customer {
        return Customer(
            id = (nid ?: System.currentTimeMillis()).toString(),
            nidType = nidType ?: type?.nid,
            name = cname,
            phone = cphone ?: "",
            email = cemail ?: "",
            address = caddress ?: "",
            customerType = type?.cname ?: customerType ?: "Guest",
            gender = gender ?: "",
            membershipNo = membershipNo ?: "",
            birthDate = birthDate ?: "",
            notes = notes ?: "",
            postalCode = postalCode ?: "",
            country = country ?: "Indonesia",
            province = province ?: "",
            city = city ?: "",
            district = district ?: ""
        )
    }
}

data class CreateCustomerRequest(
    @SerializedName("nid_type") val nidType: Int? = null,
    @SerializedName("cname") val name: String,
    @SerializedName("cphone") val phone: String? = null,
    @SerializedName("cemail") val email: String? = null,
    @SerializedName("caddress") val address: String? = null,
    @SerializedName("customer_type") val customerType: String? = null,
    @SerializedName("cgender") val gender: String? = null,
    @SerializedName("cmembership_no") val membershipNo: String? = null,
    @SerializedName("dbirth") val birthDate: String? = null,
    @SerializedName("cnotes") val notes: String? = null,
    @SerializedName("cpostal_code") val postalCode: String? = null,
    @SerializedName("ccountry") val country: String? = null,
    @SerializedName("cprovince") val province: String? = null,
    @SerializedName("ccity") val city: String? = null,
    @SerializedName("cdistrict") val district: String? = null
)

data class Customer(
    val id: String,
    val nidType: Int? = null,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val customerType: String = "Guest",
    val gender: String = "",
    val membershipNo: String = "",
    val birthDate: String = "",
    val notes: String = "",
    val postalCode: String = "",
    val country: String = "Indonesia",
    val province: String = "",
    val city: String = "",
    val district: String = ""
)
