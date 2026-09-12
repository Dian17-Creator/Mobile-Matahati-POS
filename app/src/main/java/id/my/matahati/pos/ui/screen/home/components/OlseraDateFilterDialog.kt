package id.my.matahati.pos.ui.screen.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

data class DateFilterResult(
    val displayLabel: String,
    val startDate: String,
    val endDate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OlseraDateFilterDialog(
    onDismiss: () -> Unit,
    onDateSelected: (DateFilterResult) -> Unit
) {
    val options = listOf("Hari Ini", "Kemarin", "Bulan Ini", "Bulan Lalu", "Pilih Tanggal")
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val displaySdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                        val apiSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val date = Date(millis)
                        val formatted = displaySdf.format(date)
                        val apiDate = apiSdf.format(date)
                        
                        onDateSelected(DateFilterResult(formatted, apiDate, apiDate))
                        onDismiss()
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = OlseraBlueHeader, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("BATAL", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(380.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Header with Title and Close Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "Filter Tanggal",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OlseraBlueHeader,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.Red,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                HorizontalDivider()

                options.forEach { option ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (option == "Pilih Tanggal") {
                                    showDatePicker = true
                                } else {
                                    val result = calculateDateRange(option)
                                    onDateSelected(result)
                                    onDismiss()
                                }
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = OlseraBlueHeader,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (option != options.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = Color.LightGray.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

private fun calculateDateRange(option: String): DateFilterResult {
    val calendar = Calendar.getInstance()
    val localeId = Locale("id", "ID")
    val displaySdf = SimpleDateFormat("dd MMM yyyy", localeId)
    val displaySdfMonth = SimpleDateFormat("dd MMM", localeId)
    val apiSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    return when (option) {
        "Hari Ini" -> {
            val dateStr = apiSdf.format(calendar.time)
            DateFilterResult(displaySdf.format(calendar.time), dateStr, dateStr)
        }
        "Kemarin" -> {
            calendar.add(Calendar.DATE, -1)
            val dateStr = apiSdf.format(calendar.time)
            DateFilterResult(displaySdf.format(calendar.time), dateStr, dateStr)
        }
        "Bulan Ini" -> {
            val start = calendar.clone() as Calendar
            start.set(Calendar.DAY_OF_MONTH, 1)
            
            val end = calendar.clone() as Calendar
            end.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            
            DateFilterResult(
                "${displaySdfMonth.format(start.time)} - ${displaySdf.format(end.time)}",
                apiSdf.format(start.time),
                apiSdf.format(end.time)
            )
        }
        "Bulan Lalu" -> {
            calendar.add(Calendar.MONTH, -1)
            val start = calendar.clone() as Calendar
            start.set(Calendar.DAY_OF_MONTH, 1)
            
            val end = calendar.clone() as Calendar
            end.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            
            DateFilterResult(
                "${displaySdfMonth.format(start.time)} - ${displaySdf.format(end.time)}",
                apiSdf.format(start.time),
                apiSdf.format(end.time)
            )
        }
        else -> {
            val dateStr = apiSdf.format(calendar.time)
            DateFilterResult(displaySdf.format(calendar.time), dateStr, dateStr)
        }
    }
}
