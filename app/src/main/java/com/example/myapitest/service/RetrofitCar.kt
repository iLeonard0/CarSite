package com.example.myapitest.service

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object RetrofitCar {

    private const val BASE_URL = "http://10.0.2.2:3000/" // Endereço usado para acessar o localhost no emulador android

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // cria um interceptor para registrar logging no LogCat para cada
        // requisição realizada utilizando esse interceptor
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()


    private val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
    }

    val apiService = instance.create(CarApiService::class.java)

}