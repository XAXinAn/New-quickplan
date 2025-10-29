# OCR 功能待办事项清单

## ✅ 已完成

- [x] 添加 PaddleOCR 和 Coil 依赖到 `build.gradle.kts`
- [x] 添加相机和存储权限到 `AndroidManifest.xml`
- [x] 创建 `PaddleOCRHelper.kt` OCR 辅助类
- [x] 在 `ApiModels.kt` 中添加 OCR 请求/响应模型
- [x] 在 `AiApiService.kt` 中添加 OCR API 端点
- [x] 创建 `ImagePicker.kt` 图片选择组件
- [x] 修改 `AiViewModel.kt` 添加 OCR 处理逻辑
- [x] 修改 `AIScreen.kt` 集成图片选择按钮
- [x] 创建后端修改文档 `后端OCR功能修改文档.md`
- [x] 创建使用指南 `OCR功能使用指南.md`

---

## 📋 待完成 (前端)

### 1. 准备 OCR 模型文件 ⚠️ **重要**

**优先级**: 🔴 最高

**任务**:
1. 下载 PaddleOCR 模型文件:
   - `ch_PP-OCRv4_det_infer.nb` (文字检测模型)
   - `ch_PP-OCRv4_rec_infer.nb` (文字识别模型)
   - `ppocr_keys_v1.txt` (字符字典)

2. 创建目录并放置文件:
   ```
   app/src/main/assets/ocr/
   ├── ch_PP-OCRv4_det_infer.nb
   ├── ch_PP-OCRv4_rec_infer.nb
   └── ppocr_keys_v1.txt
   ```

**下载地址**: 参见 `OCR功能使用指南.md` 中的"模型下载地址"部分

**注意**: 
- 如果没有这些文件,应用启动时会报错
- 模型文件需要转换为 `.nb` 格式 (使用 PaddleLite opt 工具)

---

### 2. 测试前端 OCR 功能

**优先级**: 🟡 中

**任务**:
1. 运行应用,打开 AI 对话界面
2. 点击图片选择按钮,选择一张包含文字的图片
3. 观察 Logcat 日志,确认:
   - OCR 模型初始化成功
   - 图片识别正常
   - 识别结果显示在消息列表
4. 测试不同类型的图片:
   - 印刷体文字 (书籍、海报)
   - 手写文字 (便签、笔记)
   - 复杂背景图片

**检查点**:
- [ ] OCR 模型加载无错误
- [ ] 图片选择功能正常
- [ ] 文字识别准确
- [ ] UI 反馈及时 (加载提示、错误提示)

---

### 3. 性能优化 (可选)

**优先级**: 🟢 低

**任务**:
1. **图片压缩**: 在识别前压缩大图片,减少内存占用
2. **缓存优化**: 对相同图片避免重复识别
3. **进度提示**: 添加更详细的识别进度条
4. **错误重试**: 识别失败时允许用户重新尝试

---

## 📋 待完成 (后端)

### 1. 实现 OCR API 接口 ⚠️ **必须**

**优先级**: 🔴 最高

**任务**: 参考 `后端OCR功能修改文档.md`,完成以下开发:

1. **创建 DTO 类**:
   - `OCRReminderRequest.java`
   - `OCRReminderResponse.java`
   - `ReminderData.java`

2. **修改 Controller**:
   - 在 `AiController.java` 中添加 `createReminderFromOCR()` 方法

3. **修改 Service**:
   - 在 `AiService.java` 中添加 `processOCRAndCreateReminder()` 方法
   - 实现 `buildOCRPrompt()` 方法构建 AI Prompt

4. **创建/修改 ReminderService**:
   - 实现 `createReminder()` 方法
   - 处理时间解析逻辑

5. **数据库相关**:
   - 创建 `reminders` 表 (如果不存在)
   - 创建 `ReminderMapper.java`
   - 创建 `Reminder.java` 实体类

**检查点**:
- [ ] API 接口可访问: `POST /api/ai/ocr/reminder`
- [ ] 能正确解析 OCR 文本
- [ ] AI 模型返回合理的提醒信息
- [ ] 提醒成功保存到数据库
- [ ] 返回正确的响应格式

---

### 2. 测试后端 API

**优先级**: 🟡 中

**测试方法**: 使用 Postman 或 curl

