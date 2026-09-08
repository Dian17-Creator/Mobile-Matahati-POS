package id.my.matahati.pos.model

import com.google.gson.annotations.SerializedName

data class CategoryResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<CategoryDto>
)

data class CategoryDto(
    @SerializedName("nid")
    val nid: Int,
    @SerializedName("cname")
    val cname: String
) {
    fun toCategory(): Category {
        return Category(
            id = nid.toString(),
            name = cname,
            iconEmoji = getEmojiForCategory(cname)
        )
    }
}

private fun getEmojiForCategory(name: String): String {
    val lower = name.lowercase()
    return when {
        lower.contains("makanan") || lower.contains("food") -> "🍔"
        lower.contains("minuman") || lower.contains("drink") || lower.contains("kopi") || lower.contains("coffee") -> "☕"
        lower.contains("snack") || lower.contains("cemilan") -> "🍟"
        lower.contains("dessert") || lower.contains("cake") -> "🍰"
        else -> "🍽️"
    }
}

data class Category(
    val id: String,
    val name: String,
    val iconEmoji: String = "🏷️"
)
