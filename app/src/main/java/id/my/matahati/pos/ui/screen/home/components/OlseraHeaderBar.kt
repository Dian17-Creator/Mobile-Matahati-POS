package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import id.my.matahati.pos.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val OlseraBlueHeader = Color(0xFF24BBCC)
val OlseraHeaderTabActive = Color(0xFF24BBCC)

@Composable
fun OlseraHeaderBar(
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    heldOrdersCount: Int = 0,
    onHeldOrdersClick: () -> Unit = {},
    selectedOrderType: String = "",
    onInAwayClick: () -> Unit = {},
    selectedTable: String = "",
    onTableClick: () -> Unit = {},
    selectedRightTab: String = "Produk",
    onRightTabSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = OlseraBlueHeader,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT PORTION
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(0.40f)
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                HeaderTabItem(
                    label = if (selectedTable.isBlank()) "Meja" else selectedTable,
                    isSelected = selectedTable.isNotBlank(),
                    onClick = onTableClick
                )
                HeaderTabItem(
                    label = if (selectedOrderType.isBlank()) "In/Aw" else selectedOrderType,
                    isSelected = selectedOrderType.isNotBlank(),
                    onClick = onInAwayClick
                )
                HeaderTabItem(label = "Pesanan", isSelected = false, onClick = {})
                HeaderTabItem(label = "Remark", isSelected = false, onClick = {})

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .padding(end = 8.dp, top = 8.dp)
                        .graphicsLayer(clip = false)
                ) {
                    IconButton(
                        onClick = onHeldOrdersClick,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .graphicsLayer(clip = false)
                                .offset(x = (-5).dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.sandwatch2),
                                contentDescription = "Pesanan Menggantung",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(20.dp)
                            )
                            if (heldOrdersCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Red,
                                    modifier = Modifier
                                        .offset(x = 8.dp, y = (-8).dp)
                                        .size(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = heldOrdersCount.toString(),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            lineHeight = 9.sp, // samakan dengan fontSize
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            style = LocalTextStyle.current.copy(
                                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                                            ),
                                            modifier = Modifier.wrapContentSize(Alignment.Center) // ganti dari fillMaxWidth
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // RIGHT PORTION
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .weight(0.60f)
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(end = 16.dp)
            ) {
                RightHeaderTabItem(
                    label = "Produk",
                    isSelected = selectedRightTab == "Produk",
                    onClick = { onRightTabSelected("Produk") },
                    modifier = Modifier.weight(1f)
                )
                RightHeaderTabItem(
                    label = "Barcode",
                    isSelected = selectedRightTab == "Barcode",
                    onClick = { onRightTabSelected("Barcode") },
                    modifier = Modifier.weight(1f)
                )
                RightHeaderTabItem(
                    label = "Custom/\nDeposit",
                    isSelected = selectedRightTab == "Custom",
                    onClick = { onRightTabSelected("Custom") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TransaksiHeaderBar(
    onMenuClick: () -> Unit,
    currentDate: String = "",
    onDateClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = OlseraBlueHeader,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, "Menu", tint = Color.White)
            }

            // Online Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Cloud, "Online", tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Online", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.weight(1f))

            val isPhone = LocalConfiguration.current.screenWidthDp < 600

            // Date & Calendar Filter
            if (isPhone) {
                IconButton(onClick = onDateClick) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Pilih Tanggal",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDateClick() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = currentDate,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Pilih Tanggal",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionHistoryHeader(
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    selectedPaymentType: String = "Semua Tipe Pembayaran",
    onPaymentTypeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isSearchActive by remember(searchQuery) { mutableStateOf(searchQuery.isNotBlank()) }

    Surface(
        color = OlseraBlueHeader,
        modifier = modifier.fillMaxWidth()
    ) {
        if (isSearchActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                        cursorBrush = SolidColor(Color.White),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Cari",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 15.sp
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.8f), thickness = 1.dp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = {
                        onSearchQueryChange("")
                        isSearchActive = false
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup Cari",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isSearchActive = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari Transaksi",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = selectedPaymentType,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPaymentTypeClick() }
                        .padding(horizontal = 8.dp)
                )

                IconButton(
                    onClick = onPaymentTypeClick
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.filter),
                        contentDescription = "Filter Pembayaran",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PengaturanHeaderBar(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = OlseraBlueHeader,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "Pengaturan",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HeaderTabItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) OlseraBlueHeader else Color.White
        )
    }
}

@Composable
private fun RightHeaderTabItem(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = Color.White
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth().height(3.dp).background(if (isSelected) Color.White else Color.Transparent)
        )
    }
}
