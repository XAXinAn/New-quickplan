package com.example.quickplan.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quickplan.data.api.*
import com.example.quickplan.data.local.UserPreferences
import com.example.quickplan.data.model.Conversation
import com.example.quickplan.data.model.Message
import com.example.quickplan.utils.MLKitOCRHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * AI 对话界面 ViewModel
 * 管理对话状态和后端API交互
 * 
 * 📍 所有后端API调用都通过这个类进行
 */
class AiViewModel(application: Application) : AndroidViewModel(application) {
    
    // API 服务实例
    private val apiService = RetrofitClient.aiApiService
    
    // 用户偏好设置实例
    private val userPreferences = UserPreferences(application)
    
    // OCR 助手 (Google ML Kit 会自动管理模型下载)
    private val ocrHelper = MLKitOCRHelper
    
    override fun onCleared() {
        super.onCleared()
        // 释放 ML Kit 资源
        MLKitOCRHelper.release()
    }
    
    // 当前用户ID - 从登录状态获取,如果未登录则使用默认值
    private val currentUserId: String
        get() = userPreferences.getUserInfo()?.userId ?: "guest_user"
    
    // 当前选中的对话ID
    private val _currentMemoryId = MutableStateFlow<String?>(null)
    val currentMemoryId: StateFlow<String?> = _currentMemoryId.asStateFlow()
    
    // 当前对话的消息列表
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    // 对话历史列表
    private val _conversations = MutableStateFlow<List<ConversationSummary>>(emptyList())
    val conversations: StateFlow<List<ConversationSummary>> = _conversations.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误消息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 是否显示侧边栏
    private val _showSidebar = MutableStateFlow(false)
    val showSidebar: StateFlow<Boolean> = _showSidebar.asStateFlow()
    
    // 是否已经初始化加载过对话列表
    private var hasLoadedConversations = false
    
    // 当前正在进行的消息发送Job,用于取消
    private var currentSendJob: kotlinx.coroutines.Job? = null
    
    init {
        // 延迟加载对话列表,避免每次重建ViewModel都加载
        viewModelScope.launch {
            if (!hasLoadedConversations) {
                loadConversations()
                hasLoadedConversations = true
            }
        }
    }
    
    /**
     * 📍 API调用位置 #1: 发送消息（非流式）
     * 调用 POST /api/ai/chat
     * 后端返回 JSON 格式的完整回复
     */
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        // 取消之前的发送任务
        currentSendJob?.cancel()
        
