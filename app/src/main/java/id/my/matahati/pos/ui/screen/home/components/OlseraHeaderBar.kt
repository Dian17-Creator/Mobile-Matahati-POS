package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val OlseraBlueHeader = Color(0xFF1565C0)
val OlseraHeaderTabActive = Color(0xFF1E88E5)

@Composable
fun OlseraHeaderBar(
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    selectedOrderType: String = "",
    onInAwayClick: () -> Unit = {},
    selectedRightTab: String = "Produk",
    onRightTabSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {

    Surface(
        color = OlseraBlueHeader,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // =========================================================
            // LEFT PORTION
            // Menu, Quick Tabs, Notification Bell
            // =========================================================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(0.40f)
                    .padding(
                        start = 12.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
            ) {

                // Menu
                IconButton(
                    onClick = onMenuClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                // Meja
                HeaderTabItem(
                    label = "Meja",
                    isSelected = false,
                    onClick = {}
                )

                // In / Away
                HeaderTabItem(
                    label = if (selectedOrderType.isBlank()) {
                        "In/Aw"
                    } else {
                        selectedOrderType
                    },
                    isSelected = selectedOrderType.isNotBlank(),
                    onClick = onInAwayClick
                )

                // Pesanan
                HeaderTabItem(
                    label = "Pesanan",
                    isSelected = false,
                    onClick = {}
                )

                // Remark
                HeaderTabItem(
                    label = "Remark",
                    isSelected = false,
                    onClick = {}
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                // =====================================================
                // NOTIFICATION
                // =====================================================
                Box(
                    modifier = Modifier.padding(end = 8.dp)
                ) {

                    // Bell Button
                    IconButton(
                        onClick = onNotificationClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifikasi",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // =========================================================
            // RIGHT PORTION
            // Produk, Barcode, Custom
            // =========================================================
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .weight(0.60f)
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(end = 16.dp)
            ) {

                // Produk
                RightHeaderTabItem(
                    label = "Produk",
                    isSelected = selectedRightTab == "Produk",
                    onClick = {
                        onRightTabSelected("Produk")
                    },
                    modifier = Modifier.weight(1f)
                )

                // Barcode
                RightHeaderTabItem(
                    label = "Barcode",
                    isSelected = selectedRightTab == "Barcode",
                    onClick = {
                        onRightTabSelected("Barcode")
                    },
                    modifier = Modifier.weight(1f)
                )

                // Custom
                RightHeaderTabItem(
                    label = "Custom",
                    isSelected = selectedRightTab == "Custom",
                    onClick = {
                        onRightTabSelected("Custom")
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


// =============================================================
// HEADER TAB KIRI
// =============================================================
@Composable
private fun HeaderTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .clip(
                RoundedCornerShape(12.dp)
            )
            .background(
                if (isSelected) {
                    Color.White
                } else {
                    Color.Transparent
                }
            )
            .border(
                width = 1.dp,
                color = if (isSelected) {
                    Color.Transparent
                } else {
                    Color.White.copy(alpha = 0.5f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 10.dp,
                vertical = 6.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            },
            color = if (isSelected) {
                OlseraBlueHeader
            } else {
                Color.White
            }
        )
    }
}


// =============================================================
// HEADER TAB KANAN
// =============================================================
@Composable
private fun RightHeaderTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable {
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
                color = Color.White
            )
        }

        // Underline tab aktif
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    if (isSelected) {
                        Color.White
                    } else {
                        Color.Transparent
                    }
                )
        )
    }
}