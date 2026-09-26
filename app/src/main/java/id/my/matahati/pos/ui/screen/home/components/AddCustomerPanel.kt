package id.my.matahati.pos.ui.screen.home.components

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import java.util.Date
import java.util.Locale

private val PanelBlueHeader = Color(0xFF1565C0)
private val PanelBgColor = Color(0xFFEBF5FE)

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
                else -> customerToEdit?.gender?.ifEmpty { "Laki-laki" } ?: "Laki-laki"
            }
        )
    }
    var membershipNoInput by remember(customerToEdit) { mutableStateOf(customerToEdit?.membershipNo ?: "") }
    var emailInput by remember(customerToEdit) { mutableStateOf(customerToEdit?.email ?: "") }
    var phoneInput by remember(customerToEdit) { mutableStateOf(customerToEdit?.phone ?: "") }
    var birthDateInput by remember(customerToEdit) {
        mutableStateOf(
            customerToEdit?.birthDate?.ifEmpty { dateFormatter.format(Date()) }
                ?: dateFormatter.format(Date())
        )
    }
    var notesInput by remember(customerToEdit) { mutableStateOf(customerToEdit?.notes ?: "") }

    // Form State - Alamat (prefilled if editing)
    var addressInput by remember(customerToEdit) { mutableStateOf(customerToEdit?.address ?: "") }
    var postalCodeInput by remember(customerToEdit) { mutableStateOf(customerToEdit?.postalCode ?: "") }
    val countryInput = remember(customerToEdit) { customerToEdit?.country?.ifEmpty { "Indonesia" } ?: "Indonesia" }

    var selectedProvince by remember(customerToEdit, provinces) {
        mutableStateOf<Province?>(
            customerToEdit?.province?.takeIf { it.isNotBlank() }?.let { provName ->
                provinces.find { it.name.equals(provName, ignoreCase = true) } ?: Province("", provName)
            }
        )
    }

    var selectedRegency by remember(customerToEdit, regencies) {
        mutableStateOf<Regency?>(
            customerToEdit?.city?.takeIf { it.isNotBlank() }?.let { cityName ->
                regencies.find { it.name.equals(cityName, ignoreCase = true) } ?: Regency("", "", cityName)
            }
        )
    }

    var selectedDistrict by remember(customerToEdit, districts) {
        mutableStateOf<District?>(
            customerToEdit?.district?.takeIf { it.isNotBlank() }?.let { distName ->
                districts.find { it.name.equals(distName, ignoreCase = true) } ?: District("", "", distName)
            }
        )
    }

    // Trigger region loading if customerToEdit has province or city
    LaunchedEffect(customerToEdit) {
        if (customerToEdit != null) {
            if (customerToEdit.province.isNotBlank()) {
                val prov = provinces.find { it.name.equals(customerToEdit.province, ignoreCase = true) }
                if (prov != null) {
                    onSelectProvince(prov)
                }
            }
        }
    }

    // Dropdown & Picker States
    var showCustomerTypeDropdown by remember { mutableStateOf(false) }
    var showGenderDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var showProvinceDropdown by remember { mutableStateOf(false) }
    var showRegencyDropdown by remember { mutableStateOf(false) }
    var showDistrictDropdown by remember { mutableStateOf(false) }

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

    Surface(
        color = PanelBgColor,
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
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tabs: Profil & Alamat
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable { selectedTab = 0 }
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Profil",
                                fontSize = 16.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) Color.White else Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .clickable { selectedTab = 1 }
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Alamat",
                                fontSize = 16.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) Color.White else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == 0) {
                    // TAB 1: PROFIL
                    // Tipe Pelanggan (Dropdown)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CustomDropdownField(
                            label = "Tipe Pelanggan",
                            value = selectedCustomerType?.cname ?: "Guest",
                            onClick = { showCustomerTypeDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showCustomerTypeDropdown,
                            onDismissRequest = { showCustomerTypeDropdown = false }
                        ) {
                            displayCustomerTypes.forEach { typeDto ->
                                DropdownMenuItem(
                                    text = { Text(typeDto.cname) },
                                    onClick = {
                                        selectedCustomerType = typeDto
                                        showCustomerTypeDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Nama
                    CustomInputField(
                        label = "Nama",
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        placeholder = "Masukkan Nama"
                    )

                    // Gender (Dropdown)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CustomDropdownField(
                            label = "Gender",
                            value = genderInput,
                            onClick = { showGenderDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showGenderDropdown,
                            onDismissRequest = { showGenderDropdown = false }
                        ) {
                            genderOptions.forEach { gen ->
                                DropdownMenuItem(
                                    text = { Text(gen) },
                                    onClick = {
                                        genderInput = gen
                                        showGenderDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // No. Keanggotaan
                    CustomInputField(
                        label = "No. Keanggotaan",
                        value = membershipNoInput,
                        onValueChange = { membershipNoInput = it },
                        placeholder = "Masukkan No. Keanggotaan"
                    )

                    // Email
                    CustomInputField(
                        label = "Email",
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        placeholder = "Masukkan Email",
                        keyboardType = KeyboardType.Email
                    )

                    // Telpon
                    CustomInputField(
                        label = "Telpon",
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        placeholder = "Masukkan No. Telpon",
                        keyboardType = KeyboardType.Phone
                    )

                    // Tanggal Lahir (Date Picker)
                    CustomDropdownField(
                        label = "Tanggal Lahir",
                        value = birthDateInput,
                        onClick = { showDatePicker = true }
                    )

                    // Catatan
                    CustomInputField(
                        label = "Catatan",
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        placeholder = "Masukkan Catatan"
                    )
                } else {
                    // TAB 2: ALAMAT
                    CustomInputField(
                        label = "Alamat",
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        placeholder = "Masukkan Alamat Lengkap"
                    )

                    CustomInputField(
                        label = "Kode Pos",
                        value = postalCodeInput,
                        onValueChange = { postalCodeInput = it },
                        placeholder = "Masukkan Kode Pos",
                        keyboardType = KeyboardType.Number
                    )

                    // Negara (Static/Hardcoded)
                    CustomDropdownField(
                        label = "Negara",
                        value = countryInput,
                        enabled = false,
                        onClick = { }
                    )

                    // Provinsi (Dependent Dropdown 1)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CustomDropdownField(
                            label = "Negara Bagian/Provinsi",
                            value = selectedProvince?.name ?: "Pilih Provinsi",
                            isLoading = isProvincesLoading,
                            onClick = {
                                if (provinces.isEmpty() && !isProvincesLoading) {
                                    onLoadProvinces()
                                }
                                showProvinceDropdown = true
                            }
                        )
                        DropdownMenu(
                            expanded = showProvinceDropdown,
                            onDismissRequest = { showProvinceDropdown = false }
                        ) {
                            if (provinces.isEmpty()) {
                                if (isProvincesLoading) {
                                    DropdownMenuItem(
                                        text = { Text("Memuat provinsi...") },
                                        onClick = {}
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text("Klik untuk muat ulang provinsi") },
                                        onClick = {
                                            onLoadProvinces()
                                        }
                                    )
                                }
                            } else {
                                provinces.forEach { prov ->
                                    DropdownMenuItem(
                                        text = { Text(prov.name) },
                                        onClick = {
                                            selectedProvince = prov
                                            selectedRegency = null
                                            selectedDistrict = null
                                            onSelectProvince(prov)
                                            showProvinceDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Kota / Kabupaten (Dependent Dropdown 2)
                    Box(modifier = Modifier.fillMaxWidth()) {
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
                                    showRegencyDropdown = true
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = showRegencyDropdown,
                            onDismissRequest = { showRegencyDropdown = false }
                        ) {
                            if (regencies.isEmpty()) {
                                if (isRegenciesLoading) {
                                    DropdownMenuItem(
                                        text = { Text("Memuat kota/kabupaten...") },
                                        onClick = {}
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text("Klik untuk muat ulang kota") },
                                        onClick = {
                                            selectedProvince?.let { onSelectProvince(it) }
                                        }
                                    )
                                }
                            } else {
                                regencies.forEach { reg ->
                                    DropdownMenuItem(
                                        text = { Text(reg.name) },
                                        onClick = {
                                            selectedRegency = reg
                                            selectedDistrict = null
                                            onSelectRegency(reg)
                                            showRegencyDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Kecamatan (Dependent Dropdown 3)
                    Box(modifier = Modifier.fillMaxWidth()) {
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
                                    showDistrictDropdown = true
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = showDistrictDropdown,
                            onDismissRequest = { showDistrictDropdown = false }
                        ) {
                            if (districts.isEmpty()) {
                                if (isDistrictsLoading) {
                                    DropdownMenuItem(
                                        text = { Text("Memuat kecamatan...") },
                                        onClick = {}
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text("Klik untuk muat ulang kecamatan") },
                                        onClick = {
                                            selectedRegency?.let { onSelectRegency(it) }
                                        }
                                    )
                                }
                            } else {
                                districts.forEach { dist ->
                                    DropdownMenuItem(
                                        text = { Text(dist.name) },
                                        onClick = {
                                            selectedDistrict = dist
                                            showDistrictDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Action: Simpan / Simpan Perubahan Button
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        val genderValue = when (genderInput) {
                            "Laki-laki" -> "MALE"
                            "Perempuan" -> "FEMALE"
                            else -> genderInput
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
private fun CustomInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF555555),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.Gray) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = PanelBlueHeader,
                unfocusedBorderColor = Color(0xFFB0BEC5)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CustomDropdownField(
    label: String,
    value: String,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF555555),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (enabled) Color.White else Color(0xFFF0F0F0),
            border = BorderStroke(1.dp, if (enabled) Color(0xFFB0BEC5) else Color(0xFFE0E0E0)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled && !isLoading) { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value.ifEmpty { label },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) Color(0xFF222222) else Color.Gray
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = PanelBlueHeader
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (enabled) Color.Gray else Color.LightGray
                    )
                }
            }
        }
    }
}
