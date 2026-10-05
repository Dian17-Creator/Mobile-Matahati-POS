package id.my.matahati.pos.ui.screen.home.components

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.data.remote.District
import id.my.matahati.pos.data.remote.Province
import id.my.matahati.pos.data.remote.Regency
import id.my.matahati.pos.model.CreateCustomerRequest
import id.my.matahati.pos.model.Customer
import id.my.matahati.pos.model.CustomerTypeDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val PanelBlueHeader = Color(0xFF24BBCC)
private val PanelBgColor = Color(0xFFE0F7FA)

@Composable
fun AddCustomerPanel(
    customerToEdit: Customer? = null,
    customerTypes: List<CustomerTypeDto> = emptyList(),
    provinces: List<Province> = emptyList(),
    regencies: List<Regency> = emptyList(),
    districts: List<District> = emptyList(),
    isProvincesLoading: Boolean = false,
    isRegenciesLoading: Boolean = false,
    isDistrictsLoading: Boolean = false,
    regionError: String? = null,
    onLoadProvinces: () -> Unit = {},
    onSelectProvince: (Province) -> Unit = {},
    onSelectRegency: (Regency) -> Unit = {},
    onClose: () -> Unit,
    onSubmit: (request: CreateCustomerRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Profil, 1 = Alamat

    // Load provinces when panel is initialized or when Alamat tab is selected
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1 && provinces.isEmpty()) {
            onLoadProvinces()
        }
    }

    // Compute display customer types with fallback if API is empty
    val displayCustomerTypes = remember(customerTypes) {
        if (customerTypes.isNotEmpty()) {
            customerTypes
        } else {
            listOf(
                CustomerTypeDto(1, "Guest"),
                CustomerTypeDto(2, "Member"),
                CustomerTypeDto(3, "VIP"),
                CustomerTypeDto(4, "Regular")
            )
        }
    }

    var selectedCustomerType by remember(customerToEdit, displayCustomerTypes) {
        mutableStateOf<CustomerTypeDto?>(
            if (customerToEdit != null) {
                displayCustomerTypes.find {
                    it.nid == customerToEdit.nidType || it.cname.equals(customerToEdit.customerType, ignoreCase = true)
                } ?: CustomerTypeDto(customerToEdit.nidType ?: 1, customerToEdit.customerType)
            } else {
                displayCustomerTypes.find { it.cname.equals("Guest", ignoreCase = true) }
                    ?: displayCustomerTypes.firstOrNull()
            }
        )
    }

    // Form State - Profil (prefilled if editing)
    var nameInput by remember(customerToEdit) { mutableStateOf(customerToEdit?.name ?: "") }
    var genderInput by remember(customerToEdit) {
        mutableStateOf(
            when {
                customerToEdit?.gender.equals("MALE", ignoreCase = true) -> "Laki-laki"
                customerToEdit?.gender.equals("FEMALE", ignoreCase = true) -> "Perempuan"
                else -> customerToEdit?.gender?.ifEmpty { "Gender" } ?: "Gender"
            }
        )
    }
    var membershipNoInput by remember(customerToEdit) { mutableStateOf(customerToEdit?.membershipNo ?: "") }
    var emailInput by remember(customerToEdit) { mutableStateOf(customerToEdit?.email ?: "") }
    var phoneInput by remember(customerToEdit) { mutableStateOf(customerToEdit?.phone ?: "") }
    var birthDateInput by remember(customerToEdit) {
        mutableStateOf(
            customerToEdit?.birthDate?.ifEmpty { "01 Jan 1900" } ?: "01 Jan 1900"
        )
    }
    var notesInput by remember(customerToEdit) { mutableStateOf(customerToEdit?.notes ?: "") }

    // Form State - Alamat (prefilled if editing)
    var addressInput by remember(customerToEdit) { mutableStateOf(customerToEdit?.address ?: "") }
    var postalCodeInput by remember(customerToEdit) { mutableStateOf(customerToEdit?.postalCode ?: "") }
    val countryInput = remember(customerToEdit) { customerToEdit?.country?.ifEmpty { "Indonesia" } ?: "Indonesia" }

    var selectedProvince by remember(customerToEdit) {
        mutableStateOf<Province?>(
            customerToEdit?.province?.takeIf { it.isNotBlank() }?.let { Province("", it) }
        )
    }

    var selectedRegency by remember(customerToEdit) {
        mutableStateOf<Regency?>(
            customerToEdit?.city?.takeIf { it.isNotBlank() }?.let { Regency("", "", it) }
        )
    }

    var selectedDistrict by remember(customerToEdit) {
        mutableStateOf<District?>(
            customerToEdit?.district?.takeIf { it.isNotBlank() }?.let { District("", "", it) }
        )
    }

    // Mode edit cascading: Province -> Regencies
    LaunchedEffect(customerToEdit, provinces) {
        val targetProvName = customerToEdit?.province
        if (!targetProvName.isNullOrBlank() && provinces.isNotEmpty()) {
            val matched = provinces.find { it.name.equals(targetProvName, ignoreCase = true) }
            if (matched != null) {
                selectedProvince = matched
                onSelectProvince(matched)
            }
        }
    }

    // Mode edit cascading: Regency -> Districts
    LaunchedEffect(customerToEdit, regencies) {
        val targetCityName = customerToEdit?.city
        if (!targetCityName.isNullOrBlank() && regencies.isNotEmpty()) {
            val matched = regencies.find { it.name.equals(targetCityName, ignoreCase = true) }
            if (matched != null) {
                selectedRegency = matched
                onSelectRegency(matched)
            }
        }
    }

    // Mode edit cascading: District
    LaunchedEffect(customerToEdit, districts) {
        val targetDistrictName = customerToEdit?.district
        if (!targetDistrictName.isNullOrBlank() && districts.isNotEmpty()) {
            val matched = districts.find { it.name.equals(targetDistrictName, ignoreCase = true) }
            if (matched != null) {
                selectedDistrict = matched
            }
        }
    }

    // Dropdown & Picker States
    var showCustomerTypeDialog by remember { mutableStateOf(false) }
    var showGenderDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var showProvinceDialog by remember { mutableStateOf(false) }
    var showRegencyDialog by remember { mutableStateOf(false) }
    var showDistrictDialog by remember { mutableStateOf(false) }

    val genderOptions = listOf("Laki-laki", "Perempuan")

    // Date Picker Dialog Logic
    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        try {
            val parsedDate = dateFormatter.parse(birthDateInput)
            if (parsedDate != null) {
                calendar.time = parsedDate
            }
        } catch (_: Exception) {}

        DisposableEffect(Unit) {
            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    }
                    birthDateInput = dateFormatter.format(selectedCal.time)
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.setOnDismissListener {
                showDatePicker = false
            }
            datePickerDialog.show()
            onDispose {
                if (datePickerDialog.isShowing) {
                    datePickerDialog.dismiss()
                }
            }
        }
    }

    // Popup Selection Dialogs
    if (showCustomerTypeDialog) {
        SelectionOptionDialog(
            title = "Pilih Tipe Pelanggan",
            items = displayCustomerTypes,
            itemLabel = { it.cname },
            onItemSelected = { selectedCustomerType = it },
            onDismissRequest = { showCustomerTypeDialog = false }
        )
    }

    if (showGenderDialog) {
        SelectionOptionDialog(
            title = "Pilih Gender",
            items = genderOptions,
            itemLabel = { it },
            onItemSelected = { genderInput = it },
            onDismissRequest = { showGenderDialog = false }
        )
    }

    if (showProvinceDialog) {
        SelectionOptionDialog(
            title = "Pilih Provinsi",
            items = provinces,
            itemLabel = { it.name },
            isLoading = isProvincesLoading,
            emptyMessage = "Daftar provinsi belum tersedia. Klik muat ulang pada form.",
            onItemSelected = { prov ->
                selectedProvince = prov
                selectedRegency = null
                selectedDistrict = null
                onSelectProvince(prov)
            },
            onDismissRequest = { showProvinceDialog = false }
        )
    }

    if (showRegencyDialog) {
        SelectionOptionDialog(
            title = "Pilih Kota/Kabupaten",
            items = regencies,
            itemLabel = { it.name },
            isLoading = isRegenciesLoading,
            emptyMessage = "Daftar kota belum tersedia.",
            onItemSelected = { reg ->
                selectedRegency = reg
                selectedDistrict = null
                onSelectRegency(reg)
            },
            onDismissRequest = { showRegencyDialog = false }
        )
    }

    if (showDistrictDialog) {
        SelectionOptionDialog(
            title = "Pilih Kecamatan",
            items = districts,
            itemLabel = { it.name },
            isLoading = isDistrictsLoading,
            emptyMessage = "Daftar kecamatan belum tersedia.",
            onItemSelected = { dist ->
                selectedDistrict = dist
            },
            onDismissRequest = { showDistrictDialog = false }
        )
    }

    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxHeight()
            .widthIn(min = 650.dp, max = 700.dp)
            .imePadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar with Tabs & Close 'X' Button
            Surface(
                color = PanelBlueHeader,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(start = 8.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tabs: Profil & Alamat matching OlseraHeaderBar
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        PanelHeaderTabItem(
                            label = "Profil",
                            isSelected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        PanelHeaderTabItem(
                            label = "Alamat",
                            isSelected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Close Button 'X'
                    IconButton(onClick = onClose) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.25f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup Panel",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(25.dp),
                verticalArrangement = Arrangement.spacedBy(35.dp)
            ) {
                if (selectedTab == 0) {
                    // TAB 1: PROFIL
                    // Tipe Pelanggan (Boxed Button)
                    CustomDropdownField(
                        label = "Tipe Pelanggan",
                        value = selectedCustomerType?.cname ?: "Guest",
                        onClick = { showCustomerTypeDialog = true }
                    )

                    // Nama (Floating Label Underline Input)
                    CustomInputField(
                        label = "Nama",
                        value = nameInput,
                        onValueChange = { nameInput = it }
                    )

                    // Gender (Boxed Button)
                    CustomDropdownField(
                        label = "Gender",
                        value = genderInput,
                        onClick = { showGenderDialog = true }
                    )

                    // No. Keanggotaan (Floating Label Underline Input)
                    CustomInputField(
                        label = "No. Keanggotaan",
                        value = membershipNoInput,
                        onValueChange = { membershipNoInput = it }
                    )

                    // Email (Floating Label Underline Input)
                    CustomInputField(
                        label = "Email",
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        keyboardType = KeyboardType.Email
                    )

                    // Telpon (Floating Label Underline Input)
                    CustomInputField(
                        label = "Telpon",
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        keyboardType = KeyboardType.Phone
                    )

                    // Tanggal Lahir (Boxed Button Date Picker)
                    CustomDropdownField(
                        label = "Tanggal Lahir",
                        value = birthDateInput,
                        onClick = { showDatePicker = true }
                    )

                    // Catatan (Floating Label Underline Input)
                    CustomInputField(
                        label = "Catatan",
                        value = notesInput,
                        onValueChange = { notesInput = it }
                    )
                } else {
                    // TAB 2: ALAMAT
                    if (!regionError.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = regionError,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    CustomInputField(
                        label = "Alamat",
                        value = addressInput,
                        onValueChange = { addressInput = it }
                    )

                    CustomInputField(
                        label = "Kode Pos",
                        value = postalCodeInput,
                        onValueChange = { postalCodeInput = it },
                        keyboardType = KeyboardType.Number
                    )

                    // Negara (Static Boxed Button)
                    CustomDropdownField(
                        label = "Negara",
                        value = countryInput,
                        enabled = false,
                        onClick = { }
                    )

                    // Provinsi (Boxed Button)
                    CustomDropdownField(
                        label = "Negara Bagian/Provinsi",
                        value = selectedProvince?.name ?: "Pilih Provinsi",
                        isLoading = isProvincesLoading,
                        onClick = {
                            if (provinces.isEmpty() && !isProvincesLoading) {
                                onLoadProvinces()
                            }
                            showProvinceDialog = true
                        }
                    )

                    // Kota / Kabupaten (Boxed Button)
                    CustomDropdownField(
                        label = "Kota",
                        value = selectedRegency?.name ?: if (selectedProvince == null) "Pilih Provinsi terlebih dahulu" else "Pilih Kota/Kabupaten",
                        enabled = selectedProvince != null,
                        isLoading = isRegenciesLoading,
                        onClick = {
                            if (selectedProvince != null) {
                                if (regencies.isEmpty() && !isRegenciesLoading) {
                                    onSelectProvince(selectedProvince!!)
                                }
                                showRegencyDialog = true
                            }
                        }
                    )

                    // Kecamatan (Boxed Button)
                    CustomDropdownField(
                        label = "Kecamatan",
                        value = selectedDistrict?.name ?: if (selectedRegency == null) "Pilih Kota terlebih dahulu" else "Pilih Kecamatan",
                        enabled = selectedRegency != null,
                        isLoading = isDistrictsLoading,
                        onClick = {
                            if (selectedRegency != null) {
                                if (districts.isEmpty() && !isDistrictsLoading) {
                                    onSelectRegency(selectedRegency!!)
                                }
                                showDistrictDialog = true
                            }
                        }
                    )
                }
            }

            // Bottom Action: Simpan / Simpan Perubahan Button
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        val genderValue = when (genderInput) {
                            "Laki-laki" -> "MALE"
                            "Perempuan" -> "FEMALE"
                            "Gender" -> null
                            else -> genderInput.ifEmpty { null }
                        }
                        val req = CreateCustomerRequest(
                            nidType = selectedCustomerType?.nid,
                            name = nameInput.trim(),
                            phone = phoneInput.trim().ifEmpty { null },
                            email = emailInput.trim().ifEmpty { null },
                            address = addressInput.trim().ifEmpty { null },
                            customerType = selectedCustomerType?.cname ?: "Guest",
                            gender = genderValue,
                            membershipNo = membershipNoInput.trim().ifEmpty { null },
                            birthDate = birthDateInput.trim().ifEmpty { null },
                            notes = notesInput.trim().ifEmpty { null },
                            postalCode = postalCodeInput.trim().ifEmpty { null },
                            country = countryInput,
                            province = selectedProvince?.name,
                            city = selectedRegency?.name,
                            district = selectedDistrict?.name
                        )
                        onSubmit(req)
                    }
                },
                enabled = nameInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PanelBlueHeader),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = if (customerToEdit != null) "Simpan Perubahan" else "Simpan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun <T> SelectionOptionDialog(
    title: String,
    items: List<T>,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    onDismissRequest: () -> Unit,
    isLoading: Boolean = false,
    emptyMessage: String = "Tidak ada pilihan"
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = PanelBlueHeader
            )
        },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PanelBlueHeader)
                }
            } else if (items.isEmpty()) {
                Text(
                    text = emptyMessage,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    items(items) { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onItemSelected(item)
                                    onDismissRequest()
                                },
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = itemLabel(item),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF222222)
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Tutup", color = PanelBlueHeader, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun PanelHeaderTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(if (isSelected) Color.White else Color.Transparent)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val colors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = PanelBlueHeader,
        unfocusedIndicatorColor = PanelBlueHeader.copy(alpha = 0.6f),
        focusedLabelColor = PanelBlueHeader,
        unfocusedLabelColor = PanelBlueHeader.copy(alpha = 0.7f),
        focusedTextColor = Color(0xFF222222),
        unfocusedTextColor = Color(0xFF222222)
    )

    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        interactionSource = interactionSource,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = Color(0xFF222222)),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = @Composable { innerTextField ->
            TextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
                interactionSource = interactionSource,
                label = {
                    Text(
                        text = label,
                        fontSize = 14.sp
                    )
                },
                colors = colors,
                contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 4.dp, bottom = 4.dp)
            )
        }
    )
}

@Composable
private fun CustomDropdownField(
    label: String,
    value: String,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(2.dp),
        color = if (enabled) Color.White.copy(alpha = 0.6f) else Color(0xFFE8E8E8),
        border = BorderStroke(1.dp, if (enabled) PanelBlueHeader.copy(alpha = 0.6f) else Color(0xFFB0BEC5)),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(enabled = enabled && !isLoading) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Label di pojok kiri atas
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = PanelBlueHeader,
                modifier = Modifier.align(Alignment.TopStart)
            )

            // Teks Nilai di Tengah (Center)
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center),
                    strokeWidth = 2.dp,
                    color = PanelBlueHeader
                )
            } else {
                Text(
                    text = value.ifEmpty { label },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) PanelBlueHeader else Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
