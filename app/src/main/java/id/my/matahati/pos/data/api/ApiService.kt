package id.my.matahati.pos.data.api

import id.my.matahati.pos.model.VoucherResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    
    @POST("api/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @GET("api/vouchers")
    suspend fun getVouchers(): Response<VoucherResponse>
    
}
