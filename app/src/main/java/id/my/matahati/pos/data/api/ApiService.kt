package id.my.matahati.pos.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    
    @POST("api/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>
    
}
