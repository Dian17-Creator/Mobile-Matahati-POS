package id.my.matahati.pos.data.remote

import id.my.matahati.pos.model.CategoryResponse
import id.my.matahati.pos.model.CustomerResponse
import id.my.matahati.pos.model.LoginRequest
import id.my.matahati.pos.model.LoginResponse
import id.my.matahati.pos.model.OrderTypeResponse
import id.my.matahati.pos.model.PaymentMethodResponse
import id.my.matahati.pos.model.ProductResponse
import id.my.matahati.pos.model.VoucherResponse
import id.my.matahati.pos.model.CurrentShiftResponse
import id.my.matahati.pos.model.ShiftDetailResponse
import id.my.matahati.pos.model.ShiftHistoryResponse
import id.my.matahati.pos.model.CashMovementResponse
import id.my.matahati.pos.model.StartShiftRequest
import id.my.matahati.pos.model.CashMovementRequest
import id.my.matahati.pos.model.CloseShiftRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/categories")
    suspend fun getCategories(): Response<CategoryResponse>

    @GET("api/products")
    suspend fun getProducts(
        @Query("nid_outlet") outletId: String? = null
    ): Response<ProductResponse>

    @GET("api/customers")
    suspend fun getCustomers(): Response<CustomerResponse>

    @GET("api/vouchers")
    suspend fun getVouchers(): Response<VoucherResponse>

    @GET("api/payment-methods")
    suspend fun getPaymentMethods(): Response<PaymentMethodResponse>

    @GET("api/pos-users")
    suspend fun getPosUsers(
        @Query("nid_outlet") outletId: Int? = null
    ): Response<id.my.matahati.pos.model.PosUserResponse>

    @GET("api/pos/order-types")
    suspend fun getOrderTypes(): Response<OrderTypeResponse>

    @POST("api/pos/transactions")
    suspend fun submitTransaction(@Body request: id.my.matahati.pos.model.TransactionRequest): Response<id.my.matahati.pos.model.TransactionResponse>

    @GET("api/pos/transactions")
    suspend fun getTransactions(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("search") search: String? = null,
        @Query("nid_outlet") outletId: String? = null,
        @Query("nid_payment") paymentId: String? = null
    ): Response<id.my.matahati.pos.model.TransactionHistoryResponse>

    @DELETE("api/pos/transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String): Response<id.my.matahati.pos.model.TransactionResponse>

    // Shift Endpoints
    @GET("api/shifts/current")
    suspend fun getCurrentShift(
        @Query("nid_outlet") outletId: String?
    ): Response<CurrentShiftResponse>

    @POST("api/shifts/start")
    suspend fun startShift(
        @Body request: StartShiftRequest
    ): Response<CurrentShiftResponse>

    @POST("api/shifts/cash-movement")
    suspend fun addCashMovement(
        @Body request: CashMovementRequest
    ): Response<CashMovementResponse>

    @POST("api/shifts/{id_shift}/close")
    suspend fun closeShift(
        @Path("id_shift") shiftId: String,
        @Body request: CloseShiftRequest
    ): Response<CurrentShiftResponse>

    @GET("api/shifts/history")
    suspend fun getShiftHistory(
        @Query("nid_outlet") outletId: String?,
        @Query("page") page: Int = 1
    ): Response<ShiftHistoryResponse>

    @GET("api/shifts/{id_shift}")
    suspend fun getShiftDetail(
        @Path("id_shift") shiftId: String
    ): Response<ShiftDetailResponse>
}
