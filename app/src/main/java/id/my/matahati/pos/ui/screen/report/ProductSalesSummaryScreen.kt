package id.my.matahati.pos.ui.screen.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.ui.screen.home.components.OlseraDateFilterDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val OlseraBlueHeader = Color(0xFF1565C0)
private val GreenSubHeader = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSalesSummaryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDateFilterDialog by remember { mutableStateOf(false) }

    val today = remember { Date() }
    val displayHeaderSdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")) }
    val displayDaySdf = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.forLanguageTag("id-ID")) }

    var selectedHeaderDateText by remember { mutableStateOf(displayHeaderSdf.format(today)) }
    var selectedDayItems by remember { mutableStateOf(listOf(displayDaySdf.format(today))) }

    Scaffold(
        topBar = {
            Surface(
                color = OlseraBlueHeader,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Back Arrow
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }

                    // Middle: Date Text
                    Text(
                        text = selectedHeaderDateText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Right: Calendar Filter Icon
                    IconButton(onClick = { showDateFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Filter Tanggal",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // Sub-Header Bar "Tanggal"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GreenSubHeader)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Tanggal",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Date List Content
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(selectedDayItems) { dayText ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                /* Action when clicking individual day row */
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = dayText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF424242)
                            )
                        }

                        HorizontalDivider(
                            color = Color.LightGray.copy(alpha = 0.4f),
                            thickness = 0.8.dp
                        )
                    }
                }
            }
        }
    }

    // Date Filter Dialog
    if (showDateFilterDialog) {
        OlseraDateFilterDialog(
            onDismiss = { showDateFilterDialog = false },
            onDateSelected = { result ->
                selectedHeaderDateText = result.displayLabel
                selectedDayItems = generateDailyItemList(result.startDate, result.endDate)
            }
        )
    }
}

private fun generateDailyItemList(startDateStr: String, endDateStr: String): List<String> {
    val items = mutableListOf<String>()
    try {
        val apiSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val displayDaySdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.forLanguageTag("id-ID"))

        val startDate = apiSdf.parse(startDateStr) ?: Date()
        val endDate = apiSdf.parse(endDateStr) ?: startDate

        val calendar = Calendar.getInstance().apply { time = startDate }
        val endCalendar = Calendar.getInstance().apply { time = endDate }

        while (!calendar.after(endCalendar)) {
            items.add(displayDaySdf.format(calendar.time))
            calendar.add(Calendar.DATE, 1)
        }
    } catch (_: Exception) {
        items.add(startDateStr)
    }
    return if (items.isEmpty()) listOf(startDateStr) else items
}
