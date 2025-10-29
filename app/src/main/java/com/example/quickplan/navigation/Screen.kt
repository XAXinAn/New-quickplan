package com.example.quickplan.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "首页")
    object AI : Screen("ai", "AI")
    object Profile : Screen("profile", "我的")

    // 登录路由
    object PhoneLogin : Screen("phone_login", "手机号登录")
    object WeChatLogin : Screen("wechat_login", "微信登录")
    object QQLogin : Screen("qq_login", "QQ登录")
    object EmailLogin : Screen("email_login", "邮箱登录")

    // 注册路由
    object PhoneRegister : Screen("phone_register", "手机号注册")
    object WeChatRegister : Screen("wechat_register", "微信注册")
    object QQRegister : Screen("qq_register", "QQ注册")
    object EmailRegister : Screen("email_register", "邮箱注册")
    // 新增路由
    object History : Screen("history", "历史添加")
    object More : Screen("more", "更多")
    object AccountCancel : Screen("account_cancel", "注销账号")
    object ContactUs : Screen("contact_us", "联系我们")
    object About : Screen("about", "关于QuickPlan")
    object Privacy : Screen("privacy", "用户隐私协议")
}