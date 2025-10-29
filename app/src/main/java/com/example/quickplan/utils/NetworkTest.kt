package com.example.quickplan.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * 网络连接测试工具
 */
object NetworkTest {
    
    /**
     * 测试是否能连接到后端服务器
     */
    suspend fun testConnection(baseUrl: String = "http://10.0.2.2:8080"): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("NetworkTest", "Testing connection to: $baseUrl")
            
            val url = URL("$baseUrl/api/ai/chat/new")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            // 发送简单的测试请求
            val jsonBody = """{"userId":"test"}"""
            connection.outputStream.use { it.write(jsonBody.toByteArray()) }
            
            val responseCode = connection.responseCode
            Log.d("NetworkTest", "Response code: $responseCode")
            
            connection.disconnect()
            
            responseCode in 200..299
        } catch (e: Exception) {
            Log.e("NetworkTest", "Connection failed: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 测试DNS解析
     */
    suspend fun testDNS(host: String = "10.0.2.2"): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("NetworkTest", "Testing DNS resolution for: $host")
            val address = java.net.InetAddress.getByName(host)
            Log.d("NetworkTest", "DNS resolved to: ${address.hostAddress}")
            true
        } catch (e: Exception) {
            Log.e("NetworkTest", "DNS resolution failed: ${e.message}")
            false
        }
    }
    
    /**
     * 测试端口连接
     */
    suspend fun testPort(host: String = "10.0.2.2", port: Int = 8080): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("NetworkTest", "Testing port connection to: $host:$port")
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress(host, port), 5000)
            val connected = socket.isConnected
            socket.close()
            Log.d("NetworkTest", "Port test result: $connected")
            connected
        } catch (e: Exception) {
            Log.e("NetworkTest", "Port connection failed: ${e.message}")
            false
        }
    }
}
