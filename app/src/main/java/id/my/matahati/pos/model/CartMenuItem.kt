package id.my.matahati.pos.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.vector.ImageVector
import id.my.matahati.pos.R

enum class CartMenuItem(
    val key: String,
    val title: String,
    val description: String,
    val icon: ImageVector = Icons.Default.Person,
    val iconResId: Int? = null,
    val isDestructive: Boolean = false,
    val defaultEnabled: Boolean = true
) {
    DISC_PESANAN(
        key = "cart_menu_disc_pesanan",
        title = "Disc. Pesanan",
        description = "Diskon untuk seluruh pesanan",
        icon = Icons.Default.LocalOffer
    ),
    ONGKOS_KIRIM(
        key = "cart_menu_ongkos_kirim",
        title = "Ongkos Kirim",
        description = "Biaya pengiriman pesanan",
        icon = Icons.Default.LocalShipping
    ),
    CATATAN_PESANAN(
        key = "cart_menu_catatan_pesanan",
        title = "Catatan Pesanan",
        description = "Catatan tambahan untuk pesanan",
        icon = Icons.Default.ChatBubbleOutline
    ),
    DILAYANI_OLEH(
        key = "cart_menu_dilayani_oleh",
        title = "Dilayani Oleh",
        description = "Informasi staf/kasir yang melayani",
        iconResId = R.drawable.ic_pos_profile
    ),
    KIRIM_KE_DAPUR(
        key = "cart_menu_kirim_dapur",
        title = "Kirim ke Dapur",
        description = "Kirim pesanan ke printer dapur/bar",
        icon = Icons.Default.Restaurant
    ),
    BATAL_PESANAN(
        key = "cart_menu_batal_pesanan",
        title = "Batal Pesanan",
        description = "Membatalkan seluruh pesanan di keranjang",
        icon = Icons.Default.Close,
        isDestructive = true
    ),
    CETAK_PERIKSA(
        key = "cart_menu_cetak_periksa",
        title = "Cetak Periksa",
        description = "Cetak struk periksa pesanan sementara",
        icon = Icons.Default.Print
    ),
    TEBUS_POINT(
        key = "cart_menu_tebus_point",
        title = "Tebus Point",
        description = "Penukaran poin pelanggan",
        icon = Icons.Default.Redeem
    ),
    SHARE_PESANAN(
        key = "cart_menu_share_pesanan",
        title = "Share Pesanan",
        description = "Bagikan ringkasan pesanan",
        icon = Icons.Default.Share
    ),
    REFERRAL_PELANGGAN(
        key = "cart_menu_referral_pelanggan",
        title = "Referral Pelanggan",
        description = "Program referral pelanggan",
        icon = Icons.Default.GroupAdd
    );

    companion object {
        fun fromKey(key: String): CartMenuItem? {
            return entries.find { it.key == key }
        }
    }
}
