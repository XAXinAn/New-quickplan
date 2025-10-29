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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quickplan.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("手机号登录", "邮箱登录", "微信登录", "QQ登录")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("登录") },
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
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp
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
            
            // 根据选中的Tab显示不同的登录表单
            when (selectedTab) {
                0 -> PhoneLoginForm(authViewModel, navController)
                1 -> EmailLoginForm(authViewModel, navController)
                2 -> ThirdPartyLoginForm(authViewModel, "微信", navController)
                3 -> ThirdPartyLoginForm(authViewModel, "QQ", navController)
            }
        }
    }
}

/**
 * 手机号登录表单
 */
@Composable
fun PhoneLoginForm(authViewModel: AuthViewModel, navController: NavController) {
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    val countdown by authViewModel.countdown.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    // 登录成功后返回
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigateUp()
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 登录按钮
        Button(
            onClick = { authViewModel.phoneLogin(phone, code) },
            enabled = !isLoading && phone.isNotEmpty() && code.isNotEmpty(),
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
                    text = "登录",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // 注册提示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "还没有账号? ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = "立即注册",
                color = Color(0xFF5A6F93),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                }
            )
        }
    }
}

/**
 * 邮箱登录表单
 */
@Composable
fun EmailLoginForm(authViewModel: AuthViewModel, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    // 登录成功后返回
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigateUp()
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
            label = { Text("密码") },
            placeholder = { Text("请输入密码") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                androidx.compose.ui.text.input.VisualTransformation.None
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
        
        // 忘记密码
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "忘记密码?",
                color = Color(0xFF5A6F93),
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    // TODO: 跳转到忘记密码页面
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 登录按钮
        Button(
            onClick = { authViewModel.emailLogin(email, password) },
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty(),
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
                    text = "登录",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // 注册提示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "还没有账号? ",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = "立即注册",
                color = Color(0xFF5A6F93),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                }
            )
        }
    }
}

/**
 * 第三方登录表单 (微信/QQ)
 */
@Composable
fun ThirdPartyLoginForm(authViewModel: AuthViewModel, loginType: String, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 第三方图标
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = loginType,
            modifier = Modifier.size(120.dp),
            tint = Color(0xFF5A6F93)
        )
        
        Text(
            text = "使用${loginType}登录",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "请确保已安装最新版本的$loginType",
            fontSize = 14.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                when (loginType) {
                    "微信" -> authViewModel.wechatLogin("wechat_code_placeholder")
                    "QQ" -> authViewModel.qqLogin("qq_access_token_placeholder", "qq_openid_placeholder")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5A6F93)
            )
        ) {
            Text(
                text = "授权并登录",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        TextButton(
            onClick = { navController.navigateUp() }
        ) {
            Text("返回其他登录方式")
        }
    }
}
