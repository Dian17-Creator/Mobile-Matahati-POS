package id.my.matahati.pos.data.remote

import id.my.matahati.pos.model.CategoryResponse
import id.my.matahati.pos.model.CustomerResponse
import id.my.matahati.pos.model.LoginRequest
import id.my.matahati.pos.model.LoginResponse
import id.my.matahati.pos.model.PaymentMethodResponse
import id.my.matahati.pos.model.ProductResponse
import id.my.matahati.pos.model.VoucherResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/categories")
    suspend fun getCategories(): Response<CategoryResponse>

    @GET("api/products")
    suspend fun getProducts(): Response<ProductResponse>

    @GET("api/customers")
    suspend fun getCustomers(): Response<CustomerResponse>

    @GET("api/vouchers")
    suspend fun getVouchers(): Response<VoucherResponse>

    @GET("api/payment-methods")
    suspend fun getPaymentMethods(): Response<PaymentMethodResponse>
}
