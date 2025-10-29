package com.example.quickplan.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quickplan.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("手机号注册", "邮箱注册")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("注册账号") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            // Tab选择器
            TabRow(
                selectedTabIndex = selectedTab
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 根据选中的Tab显示不同的注册表单
            when (selectedTab) {
                0 -> PhoneRegisterForm(authViewModel, navController)
                1 -> EmailRegisterForm(authViewModel, navController)
            }
        }
    }
}

/**
 * 手机号注册表单
 */
@Composable
fun PhoneRegisterForm(authViewModel: AuthViewModel, navController: NavController) {
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }
    
    val countdown by authViewModel.countdown.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    // 注册成功后返回
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate("profile") {
                popUpTo("register") { inclusive = true }
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 错误提示
        errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        
        // 昵称输入
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("昵称(可选)") },
            placeholder = { Text("请输入昵称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Person, "昵称")
            }
        )
        
        // 手机号输入
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("手机号") },
            placeholder = { Text("请输入手机号") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Phone, "手机号")
            }
        )
        
        // 验证码输入和发送按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("验证码") },
                placeholder = { Text("请输入验证码") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Lock, "验证码")
                }
            )
            
            Button(
                onClick = { authViewModel.sendVerificationCode(phone) },
                enabled = countdown == 0 && !isLoading && phone.isNotEmpty(),
                modifier = Modifier
                    .height(56.dp)
                    .widthIn(min = 120.dp)
            ) {
                Text(
                    text = if (countdown > 0) "${countdown}s" else "获取验证码",
                    fontSize = 14.sp
                )
            }
        }
        
        // 密码输入
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("设置密码") },
            placeholder = { Text("请设置登录密码") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            leadingIcon = {
                Icon(Icons.Default.Lock, "密码")
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Lock else Icons.Default.Lock,
                        "显示密码"
                    )
                }
            }
        )
        
        // 确认密码输入
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("确认密码") },
            placeholder = { Text("请再次输入密码") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            leadingIcon = {
                Icon(Icons.Default.Lock, "确认密码")
            },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        if (confirmPasswordVisible) Icons.Default.Lock else Icons.Default.Lock,
                        "显示密码"
                    )
                }
            }
        )
        
        // 用户协议
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = agreeTerms,
                onCheckedChange = { agreeTerms = it }
            )
            Text(
                text = "我已阅读并同意",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "《用户协议》",
                fontSize = 14.sp,
                color = Color(0xFF5A6F93),
                modifier = Modifier.clickable {
                    // TODO: 跳转到用户协议页面
                }
            )
            Text(
                text = "和",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "《隐私政策》",
                fontSize = 14.sp,
                color = Color(0xFF5A6F93),
                modifier = Modifier.clickable {
                    // TODO: 跳转到隐私政策页面
                }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 注册按钮
        Button(
            onClick = {
                if (!agreeTerms) {
                    // 这里需要一个临时的错误提示,但最好通过ViewModel
                    return@Button
                }
                // 调用注册接口,ViewModel会处理所有验证
                authViewModel.phoneRegister(phone, code, password, confirmPassword, nickname.ifBlank { null })
            },
            enabled = !isLoading && phone.isNotEmpty() && code.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && agreeTerms,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5A6F93)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "注册",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // 已有账号提示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "已有账号? ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = "立即登录",
                color = Color(0xFF5A6F93),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
    }
}

/**
 * 邮箱注册表单
 */
@Composable
fun EmailRegisterForm(authViewModel: AuthViewModel, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }
    
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    // 注册成功后返回
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate("profile") {
                popUpTo("register") { inclusive = true }
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 错误提示
        errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        
        // 昵称输入
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("昵称(可选)") },
            placeholder = { Text("请输入昵称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Person, "昵称")
            }
        )
        
        // 邮箱输入
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("邮箱地址") },
            placeholder = { Text("请输入邮箱地址") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Email, "邮箱")
            }
        )
        
        // 密码输入
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("设置密码") },
            placeholder = { Text("请设置登录密码") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            leadingIcon = {
                Icon(Icons.Default.Lock, "密码")
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Lock else Icons.Default.Lock,
                        "显示密码"
                    )
                }
            }
        )
        
        // 确认密码输入
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("确认密码") },
            placeholder = { Text("请再次输入密码") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            leadingIcon = {
                Icon(Icons.Default.Lock, "确认密码")
            },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        if (confirmPasswordVisible) Icons.Default.Lock else Icons.Default.Lock,
                        "显示密码"
                    )
                }
            }
        )
        
        // 用户协议
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = agreeTerms,
                onCheckedChange = { agreeTerms = it }
            )
            Text(
                text = "我已阅读并同意",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "《用户协议》",
                fontSize = 14.sp,
                color = Color(0xFF5A6F93),
                modifier = Modifier.clickable {
                    // TODO: 跳转到用户协议页面
                }
            )
            Text(
                text = "和",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "《隐私政策》",
                fontSize = 14.sp,
                color = Color(0xFF5A6F93),
                modifier = Modifier.clickable {
                    // TODO: 跳转到隐私政策页面
                }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 注册按钮
        Button(
            onClick = {
                if (!agreeTerms) {
                    return@Button
                }
                // 调用注册接口,ViewModel会处理所有验证
                authViewModel.emailRegister(email, password, confirmPassword, nickname.ifBlank { null })
            },
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && agreeTerms,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5A6F93)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "注册",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // 已有账号提示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "已有账号? ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = "立即登录",
                color = Color(0xFF5A6F93),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
    }
}