        currentSendJob = viewModelScope.launch {
            try {
                _errorMessage.value = null
                
                // 如果没有当前会话，先创建一个新会话并等待完成
                val needCreateConversation = _currentMemoryId.value == null
                if (needCreateConversation) {
                    // 创建会话时不清空消息列表,也不设置 isLoading
                    val created = createNewConversationSync(clearMessages = false, setLoading = false)
                    if (!created) {
                        _errorMessage.value = "创建会话失败"
                        return@launch
                    }
                }
                
                // 开始加载(发送消息)
                _isLoading.value = true
                
                // 添加用户消息到界面
                val userMessage = Message(content = content, isUser = true)
                _messages.value = _messages.value + userMessage
                
                // 创建一个临时的 AI 消息用于显示加载状态
                val aiMessageId = "ai-msg-${System.currentTimeMillis()}"
                val aiMessage = Message(
                    id = aiMessageId,
                    content = "正在思考...",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value + aiMessage
                
                // 调用后端API（非流式）
                val request = ChatRequest(
                    memoryId = _currentMemoryId.value!!,
                    message = content,
                    userId = currentUserId
                )
                
                val response = apiService.sendMessage(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    
                    if (result.success) {
                        // 更新AI消息为完整回复
                        _messages.value = _messages.value.map { msg ->
                            if (msg.id == aiMessageId) {
                                msg.copy(content = result.message)
                            } else {
                                msg
                            }
                        }
                        
                        // 刷新对话列表
                        forceRefreshConversations()
                    } else {
                        // 显示错误信息
                        _messages.value = _messages.value.map { msg ->
                            if (msg.id == aiMessageId) {
                                msg.copy(content = result.message)
                            } else {
                                msg
                            }
                        }
                        _errorMessage.value = result.message
                    }
                } else {
                    _errorMessage.value = "发送失败: ${response.code()}"
                    // 移除临时的 AI 消息
                    _messages.value = _messages.value.filter { it.id != aiMessageId }
                }
            } catch (e: Exception) {
                _errorMessage.value = "网络错误: ${e.message}"
                e.printStackTrace()
                
                // 更新最后一条AI消息为错误提示
                val lastAiMessageId = _messages.value.lastOrNull { !it.isUser }?.id
                if (lastAiMessageId != null) {
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == lastAiMessageId) {
                            msg.copy(content = "AI 服务连接失败")
                        } else {
                            msg
                        }
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 📍 API调用位置 #2: 加载对话列表
     * 调用 GET /api/conversation/list/{userId}
     */
    fun loadConversations() {
        // 避免重复加载
        if (_isLoading.value) return
        
        viewModelScope.launch {
            try {
                val response = apiService.getConversations(currentUserId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        _conversations.value = body.data
                        hasLoadedConversations = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 不显示错误,静默失败
            }
        }
    }
    
    /**
     * 强制刷新对话列表(用于创建/删除对话后)
     */
    private fun forceRefreshConversations() {
        viewModelScope.launch {
            try {
                val response = apiService.getConversations(currentUserId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        _conversations.value = body.data
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 📍 API调用位置 #3: 加载对话详情和消息
     * 调用 GET /api/conversation/messages/{conversationId}?userId={userId}
     */
    fun loadConversation(conversationId: String) {
        // 取消当前正在进行的消息发送
        currentSendJob?.cancel()
        currentSendJob = null
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // 先清空当前消息,避免"正在思考"气泡残留
                _messages.value = emptyList()
                
                // 获取消息列表,传递 userId
                val response = apiService.getConversationMessages(conversationId, currentUserId)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        _currentMemoryId.value = conversationId
                        
                        // 转换消息格式
                        val loadedMessages = body.data.map { dto ->
                            Message(
                                id = dto.id.toString(),
                                content = dto.content,
                                isUser = dto.role == "user",
                                timestamp = System.currentTimeMillis() // 简化处理
                            )
                        }
                        
                        // 检测并修复缺失的AI回复:如果最后一条是用户消息,说明AI未回复(流失败)
                        val finalMessages = if (loadedMessages.isNotEmpty() && loadedMessages.last().isUser) {
                            loadedMessages + Message(
                                id = "error-msg-${System.currentTimeMillis()}",
                                content = "AI 服务不可用",
                                isUser = false,
                                timestamp = System.currentTimeMillis()
                            )
                        } else {
                            loadedMessages
                        }
                        
                        _messages.value = finalMessages
                    }
                } else {
                    _errorMessage.value = "加载对话失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "网络错误: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 📍 API调用位置 #4: 创建新对话
     * 调用 POST /api/ai/chat/new
     */
    fun createNewConversation() {
        viewModelScope.launch {
            createNewConversationSync(clearMessages = true, setLoading = true)
        }
    }
    
    /**
     * 同步创建新对话（用于发送消息前）
     * @param clearMessages 是否清空消息列表
     * @param setLoading 是否设置loading状态
     * @return 是否创建成功
     */
    private suspend fun createNewConversationSync(clearMessages: Boolean = true, setLoading: Boolean = true): Boolean {
        return try {
            if (setLoading) {
                _isLoading.value = true
            }
            _errorMessage.value = null
            
            val request = CreateConversationRequest(
                userId = currentUserId,
                title = "新对话"
            )
            
            val response = apiService.createConversation(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    _currentMemoryId.value = body.data.id
                    
                    // 根据参数决定是否清空消息
                    if (clearMessages) {
                        _messages.value = emptyList()
                    }
                    
                    // 刷新对话列表
                    forceRefreshConversations()
                    
                    // 关闭侧边栏
                    _showSidebar.value = false
                    true
                } else {
                    _errorMessage.value = body.message ?: "创建对话失败"
                    false
                }
            } else {
                _errorMessage.value = "创建对话失败: ${response.code()}"
                false
            }
        } catch (e: Exception) {
            _errorMessage.value = "网络错误: ${e.message}"
            e.printStackTrace()
            false
        } finally {
            if (setLoading) {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 📍 API调用位置 #5: 删除对话
     * 调用 DELETE /api/conversation/delete/{conversationId}
     */
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteConversation(conversationId)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        // 如果删除的是当前对话，清空状态
                        if (_currentMemoryId.value == conversationId) {
                            _currentMemoryId.value = null
                            _messages.value = emptyList()
                        }
                        
                        // 刷新对话列表
                        forceRefreshConversations()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 切换侧边栏显示状态
     */
    fun toggleSidebar() {
        _showSidebar.value = !_showSidebar.value
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * 设置错误消息（用于外部调用）
     */
    fun setError(message: String) {
        _errorMessage.value = message
    }
    
    /**
     * 开始新对话（不调用API，仅清空本地状态）
     */
    fun startNewConversation() {
        // 取消当前正在进行的消息发送
        currentSendJob?.cancel()
        currentSendJob = null
        
        // 清空状态
        _currentMemoryId.value = null
        _messages.value = emptyList()
        _showSidebar.value = false
        _isLoading.value = false
        _errorMessage.value = null
    }
    
    /**
     * 📍 API调用位置 #6: OCR 识别并创建提醒
     * 1. 使用 PaddleOCR 识别图片文字
     * 2. 调用 POST /api/ai/ocr/reminder
     * @param bitmap 要识别的图片
     */
    fun processOCRImage(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // 添加一个临时的提示消息
                val tempMessage = Message(
                    content = "🔍 正在识别图片内容...",
                    isUser = false
                )
                _messages.value = _messages.value + tempMessage
                
                // 在 IO 线程进行 OCR 识别
                val ocrText = withContext(Dispatchers.IO) {
                    android.util.Log.d("AiViewModel", "开始 OCR 识别")
                    val result = ocrHelper.recognizeText(bitmap)
                    android.util.Log.d("AiViewModel", "OCR 识别结果: $result")
                    result
                }
                
                // 移除临时消息
                _messages.value = _messages.value.dropLast(1)
                
                android.util.Log.d("AiViewModel", "OCR 文本是否为空: ${ocrText.isBlank()}, 长度: ${ocrText.length}")
                
                if (ocrText.isNotBlank()) {
                    // 调用后端处理 OCR 文本
                    processOCRText(ocrText)
                } else {
                    _errorMessage.value = "OCR 识别失败,未能识别出文字"
                    val errorMsg = Message(
                        content = "❌ OCR 识别失败,图片中没有识别到文字内容",
                        isUser = false
                    )
                    _messages.value = _messages.value + errorMsg
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "OCR 识别出错: ${e.message}"
                android.util.Log.e("AiViewModel", "OCR recognition error", e)
                
                // 显示错误消息
                val errorMsg = Message(
                    content = "❌ OCR 识别异常: ${e.message}",
                    isUser = false
                )
                _messages.value = _messages.value.dropLast(1) + errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 处理 OCR 识别出的文本
     * 改为通过对话接口让 AI 自主调用工具创建日程
     * @param ocrText OCR 识别出的文本
     */
    private fun processOCRText(ocrText: String) {
        if (ocrText.isBlank()) {
            _errorMessage.value = "OCR 识别文本为空"
            android.util.Log.w("AiViewModel", "OCR 文本为空")
            return
        }
        
        android.util.Log.d("AiViewModel", "处理 OCR 文本: $ocrText")
        
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                
                // 如果没有当前会话，先创建一个新会话
                val needCreateConversation = _currentMemoryId.value == null
                android.util.Log.d("AiViewModel", "是否需要创建会话: $needCreateConversation")
                
                if (needCreateConversation) {
                    val created = createNewConversationSync(clearMessages = false, setLoading = false)
                    if (!created) {
                        _errorMessage.value = "创建会话失败"
                        android.util.Log.e("AiViewModel", "创建会话失败")
                        return@launch
                    }
                    android.util.Log.d("AiViewModel", "会话创建成功: ${_currentMemoryId.value}")
                }
                
                // 开始加载
                _isLoading.value = true
                
                // 添加用户消息显示 OCR 结果
                val ocrMessage = Message(
                    content = "📷 图片识别内容:\n$ocrText",
                    isUser = true
                )
                _messages.value = _messages.value + ocrMessage
                android.util.Log.d("AiViewModel", "已添加用户OCR消息")
                
                // 构造消息：让 AI 通过工具调用自动添加日程 (必须以"帮我添加日程："开头)
                val messageToAI = "帮我添加日程：$ocrText"
                android.util.Log.d("AiViewModel", "发送给AI的消息: $messageToAI")
                
                // 创建一个临时的 AI 消息用于显示加载状态
                val aiMessageId = "ai-msg-ocr-${System.currentTimeMillis()}"
                val aiMessage = Message(
                    id = aiMessageId,
                    content = "正在思考...",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value + aiMessage
                
                // 调用非流式聊天接口,让 AI 自己使用工具
                val chatRequest = ChatRequest(
                    message = messageToAI,
                    memoryId = _currentMemoryId.value!!,
                    userId = currentUserId
                )
                
                val response = apiService.sendMessage(chatRequest)
                
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    
                    if (result.success) {
                        // 更新AI消息为完整回复
                        _messages.value = _messages.value.map { msg ->
                            if (msg.id == aiMessageId) {
                                msg.copy(content = result.message)
                            } else {
                                msg
                            }
                        }
                        
                        // 刷新对话列表
                        forceRefreshConversations()
                    } else {
                        // 显示错误信息
                        _messages.value = _messages.value.map { msg ->
                            if (msg.id == aiMessageId) {
                                msg.copy(content = result.message)
                            } else {
                                msg
                            }
                        }
                        _errorMessage.value = result.message
                    }
                } else {
                    _errorMessage.value = "AI 请求失败: ${response.code()}"
                    // 移除临时的 AI 消息
                    _messages.value = _messages.value.filter { it.id != aiMessageId }
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "AI 处理出错: ${e.message}"
                android.util.Log.e("AiViewModel", "AI chat error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