**测试用例 1**:
```bash
curl -X POST http://localhost:8080/api/ai/ocr/reminder \
  -H "Content-Type: application/json" \
  -d '{
    "memoryId": "test_memory_001",
    "userId": "test_user_001",
    "ocrText": "明天下午3点开会,讨论项目进度"
  }'
```

**预期响应**:
```json
{
  "success": true,
  "message": "提醒创建成功",
  "data": {
    "reminderId": "reminder_xxx",
    "title": "项目进度会议",
    "time": "2025-10-27 15:00",
    "description": "讨论项目进度"
  }
}
```

**测试用例 2**: 测试错误处理
```bash
curl -X POST http://localhost:8080/api/ai/ocr/reminder \
  -H "Content-Type: application/json" \
  -d '{
    "memoryId": "test_memory_001",
    "userId": "test_user_001",
    "ocrText": ""
  }'
```

**预期响应**:
```json
{
  "success": false,
  "message": "OCR 文本不能为空",
  "data": null
}
```

---

### 3. AI Prompt 优化 (可选)

**优先级**: 🟢 低

**任务**:
1. 根据实际测试结果调整 Prompt
2. 添加更多示例和上下文
3. 处理边缘情况 (如模糊的时间表达)
4. 支持多语言 (英文、繁体中文等)

---

## 🔗 前后端联调

**优先级**: 🔴 最高

**前提条件**:
- 前端 OCR 功能正常
- 后端 API 接口实现完成

**测试步骤**:
1. 启动后端服务
2. 在前端 `RetrofitClient.kt` 中配置正确的 baseUrl
3. 运行 Android 应用
4. 上传包含提醒信息的图片
5. 观察:
   - 前端是否正确调用 API
   - 后端是否正常处理请求
   - 提醒是否成功创建
   - 前端是否正确显示结果

**检查点**:
- [ ] 网络请求成功 (HTTP 200)
- [ ] OCR 文本正确传输
- [ ] AI 分析结果合理
- [ ] 提醒创建成功
- [ ] 前端显示创建结果

---

## 📝 文档完善 (可选)

**优先级**: 🟢 低

1. **用户手册**: 为最终用户编写简单的使用说明
2. **API 文档**: 使用 Swagger 生成后端 API 文档
3. **开发文档**: 补充架构设计和代码注释
4. **测试报告**: 记录测试用例和测试结果

---

## 🚀 部署准备

**优先级**: 🟡 中

**前端**:
- [ ] 打包 APK 时确保模型文件被正确包含
- [ ] 测试不同 Android 版本 (Android 12, 13, 14+)
- [ ] 测试不同设备性能 (高端、中端、低端)
- [ ] 优化 APK 大小 (模型文件占用约 15 MB)

**后端**:
- [ ] 配置生产环境数据库
- [ ] 配置 AI 模型 API Key
- [ ] 添加监控和日志
- [ ] 性能测试和压力测试
- [ ] 配置 HTTPS 和安全策略

---

## ⏰ 时间估算

| 任务 | 预计时间 | 负责人 |
|------|---------|--------|
| 准备 OCR 模型文件 | 1-2 小时 | 前端开发 |
| 测试前端 OCR 功能 | 2-4 小时 | 前端开发 |
| 实现后端 API 接口 | 4-6 小时 | 后端开发 |
| 测试后端 API | 1-2 小时 | 后端开发 |
| 前后端联调 | 2-3 小时 | 前后端开发 |
| 优化和文档 | 2-4 小时 | 全员 |
| **总计** | **12-21 小时** | |

---

## 📞 遇到问题？

1. **查看文档**:
   - `后端OCR功能修改文档.md` - 后端开发指南
   - `OCR功能使用指南.md` - 使用和配置指南

2. **检查日志**:
   - 前端: Android Logcat
   - 后端: 应用日志文件

3. **联系团队**: 如有疑问,及时沟通

---

## ✨ 下一步计划

完成上述任务后,可以考虑以下增强功能:

1. **多图片上传**: 支持一次上传多张图片
2. **图片预览**: 上传前可以预览和编辑图片
3. **OCR 结果编辑**: 允许用户手动修改识别结果
4. **提醒管理**: 在应用中查看、编辑、删除已创建的提醒
5. **语音输入**: 结合语音识别功能
6. **智能推荐**: 根据历史提醒推荐时间和类别

---

**祝开发顺利! 🎉**
