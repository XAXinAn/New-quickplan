package com.example.quickplan.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quickplan.data.api.*
import com.example.quickplan.data.local.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 登录认证 ViewModel
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authApiService = RetrofitClient.authApiService
    private val userPreferences = UserPreferences(application)
    
    companion object {
        private const val TAG = "AuthViewModel"
    }
    
    // 当前用户信息
    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo.asStateFlow()
    
    // 是否已登录
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误消息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 验证码倒计时
    private val _countdown = MutableStateFlow(0)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()
    
    init {
        checkLoginStatus()
    }
    
    /**
     * 检查登录状态
     */
    private fun checkLoginStatus() {
        _isLoggedIn.value = userPreferences.isLoggedIn()
        if (_isLoggedIn.value) {
            _userInfo.value = userPreferences.getUserInfo()
        }
    }
    
    /**
     * 发送验证码
     */
    fun sendVerificationCode(phone: String) {
        if (phone.isBlank()) {
            _errorMessage.value = "请输入手机号"
            return
        }
        
        if (!isValidPhone(phone)) {
            _errorMessage.value = "手机号格式不正确"
            return
        }
        
        if (_countdown.value > 0) {
            _errorMessage.value = "请稍后再试"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val response = authApiService.sendVerificationCode(
                    SendCodeRequest(phone = phone)
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    _errorMessage.value = "验证码已发送"
                    startCountdown(60)
                } else {
                    _errorMessage.value = response.body()?.message ?: "发送失败"
                }
            } catch (e: Exception) {
                Log.e(TAG, "发送验证码失败", e)
                _errorMessage.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 手机号登录
     */
    fun phoneLogin(phone: String, code: String) {
        if (phone.isBlank() || code.isBlank()) {
            _errorMessage.value = "请输入手机号和验证码"
            return
        }
        
        if (!isValidPhone(phone)) {
            _errorMessage.value = "手机号格式不正确"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val response = authApiService.phoneLogin(
                    PhoneLoginRequest(phone = phone, code = code)
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginData = response.body()?.data
                    if (loginData != null) {
                        handleLoginSuccess(loginData)
                    }
                } else {
                    _errorMessage.value = response.body()?.message ?: "登录失败"
                }
            } catch (e: Exception) {
                Log.e(TAG, "手机号登录失败", e)
                _errorMessage.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 手机号注册
     */
    fun phoneRegister(phone: String, code: String, password: String, confirmPassword: String, nickname: String?) {
        // 验证手机号
        if (phone.isBlank()) {
            _errorMessage.value = "请输入手机号"
            return
        }
        
        if (!isValidPhone(phone)) {
            _errorMessage.value = "手机号格式不正确"
            return
        }
        
        // 验证验证码
        if (code.isBlank()) {
            _errorMessage.value = "请输入验证码"
            return
        }
        
        // 验证密码
        if (password.isBlank()) {
            _errorMessage.value = "请输入密码"
            return
        }
        
        if (password.length < 6) {
            _errorMessage.value = "密码长度至少6位"
            return
        }
        
        if (password != confirmPassword) {
            _errorMessage.value = "两次密码输入不一致"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val response = authApiService.phoneRegister(
                    PhoneRegisterRequest(
                        phone = phone,
                        code = code,
                        password = password,
                        nickname = nickname
                    )
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginData = response.body()?.data
                    if (loginData != null) {
                        handleLoginSuccess(loginData)
                    }
                } else {
                    _errorMessage.value = response.body()?.message ?: "注册失败"
                }
            } catch (e: Exception) {
                Log.e(TAG, "手机号注册失败", e)
                _errorMessage.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 邮箱登录
     */
    fun emailLogin(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "请输入邮箱和密码"
            return
        }
        
        if (!isValidEmail(email)) {
            _errorMessage.value = "邮箱格式不正确"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val response = authApiService.emailLogin(
                    EmailLoginRequest(email = email, password = password)
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginData = response.body()?.data
                    if (loginData != null) {
                        handleLoginSuccess(loginData)
                    }
                } else {
                    _errorMessage.value = response.body()?.message ?: "登录失败"
                }
            } catch (e: Exception) {
                Log.e(TAG, "邮箱登录失败", e)
                _errorMessage.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 邮箱注册
     */
    fun emailRegister(email: String, password: String, confirmPassword: String, nickname: String?) {
        // 验证邮箱
        if (email.isBlank()) {
            _errorMessage.value = "请输入邮箱"
            return
        }
        
        if (!isValidEmail(email)) {
            _errorMessage.value = "邮箱格式不正确"
            return
        }
        
        // 验证密码
        if (password.isBlank()) {
            _errorMessage.value = "请输入密码"
            return
        }
        
        if (password.length < 6) {
            _errorMessage.value = "密码长度至少6位"
            return
        }
        
        if (password != confirmPassword) {
            _errorMessage.value = "两次密码输入不一致"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val response = authApiService.emailRegister(
                    EmailRegisterRequest(
                        email = email,
                        password = password,
                        nickname = nickname
                    )
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginData = response.body()?.data
                    if (loginData != null) {
                        handleLoginSuccess(loginData)
                    }
                } else {
                    _errorMessage.value = response.body()?.message ?: "注册失败"
                }
            } catch (e: Exception) {
                Log.e(TAG, "邮箱注册失败", e)
                _errorMessage.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 微信登录
     */
    fun wechatLogin(code: String, userInfo: WechatUserInfo? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val response = authApiService.wechatLogin(
                    WechatLoginRequest(code = code, userInfo = userInfo)
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginData = response.body()?.data
                    if (loginData != null) {
                        handleLoginSuccess(loginData)
                    }
                } else {
                    _errorMessage.value = response.body()?.message ?: "微信登录失败"
                }
            } catch (e: Exception) {
                Log.e(TAG, "微信登录失败", e)
                _errorMessage.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * QQ登录
     */
    fun qqLogin(accessToken: String, openId: String, userInfo: QQUserInfo? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val response = authApiService.qqLogin(
                    QQLoginRequest(accessToken = accessToken, openId = openId, userInfo = userInfo)
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginData = response.body()?.data
                    if (loginData != null) {
                        handleLoginSuccess(loginData)
                    }
                } else {
                    _errorMessage.value = response.body()?.message ?: "QQ登录失败"
                }
            } catch (e: Exception) {
                Log.e(TAG, "QQ登录失败", e)
                _errorMessage.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 登出
     */
    fun logout() {
        viewModelScope.launch {
            try {
                val token = userPreferences.getToken()
                if (token != null) {
                    authApiService.logout("Bearer $token")
                }
            } catch (e: Exception) {
                Log.e(TAG, "登出请求失败", e)
            } finally {
                userPreferences.clearLoginInfo()
                _userInfo.value = null
                _isLoggedIn.value = false
            }
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * 处理登录成功
     */
    private fun handleLoginSuccess(loginData: LoginData) {
        userPreferences.saveLoginInfo(
            token = loginData.token,
            refreshToken = loginData.refreshToken,
            userInfo = loginData.userInfo
        )
        _userInfo.value = loginData.userInfo
        _isLoggedIn.value = true
        Log.d(TAG, "登录成功: ${loginData.userInfo.userId}")
    }
    
    /**
     * 开始倒计时
     */
    private fun startCountdown(seconds: Int) {
        viewModelScope.launch {
            _countdown.value = seconds
            repeat(seconds) {
                kotlinx.coroutines.delay(1000)
                _countdown.value--
            }
        }
    }
    
    /**
     * 验证手机号格式
     */
    private fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex("^1[3-9]\\d{9}$"))
    }
    
    /**
     * 验证邮箱格式
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * 获取Token (用于API调用)
     */
    fun getAuthToken(): String? {
        return userPreferences.getToken()
    }
}
