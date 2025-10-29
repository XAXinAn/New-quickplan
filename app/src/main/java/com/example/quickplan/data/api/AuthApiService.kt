package com.example.quickplan.data.api

import retrofit2.Response
import retrofit2.http.*

/**
 * 用户认证相关 API 接口
 */
interface AuthApiService {
    
    /**
     * 手机号登录 - 发送验证码
     * POST /api/auth/phone/send-code
     */
    @POST("api/auth/phone/send-code")
    suspend fun sendVerificationCode(@Body request: SendCodeRequest): Response<SendCodeResponse>
    
    /**
     * 手机号注册
     * POST /api/auth/phone/register
     */
    @POST("api/auth/phone/register")
    suspend fun phoneRegister(@Body request: PhoneRegisterRequest): Response<LoginResponse>
    
    /**
     * 邮箱注册
     * POST /api/auth/email/register
     */
    @POST("api/auth/email/register")
    suspend fun emailRegister(@Body request: EmailRegisterRequest): Response<LoginResponse>
    
    /**
     * 手机号登录 - 验证码登录
     * POST /api/auth/phone/login
     */
    @POST("api/auth/phone/login")
    suspend fun phoneLogin(@Body request: PhoneLoginRequest): Response<LoginResponse>
    
    /**
     * 微信登录
     * POST /api/auth/wechat/login
     */
    @POST("api/auth/wechat/login")
    suspend fun wechatLogin(@Body request: WechatLoginRequest): Response<LoginResponse>
    
    /**
     * QQ登录
     * POST /api/auth/qq/login
     */
    @POST("api/auth/qq/login")
    suspend fun qqLogin(@Body request: QQLoginRequest): Response<LoginResponse>
    
    /**
     * 邮箱登录
     * POST /api/auth/email/login
     */
    @POST("api/auth/email/login")
    suspend fun emailLogin(@Body request: EmailLoginRequest): Response<LoginResponse>
    
    /**
     * 刷新Token
     * POST /api/auth/refresh-token
     */
    @POST("api/auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<LoginResponse>
    
    /**
     * 登出
     * POST /api/auth/logout
     */
    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<BaseResponse>
    
    /**
     * 获取用户信息
     * GET /api/user/info
     */
    @GET("api/user/info")
    suspend fun getUserInfo(@Header("Authorization") token: String): Response<UserInfoResponse>
}

// ==================== 请求数据类 ====================

/**
 * 发送验证码请求
 */
data class SendCodeRequest(
    val phone: String,
    val type: String = "login"  // login: 登录, register: 注册
)

/**
 * 手机号登录请求
 */
data class PhoneLoginRequest(
    val phone: String,
    val code: String
)

/**
 * 手机号注册请求
 */
data class PhoneRegisterRequest(
    val phone: String,
    val code: String,
    val password: String,
    val nickname: String? = null
)

/**
 * 邮箱注册请求
 */
data class EmailRegisterRequest(
    val email: String,
    val password: String,
    val nickname: String? = null
)

/**
 * 微信登录请求
 */
data class WechatLoginRequest(
    val code: String,  // 微信授权码
    val userInfo: WechatUserInfo? = null
)

data class WechatUserInfo(
    val nickname: String,
    val avatarUrl: String,
    val openId: String
)

/**
 * QQ登录请求
 */
data class QQLoginRequest(
    val accessToken: String,  // QQ accessToken
    val openId: String,
    val userInfo: QQUserInfo? = null
)

data class QQUserInfo(
    val nickname: String,
    val avatarUrl: String
)

/**
 * 邮箱登录请求
 */
data class EmailLoginRequest(
    val email: String,
    val password: String
)

/**
 * 刷新Token请求
 */
data class RefreshTokenRequest(
    val refreshToken: String
)

// ==================== 响应数据类 ====================

/**
 * 发送验证码响应
 */
data class SendCodeResponse(
    val success: Boolean,
    val message: String,
    val data: SendCodeData? = null
)

data class SendCodeData(
    val expiresIn: Int  // 验证码有效期(秒)
)

/**
 * 登录响应
 */
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData? = null
)

data class LoginData(
    val userId: String,
    val token: String,
    val refreshToken: String,
    val expiresIn: Long,  // Token有效期(秒)
    val userInfo: UserInfo
)

/**
 * 用户信息
 */
data class UserInfo(
    val userId: String,
    val phone: String? = null,
    val email: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val createdAt: String,
    val loginType: String  // phone, wechat, qq, email
)

/**
 * 用户信息响应
 */
data class UserInfoResponse(
    val success: Boolean,
    val message: String,
    val data: UserInfo? = null
)

/**
 * 基础响应
 */
data class BaseResponse(
    val success: Boolean,
    val message: String
)
