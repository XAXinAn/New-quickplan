package com.example.quickplan.data.api

import android.util.Log

/**
 * 网络配置管理
 * 提供多种后端URL配置方案
 */
object NetworkConfig {
    private const val TAG = "NetworkConfig"
    
    /**
     * 🔥 重要: 根据你的网络环境选择合适的配置
     */
    enum class ServerMode {
        // 模拟器访问本机 - 标准方式
        EMULATOR_STANDARD,
        
        // 模拟器访问本机 - 使用电脑IP (推荐!)
        EMULATOR_HOST_IP,
        
        // 真机访问本机 - 必须使用电脑IP
        REAL_DEVICE,
        
        // 生产环境
        PRODUCTION
    }
    
    // ⚡⚡⚡ USB 调试模式 - 使用 adb reverse ⚡⚡⚡
    // 需要先在终端运行: adb reverse tcp:8080 tcp:8080
    // 这样手机访问 localhost:8080 会转发到电脑的 8080 端口
    private val CURRENT_MODE = ServerMode.EMULATOR_STANDARD  // 使用 localhost
    
    // 📍 USB 调试使用 localhost（通过 adb reverse 转发）
    // 如果需要 Wi-Fi 调试，改为你的电脑IP (从 ipconfig 获取)
    private const val HOST_IP = "127.0.0.1"  // USB 调试使用 localhost
    
    // 后端端口
    private const val PORT = 8080
    
    /**
     * 获取Base URL
     */
    val BASE_URL: String
        get() {
            val url = when (CURRENT_MODE) {
                ServerMode.EMULATOR_STANDARD -> "http://localhost:$PORT/"  // USB调试用localhost
                ServerMode.EMULATOR_HOST_IP -> "http://$HOST_IP:$PORT/"
                ServerMode.REAL_DEVICE -> "http://$HOST_IP:$PORT/"
                ServerMode.PRODUCTION -> "https://your-domain.com/"
            }
            
            Log.d(TAG, "==========================================")
            Log.d(TAG, "🌐 网络配置")
            Log.d(TAG, "📍 模式: $CURRENT_MODE")
            Log.d(TAG, "📍 BASE_URL: $url")
            Log.d(TAG, "==========================================")
            
            return url
        }
    
    /**
     * 获取所有可能的备选URL (用于故障排查)
     */
    fun getAllPossibleUrls(): List<String> {
        return listOf(
            "http://10.0.2.2:$PORT/",           // 模拟器标准方式
            "http://localhost:$PORT/",           // 某些模拟器
            "http://127.0.0.1:$PORT/",          // 本地回环
            "http://$HOST_IP:$PORT/",           // 宿主机IP
            "http://172.18.116.223:$PORT/",     // 你的主IP
            "http://192.168.132.1:$PORT/",      // 备选IP 1
            "http://192.168.126.1:$PORT/"       // 备选IP 2
        )
    }
}
