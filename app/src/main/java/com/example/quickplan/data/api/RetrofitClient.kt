package com.example.quickplan.data.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Retrofit 客户端单例
 * 
 * ⚠️⚠️⚠️ 重要：修改后端地址的位置 ⚠️⚠️⚠️
 * 
 * 当你的后端服务部署完成后，请修改下面的 BASE_URL
 * 
 * 本地开发示例：
 * - Android模拟器访问本机: "http://10.0.2.2:8080/"
 * - Android真机访问本机: "http://你的电脑IP:8080/"  (如 "http://192.168.1.100:8080/")
 * 
 * 生产环境示例：
 * - "https://your-domain.com/"
 */
object RetrofitClient {
    
    private const val TAG = "RetrofitClient"
    
    // 使用 NetworkConfig 管理所有网络地址配置（已为你切换为真机模式）
    init {
        Log.d(TAG, "==========================================")
        Log.d(TAG, "🌐 Retrofit 初始化")
        Log.d(TAG, "📍 BASE_URL = ${NetworkConfig.BASE_URL}")
        Log.d(TAG, "==========================================")
    }
    
    /**
     * 自定义拦截器用于调试网络连接
     */
    private val debugInterceptor = Interceptor { chain ->
        val request = chain.request()
        val url = request.url.toString()
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "发起网络请求:")
        Log.d(TAG, "URL: $url")
        Log.d(TAG, "Method: ${request.method}")
        Log.d(TAG, "Headers: ${request.headers}")
        
        try {
            val startTime = System.currentTimeMillis()
            val response = chain.proceed(request)
            val duration = System.currentTimeMillis() - startTime
            
            Log.d(TAG, "请求成功:")
            Log.d(TAG, "响应码: ${response.code}")
            Log.d(TAG, "耗时: ${duration}ms")
            Log.d(TAG, "响应头: ${response.headers}")
            Log.d(TAG, "========================================")
            
            response
        } catch (e: IOException) {
            Log.e(TAG, "❌ 网络请求失败:")
            Log.e(TAG, "URL: $url")
            Log.e(TAG, "异常类型: ${e.javaClass.simpleName}")
            Log.e(TAG, "错误信息: ${e.message}")
            Log.e(TAG, "堆栈跟踪:", e)
            Log.e(TAG, "========================================")
            
            // 重新抛出异常
            throw e
        }
    }
    
    /**
     * OkHttp 客户端（带日志拦截器）
     */
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY // 开发时使用BODY，生产环境改为NONE
        }
        
        Log.d(TAG, "初始化 OkHttpClient, BASE_URL: ${NetworkConfig.BASE_URL}")
        
        OkHttpClient.Builder()
            .addInterceptor(debugInterceptor) // 添加调试拦截器
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.MINUTES)  // 流式传输需要更长的读取超时
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true) // 启用连接失败重试
            .build()
    }
    
    /**
     * Retrofit 实例
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)  // 使用NetworkConfig
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * AI API 服务实例
     */
    val aiApiService: AiApiService by lazy {
        retrofit.create(AiApiService::class.java)
    }
    
    /**
     * 认证 API 服务实例
     */
    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}
