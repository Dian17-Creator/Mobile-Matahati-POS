package id.my.matahati.pos.model

import java.util.UUID

data class HeldOrder(
    val id: String = UUID.randomUUID().toString(),
    val orderNumber: Int,
    val time: String,
    val tableName: String?,
    val customerName: String?,
    val orderType: String?,
    val cartItems: List<CartItem>,
    val subtotal: Double,
    val remark: String? = null
)