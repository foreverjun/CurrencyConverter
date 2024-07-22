package com.example.currencyconverter

import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.core.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

const val apiKey = BuildConfig.API_KEY


private const val BASE_URL = "https://v6.exchangerate-api.com/v6/${apiKey}/"


data class ConverterResponse(
    val result: String?,
    @SerializedName("error-type")
    val errorType: String?,
    val documentation: String?,
    @SerializedName("terms_of_use")
    val termsOfUse: String?,
    @SerializedName("time_last_update_unix")
    val timeLastUpdateUnix: Long?,
    @SerializedName("time_last_update_utc")
    val timeLastUpdateUtc: String?,
    @SerializedName("time_next_update_unix")
    val timeNextUpdateUnix: Long?,
    @SerializedName("time_next_update_utc")
    val timeNextUpdateUtc: String?,
    @SerializedName("base_code")
    val baseCode: String?,
    @SerializedName("target_code")
    val targetCode: String?,
    @SerializedName("conversion_rate")
    val conversionRate: String?,
    @SerializedName("conversion_result")
    val conversionResult: String?,
)

data class CurrenciesResponse(
    @SerializedName("error") val error: String?,
    @SerializedName("result") val result: String?,
    @SerializedName("documentation") val documentation: String?,
    @SerializedName("terms_of_use") val termsOfUse: String?,
    @SerializedName("supported_codes") val supportedCodes: ArrayList<ArrayList<String>>?

)

interface ConverterApiServ {
    @GET("pair/{from}/{to}/{amount}")
    fun getConvertedAmount(
        @Path("from") from: String,
        @Path("to") to: String,
        @Path("amount") amount: String
    ): Single<ConverterResponse>

    @GET("codes")
    fun getSupportedCurrencies(): Single<CurrenciesResponse>
}

object ConverterApi {
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).build()
    }
    val retrofitServ: ConverterApiServ by lazy {
        Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(
            BASE_URL
        ).addCallAdapterFactory(RxJava3CallAdapterFactory.create()).client(client).build()
            .create(ConverterApiServ::class.java)
    }
}

enum class ApiError(val err: String) {
    UNSUPPORTED("unsupported-code"),
    MALFORMED("malformed-request"),
    INVALID("invalid-key"),
    INACTIVE("inactive-account"),
    QUOTA("quota-reached")
}

fun getErrorMsg(error: String?): String {
    return when (error) {
        ApiError.UNSUPPORTED.err -> "Unsupported currency code"
        ApiError.INVALID.err -> "Invalid API key"
        ApiError.MALFORMED.err -> "Wrong request format"
        ApiError.INACTIVE.err -> "Inactive account"
        ApiError.QUOTA.err -> "Request quota reached"
        else -> "Unknown error"
    }
}