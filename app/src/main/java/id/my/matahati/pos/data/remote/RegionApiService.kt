package id.my.matahati.pos.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface RegionApiService {
    @GET("provinsi.json")
    suspend fun getProvinces(): List<Province>

    @GET("kabupaten/{province_id}.json")
    suspend fun getRegencies(@Path("province_id") provinceId: String): List<Regency>

    @GET("kecamatan/{regency_id}.json")
    suspend fun getDistricts(@Path("regency_id") regencyId: String): List<District>
}

interface SecondaryRegionApiService {
    @GET("provinces.json")
    suspend fun getProvinces(): List<Province>

    @GET("regencies/{province_id}.json")
    suspend fun getRegencies(@Path("province_id") provinceId: String): List<Regency>

    @GET("districts/{regency_id}.json")
    suspend fun getDistricts(@Path("regency_id") regencyId: String): List<District>
}

object RegionRetrofitClient {
    private const val PRIMARY_BASE_URL = "https://ibnux.github.io/data-indonesia/"
    private const val SECONDARY_BASE_URL = "https://raw.githubusercontent.com/emsifa/api-wilayah-indonesia/main/api/"

    private val isDebug: Boolean by lazy {
        try {
            val clazz = Class.forName("id.my.matahati.pos.BuildConfig")
            clazz.getField("DEBUG").getBoolean(null)
        } catch (_: Exception) {
            true
        }
    }

    private val headerInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Accept", "application/json")
            .build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (isDebug) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .addInterceptor(headerInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val regionApiService: RegionApiService by lazy {
        Retrofit.Builder()
            .baseUrl(PRIMARY_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RegionApiService::class.java)
    }

    val secondaryRegionApiService: SecondaryRegionApiService by lazy {
        Retrofit.Builder()
            .baseUrl(SECONDARY_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SecondaryRegionApiService::class.java)
    }
}
