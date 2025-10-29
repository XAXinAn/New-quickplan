package com.example.quickplan.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.InputStream

/**
 * 图片选择器组件
 * 支持从相册选择图片用于 OCR 识别
 */
@Composable
fun ImagePickerButton(
    onImageSelected: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showError by remember { mutableStateOf<String?>(null) }
    
    // 图片选择启动器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (bitmap != null) {
                    onImageSelected(bitmap)
                } else {
                    showError = "无法加载图片"
                }
            } catch (e: Exception) {
                showError = "图片加载失败: ${e.message}"
            }
        }
    }
    
    Column(modifier = modifier) {
        FloatingActionButton(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "选择图片进行OCR识别"
            )
        }
        
        // 错误提示
        showError?.let { error ->
            Snackbar(
                modifier = Modifier.padding(8.dp),
                action = {
                    TextButton(onClick = { showError = null }) {
                        Text("关闭")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}
