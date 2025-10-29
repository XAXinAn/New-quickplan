package com.example.quickplan.utils

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Google ML Kit OCR 助手类
 * 提供中文文字识别功能
 */
object MLKitOCRHelper {
    
    // 初始化中文文字识别器
    private val recognizer by lazy {
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    }
    
    /**
     * 识别图片中的文字
     * @param bitmap 要识别的图片
     * @return 识别结果文本
     */
    suspend fun recognizeText(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
        try {
            android.util.Log.d("MLKitOCR", "开始识别图片, 尺寸: ${bitmap.width}x${bitmap.height}")
            
            val image = InputImage.fromBitmap(bitmap, 0)
            
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val recognizedText = visionText.text
                    android.util.Log.d("MLKitOCR", "识别成功, 文本长度: ${recognizedText.length}")
                    android.util.Log.d("MLKitOCR", "识别内容: $recognizedText")
                    continuation.resume(recognizedText)
                }
                .addOnFailureListener { exception ->
                    android.util.Log.e("MLKitOCR", "识别失败", exception)
                    continuation.resumeWithException(exception)
                }
        } catch (e: Exception) {
            android.util.Log.e("MLKitOCR", "识别异常", e)
            continuation.resumeWithException(e)
        }
    }
    
    /**
     * 识别图片中的文字（详细结果）
     * @param bitmap 要识别的图片
     * @return OCR 识别详细结果
     */
    suspend fun recognizeTextDetailed(bitmap: Bitmap): OCRResult = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val textBlocks = visionText.textBlocks.map { block ->
                        TextBlock(
                            text = block.text,
                            boundingBox = block.boundingBox,
                            lines = block.lines.map { line ->
                                TextLine(
                                    text = line.text,
                                    boundingBox = line.boundingBox
                                )
                            }
                        )
                    }
                    
                    val result = OCRResult(
                        success = true,
                        fullText = visionText.text,
                        textBlocks = textBlocks,
                        error = null
                    )
                    continuation.resume(result)
                }
                .addOnFailureListener { exception ->
                    val result = OCRResult(
                        success = false,
                        fullText = "",
                        textBlocks = emptyList(),
                        error = exception.message ?: "识别失败"
                    )
                    continuation.resume(result)
                }
        } catch (e: Exception) {
            val result = OCRResult(
                success = false,
                fullText = "",
                textBlocks = emptyList(),
                error = e.message ?: "识别失败"
            )
            continuation.resume(result)
        }
    }
    
    /**
     * 提取图片中的待办事项
     * 智能识别并解析待办事项文本
     */
    suspend fun extractReminders(bitmap: Bitmap): List<String> {
        val result = recognizeTextDetailed(bitmap)
        if (!result.success) return emptyList()
        
        // 解析文本，提取待办事项
        return result.textBlocks
            .map { it.text }
            .filter { it.isNotBlank() }
            .flatMap { parseRemindersFromText(it) }
    }
    
    /**
     * 从文本中解析待办事项
     * 支持多种格式：
     * - 带序号的列表 (1. xxx, 2. xxx)
     * - 带符号的列表 (- xxx, * xxx, • xxx)
     * - 每行一个待办
     */
    private fun parseRemindersFromText(text: String): List<String> {
        val lines = text.split("\n")
        val reminders = mutableListOf<String>()
        
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            
            // 移除常见的列表标记
            val cleaned = trimmed
                .removePrefix("•")
                .removePrefix("-")
                .removePrefix("*")
                .removePrefix("□")
                .removePrefix("☐")
                .trim()
                .let { 
                    // 移除数字序号 (1. 2. 3. etc.)
                    if (it.matches(Regex("^\\d+\\.\\s*.*"))) {
                        it.replaceFirst(Regex("^\\d+\\.\\s*"), "")
                    } else {
                        it
                    }
                }
            
            if (cleaned.isNotBlank() && cleaned.length >= 2) {
                reminders.add(cleaned)
            }
        }
        
        return reminders
    }
    
    /**
     * 释放资源
     */
    fun release() {
        recognizer.close()
    }
}

/**
 * OCR 识别结果数据类
 */
data class OCRResult(
    val success: Boolean,
    val fullText: String,
    val textBlocks: List<TextBlock>,
    val error: String?
)

/**
 * 文本块
 */
data class TextBlock(
    val text: String,
    val boundingBox: android.graphics.Rect?,
    val lines: List<TextLine>
)

/**
 * 文本行
 */
data class TextLine(
    val text: String,
    val boundingBox: android.graphics.Rect?
)
