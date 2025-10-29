package com.example.quickplan.utils

import android.util.Log
import com.example.quickplan.data.api.NetworkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

/**
 * 网络连接调试助手
 */
object NetworkDebugHelper {
    private const val TAG = "NetworkDebug"
    
    /**
     * 全面的网络诊断 - 测试所有可能的URL
     */
    suspend fun diagnose(): DiagnosisResult = withContext(Dispatchers.IO) {
        val result = DiagnosisResult()
        
        Log.d(TAG, "====================================")
        Log.d(TAG, "🔍 开始全面网络诊断")
        Log.d(TAG, "====================================")
        
        // 当前配置的URL
        val currentUrl = NetworkConfig.BASE_URL
        Log.d(TAG, "📍 当前配置: $currentUrl")
        
        // 测试当前URL
        val currentWorking = testHttpRequest(currentUrl)
        result.httpWorking = currentWorking
        result.workingUrl = if (currentWorking) currentUrl else null
        
        if (currentWorking) {
            Log.d(TAG, "✅ 当前配置可用!")
            result.dnsResolved = true
            result.connected = true
        } else {
            Log.d(TAG, "❌ 当前配置失败,尝试其他地址...")
            
            // 尝试所有可能的URL
            val allUrls = NetworkConfig.getAllPossibleUrls()
            for (url in allUrls) {
                if (url == currentUrl) continue // 跳过已测试的
                
                Log.d(TAG, "🔄 尝试: $url")
                if (testHttpRequest(url)) {
                    Log.d(TAG, "✅ 找到可用地址: $url")
                    result.workingUrl = url
                    result.httpWorking = true
                    result.dnsResolved = true
                    result.connected = true
                    break
                }
            }
        }
        
        Log.d(TAG, "====================================")
        Log.d(TAG, "📊 诊断结果:")
        Log.d(TAG, "可用地址: ${result.workingUrl ?: "❌ 未找到"}")
        Log.d(TAG, "====================================")
        
        result
    }
    
    /**
     * 测试HTTP请求
     */
    private fun testHttpRequest(baseUrl: String): Boolean {
        return try {
            val url = URL("${baseUrl.trimEnd('/')}/api/ai/chat/new")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            // 发送请求体
            val requestBody = """{"userId":"test"}"""
            connection.outputStream.use { os ->
                os.write(requestBody.toByteArray())
                os.flush()
            }
            
            val responseCode = connection.responseCode
            connection.disconnect()
            
            responseCode in 200..299
        } catch (e: Exception) {
            Log.d(TAG, "   ❌ ${e.javaClass.simpleName}: ${e.message}")
            false
        }
    }
    
    data class DiagnosisResult(
        var dnsResolved: Boolean = false,
        var connected: Boolean = false,
        var httpWorking: Boolean = false,
        var workingUrl: String? = null
    ) {
        fun isHealthy(): Boolean = httpWorking && workingUrl != null
        
        fun getMessage(): String {
            return if (isHealthy()) {
                "✅ 网络诊断成功!\n\n可用地址:\n$workingUrl\n\n💡 建议: 在 NetworkConfig.kt 中\n使用这个地址配置"
            } else {
                """
                ❌ 网络诊断失败
                
                可能原因:
                1️⃣ 后端未启动或端口错误
                2️⃣ 防火墙阻止连接
                3️⃣ 模拟器网络配置问题
                
                解决方案:
                • 确认后端运行在 8080 端口
                • 关闭防火墙或添加例外
                • 尝试使用真机调试
                • 检查 NetworkConfig.kt 配置
                """.trimIndent()
            }
        }
    }
}
