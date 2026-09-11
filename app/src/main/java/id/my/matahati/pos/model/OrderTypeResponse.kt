package id.my.matahati.pos.model

data class OrderTypeResponse(
    val success: Boolean,
    val data: List<OrderTypeItem>
)
