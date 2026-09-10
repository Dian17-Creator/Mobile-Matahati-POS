package id.my.matahati.pos.ui.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.my.matahati.pos.data.DummyData
import id.my.matahati.pos.data.remote.RetrofitClient
import id.my.matahati.pos.model.Category
import id.my.matahati.pos.model.Customer
import id.my.matahati.pos.model.Product
import id.my.matahati.pos.model.Voucher
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    val categories = mutableStateListOf<Category>()
    val products = mutableStateListOf<Product>()
    val customers = mutableStateListOf<Customer>()
    val vouchers = mutableStateListOf<Voucher>()

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Fetch Categories
                val catResponse = RetrofitClient.apiService.getCategories()
                if (catResponse.isSuccessful && catResponse.body()?.success == true) {
                    val dtos = catResponse.body()?.data ?: emptyList()
                    val fetchedCategories = mutableListOf<Category>(
                        Category(id = "all", name = "Semua Kategori", iconEmoji = "🏷️")
                    )
                    fetchedCategories.addAll(dtos.map { it.toCategory() })
                    categories.clear()
                    categories.addAll(fetchedCategories)
                }

                // Fetch Products
                val prodResponse = RetrofitClient.apiService.getProducts()
                if (prodResponse.isSuccessful && prodResponse.body()?.success == true) {
                    val dtos = prodResponse.body()?.data ?: emptyList()
                    val fetchedProducts = dtos.map { it.toProduct() }
                    products.clear()
                    products.addAll(fetchedProducts)
                }

                // Fetch Customers
                val custResponse = RetrofitClient.apiService.getCustomers()
                if (custResponse.isSuccessful && custResponse.body()?.success == true) {
                    val dtos = custResponse.body()?.data ?: emptyList()
                    val fetchedCustomers = dtos.map { it.toCustomer() }
                    customers.clear()
                    customers.addAll(fetchedCustomers)
                }

                // Fetch Vouchers
                val vouchResponse = RetrofitClient.apiService.getVouchers()
                if (vouchResponse.isSuccessful && vouchResponse.body()?.success == true) {
                    val dtos = vouchResponse.body()?.data ?: emptyList()
                    val fetchedVouchers = dtos.map { it.toVoucher() }
                    vouchers.clear()
                    vouchers.addAll(fetchedVouchers)
                }
            } catch (e: Exception) {
                errorMessage = "Gagal memuat data dari server: ${e.localizedMessage}"
                // Fallback to dummy data if network fails so POS is still usable
                if (categories.isEmpty()) {
                    categories.clear()
                    categories.addAll(DummyData.categories)
                }
                if (products.isEmpty()) {
                    products.clear()
                    products.addAll(DummyData.products)
                }
                if (customers.isEmpty()) {
                    customers.clear()
                    customers.add(Customer(id = "1", name = "A.N DITO", phone = "", email = "", address = ""))
                }
            } finally {
                isLoading = false
            }
        }
    }
}
