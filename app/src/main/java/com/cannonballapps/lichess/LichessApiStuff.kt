package com.cannonballapps.lichess

import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import java.io.IOException

object LichessRetrofitHelper {

    val baseUrl = "https://lichess.org/"

//    val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(object : Interceptor() {
//        @Throws(IOException::class)
//        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
//            val newRequest: Request = chain.request().newBuilder()
//                .addHeader("Authorization", "Bearer $token")
//                .build()
//            return chain.proceed(newRequest)
//        }
//    }).build()

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
//            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            // we need to add converter factory to
            // convert JSON object to Java object
            .build()
    }
}

interface LichessApi {
    @GET("/api/account/email")
    suspend fun getAccountEmail(@Header("Authorization") authHeader: String) : Response<LichessEmail>
}

data class LichessEmail(
    val email: String,
)
