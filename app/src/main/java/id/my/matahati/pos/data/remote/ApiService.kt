package id.my.matahati.pos.data.remote

import id.my.matahati.pos.model.LoginRequest
import id.my.matahati.pos.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
