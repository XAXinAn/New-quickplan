package com.example.quickplan.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quickplan.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as android.app.Application
        )
    )
    
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val userInfo by authViewModel.userInfo.collectAsState()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isLoggedIn && userInfo != null) {
            LoggedInProfileScreen(authViewModel, userInfo!!)
        } else {
            NotLoggedInProfileScreen(navController)
        }
    }
}

@Composable
fun NotLoggedInProfileScreen(navController: NavController) {
    var darkMode by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "我的",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = { darkMode = !darkMode }) {
                Icon(
                    imageVector = if (darkMode) Icons.Default.Star else Icons.Default.Search,
                    contentDescription = "暗黑模式",
                    tint = Color(0xFF5A6F93)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "用户头像",
                modifier = Modifier.size(140.dp),
                tint = Color(0xFF5A6F93)
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5A6F93)
            )
        ) {
            Text(
                text = "登录",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "注册账号",
                color = Color(0xFF5A6F93),
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                }
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "悬浮窗",
                fontSize = 18.sp,
                color = Color.Black
            )
            
            Switch(
                checked = false,
                onCheckedChange = { },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF5A6F93),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "历史添加",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "历史添加",
                fontSize = 18.sp,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "更多",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "更多",
                fontSize = 18.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun LoggedInProfileScreen(authViewModel: AuthViewModel, userInfo: com.example.quickplan.data.api.UserInfo) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "用户头像",
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape),
            tint = Color(0xFF5A6F93)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = userInfo.nickname ?: "用户${userInfo.userId.take(6)}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "ID: ${userInfo.userId}",
            fontSize = 13.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            userInfo.phone?.let { phone ->
                ProfileInfoItem(
                    icon = Icons.Default.Phone,
                    label = "手机号",
                    value = phone
                )
            }
            
            userInfo.email?.let { email ->
                ProfileInfoItem(
                    icon = Icons.Default.Email,
                    label = "邮箱",
                    value = email
                )
            }
            
            ProfileInfoItem(
                icon = Icons.Default.Info,
                label = "登录方式",
                value = when (userInfo.loginType) {
                    "phone" -> "手机号"
                    "email" -> "邮箱"
                    "wechat" -> "微信"
                    "qq" -> "QQ"
                    else -> userInfo.loginType
                }
            )
            
            ProfileInfoItem(
                icon = Icons.Default.DateRange,
                label = "注册时间",
                value = userInfo.createdAt.take(10)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileMenuItem(
                icon = Icons.Default.DateRange,
                text = "历史添加",
                onClick = { }
            )
            
            ProfileMenuItem(
                icon = Icons.Default.Settings,
                text = "更多设置",
                onClick = { }
            )
            
            ProfileMenuItem(
                icon = Icons.Default.Phone,
                text = "联系我们",
                onClick = { }
            )
            
            ProfileMenuItem(
                icon = Icons.Default.Info,
                text = "关于QuickPlan",
                onClick = { }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "退出登录",
                color = Color.Red,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("退出登录") },
            text = { Text("确定要退出登录吗?") },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.logout()
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("退出")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun ProfileInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp),
                tint = Color(0xFF5A6F93)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(22.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "进入",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
