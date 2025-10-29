package com.example.quickplan.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.quickplan.data.api.UserInfo
import com.google.gson.Gson

/**
 * 用户信息本地存储
 */
class UserPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "user_prefs",
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_INFO = "user_info"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    /**
     * 保存登录信息
     */
    fun saveLoginInfo(token: String, refreshToken: String, userInfo: UserInfo) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putString(KEY_USER_INFO, gson.toJson(userInfo))
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    /**
     * 获取Token
     */
    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }
    
    /**
     * 获取RefreshToken
     */
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    /**
     * 获取用户信息
     */
    fun getUserInfo(): UserInfo? {
        val json = prefs.getString(KEY_USER_INFO, null) ?: return null
        return try {
            gson.fromJson(json, UserInfo::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 是否已登录
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getToken() != null
    }
    
    /**
     * 清除登录信息
     */
    fun clearLoginInfo() {
        prefs.edit().clear().apply()
    }
    
    /**
     * 更新Token
     */
    fun updateToken(token: String, refreshToken: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
    }
}
