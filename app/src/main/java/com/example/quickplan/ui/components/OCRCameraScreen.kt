package com.example.quickplan.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.quickplan.utils.MLKitOCRHelper
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * OCR 相机屏幕
 * 使用 Google ML Kit 进行文字识别
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OCRCameraScreen(
    onRemindersExtracted: (List<String>) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var recognizedText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedBitmap = bitmap
                inputStream?.close()
                errorMessage = null
            } catch (e: IOException) {
                errorMessage = "无法加载图片: ${e.message}"
            }
        }
    }
    
    // 执行 OCR 识别
    fun performOCR(bitmap: Bitmap) {
        scope.launch {
            isProcessing = true
            errorMessage = null
            
            try {
                val result = MLKitOCRHelper.recognizeTextDetailed(bitmap)
                
                if (result.success) {
                    recognizedText = result.fullText
                    
                    // 提取待办事项
                    val reminders = MLKitOCRHelper.extractReminders(bitmap)
                    if (reminders.isNotEmpty()) {
                        onRemindersExtracted(reminders)
                    }
                } else {
                    errorMessage = result.error ?: "识别失败"
                }
            } catch (e: Exception) {
                errorMessage = "识别出错: ${e.message}"
            } finally {
                isProcessing = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("智能识别待办事项") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 选择图片按钮
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "从相册选择图片",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, "选择图片")
                        Spacer(Modifier.width(8.dp))
                        Text("选择图片")
                    }
                }
            }
            
            // 显示选中的图片
            selectedBitmap?.let { bitmap ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "已选择的图片",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "选中的图片",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 开始识别按钮
                        Button(
                            onClick = { performOCR(bitmap) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isProcessing
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("识别中...")
                            } else {
                                Icon(Icons.Default.TextFields, "识别")
                                Spacer(Modifier.width(8.dp))
                                Text("开始识别")
                            }
                        }
                    }
                }
            }
            
            // 显示错误信息
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // 显示识别结果
            if (recognizedText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "识别结果",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = recognizedText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // 使用说明
            if (selectedBitmap == null) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "使用提示",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = """
                                1. 点击"选择图片"从相册选择待办事项图片
                                2. 支持识别手写或打印的文字
                                3. 识别后自动提取待办事项
                                4. 支持的格式：
                                   • 带序号的列表 (1. xxx, 2. xxx)
                                   • 带符号的列表 (- xxx, * xxx)
                                   • 每行一个待办事项
                            """.trimIndent(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
    
    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            MLKitOCRHelper.release()
        }
    }
}
