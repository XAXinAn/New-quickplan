# 后端 OCR 提醒功能开发文档

## 📋 功能概述

在现有 AI 对话系统的基础上，添加 OCR 图片识别创建提醒功能。用户在前端上传图片后，前端使用本地 PaddleOCR 模型识别文字，将识别结果发送到后端，后端通过 AI 大模型解析提醒信息并自动创建提醒事项。

---

## 🔧 需要修改的内容

### 1. 创建 OCR 相关的数据模型

**路径**: `src/main/java/com/example/quickplan/dto/` (或对应的 DTO 包)

**文件名**: `OCRReminderDTO.java`

```java
package com.example.quickplan.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * OCR 提醒请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OCRReminderRequest {
    /**
     * 对话 ID (memoryId)
     */
    private String memoryId;
    
    /**
     * 用户 ID
     */
    private String userId;
    
    /**
     * OCR 识别出的文本内容
     */
    private String ocrText;
}

/**
 * OCR 提醒响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OCRReminderResponse {
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 提醒数据 (可选)
     */
    private ReminderData data;
}

/**
 * 提醒数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReminderData {
    /**
     * 提醒 ID
     */
    private String reminderId;
    
    /**
     * 提醒标题
     */
    private String title;
    
    /**
     * 提醒时间
     */
    private String time;
    
    /**
     * 提醒描述
     */
    private String description;
}
```

---

### 2. 在 AI Controller 中添加新的接口

**路径**: `src/main/java/com/example/quickplan/controller/AiController.java`

**添加方法**:

```java
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

/**
 * OCR 图片识别并创建提醒
 * 
 * @param request OCR 识别请求
 * @return OCR 识别响应
 */
@PostMapping("/ocr/reminder")
public ResponseEntity<OCRReminderResponse> createReminderFromOCR(
        @RequestBody OCRReminderRequest request) {
    
    try {
        // 1. 验证参数
        if (request.getOcrText() == null || request.getOcrText().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                new OCRReminderResponse(false, "OCR 文本不能为空", null)
            );
        }
        
        // 2. 调用 AI 服务解析 OCR 文本并创建提醒
        OCRReminderResponse response = aiService.processOCRAndCreateReminder(
            request.getMemoryId(),
            request.getUserId(),
            request.getOcrText()
        );
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        log.error("OCR 提醒创建失败", e);
        return ResponseEntity.internalServerError().body(
            new OCRReminderResponse(false, "服务器错误: " + e.getMessage(), null)
        );
    }
}
```

---

### 3. 在 AI Service 中实现 OCR 处理逻辑

**路径**: `src/main/java/com/example/quickplan/service/AiService.java`

**添加方法**:

```java
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 处理 OCR 文本并创建提醒
 * 
 * @param memoryId 对话 ID
 * @param userId 用户 ID
 * @param ocrText OCR 识别的文本
 * @return OCR 响应
 */
public OCRReminderResponse processOCRAndCreateReminder(
        String memoryId, String userId, String ocrText) {
    
    try {
        // 1. 构建 AI Prompt
        String prompt = buildOCRPrompt(ocrText);
        
        // 2. 调用大模型分析 OCR 文本
        UserMessage userMessage = UserMessage.from(prompt);
        AiMessage aiResponse = chatLanguageModel.generate(userMessage).content();
        String aiResponseText = aiResponse.text();
        
        // 3. 解析 AI 返回的 JSON 结果
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(aiResponseText);
        
        // 4. 提取提醒信息
        String title = jsonNode.get("title").asText();
        String time = jsonNode.has("time") ? jsonNode.get("time").asText() : null;
        String description = jsonNode.has("description") ? jsonNode.get("description").asText() : null;
        
        // 5. 调用提醒创建服务
        String reminderId = reminderService.createReminder(userId, title, time, description);
        
        // 6. 构建响应
        ReminderData reminderData = new ReminderData(reminderId, title, time, description);
        
        return new OCRReminderResponse(
            true,
            "提醒创建成功",
            reminderData
        );
        
    } catch (Exception e) {
        log.error("OCR 提醒处理失败", e);
        return new OCRReminderResponse(
            false,
            "提醒创建失败: " + e.getMessage(),
            null
        );
    }
}

/**
 * 构建 OCR 提示词
 * 
 * @param ocrText OCR 识别的文本
 * @return AI Prompt
 */
private String buildOCRPrompt(String ocrText) {
    return String.format("""
        你是一个智能提醒助手。用户上传了一张图片，经过 OCR 识别后得到以下文本内容：
        
        ---
        %s
        ---
        
        请分析这段文本，提取出提醒事项的关键信息，并以 JSON 格式返回。
        
        要求：
        1. 提取提醒的标题 (title) - 必填，简洁明了
        2. 提取提醒的时间 (time) - 选填，格式为 "yyyy-MM-dd HH:mm"，如果文本中没有明确时间则返回 null
        3. 提取提醒的描述 (description) - 选填，补充说明信息
        
        返回格式示例：
        ```json
        {
            "title": "参加公司会议",
            "time": "2025-10-27 14:30",
            "description": "讨论Q4季度目标，会议室：301"
        }
        ```
        
        如果文本中没有明确的提醒信息，请根据内容智能推断一个合理的提醒。
        只返回 JSON，不要返回其他内容。
        """, ocrText);
}
```

---

### 4. 创建或修改 ReminderService

**路径**: `src/main/java/com/example/quickplan/service/ReminderService.java`

如果项目中还没有提醒服务，需要创建一个：

```java
package com.example.quickplan.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.quickplan.mapper.ReminderMapper;
import com.example.quickplan.entity.Reminder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ReminderService {
    
    @Autowired
    private ReminderMapper reminderMapper;
    
    /**
     * 创建提醒
     * 
     * @param userId 用户 ID
     * @param title 提醒标题
     * @param time 提醒时间 (可选)
     * @param description 提醒描述 (可选)
     * @return 提醒 ID
     */
    public String createReminder(String userId, String title, String time, String description) {
        Reminder reminder = new Reminder();
        reminder.setId(generateReminderId());
        reminder.setUserId(userId);
        reminder.setTitle(title);
        reminder.setDescription(description);
        
        // 解析时间字符串
        if (time != null && !time.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime reminderTime = LocalDateTime.parse(time, formatter);
                reminder.setReminderTime(reminderTime);
            } catch (Exception e) {
                // 时间格式错误，使用默认时间（当前时间 + 1 小时）
                reminder.setReminderTime(LocalDateTime.now().plusHours(1));
            }
        } else {
            // 没有指定时间，默认为当前时间 + 1 小时
            reminder.setReminderTime(LocalDateTime.now().plusHours(1));
        }
        
        reminder.setStatus("pending");
        reminder.setCreatedAt(LocalDateTime.now());
        reminder.setUpdatedAt(LocalDateTime.now());
        
        reminderMapper.insert(reminder);
        
        return reminder.getId();
    }
    
    /**
     * 生成提醒 ID
     */
    private String generateReminderId() {
        return "reminder_" + System.currentTimeMillis() + "_" + 
               String.format("%04d", (int)(Math.random() * 10000));
    }
}
```

---

### 5. 数据库相关 (如果需要)

**表名**: `reminders`

如果数据库中还没有提醒表，需要创建：

```sql
CREATE TABLE IF NOT EXISTS reminders (
    id VARCHAR(64) PRIMARY KEY COMMENT '提醒ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    title VARCHAR(255) NOT NULL COMMENT '提醒标题',
    description TEXT COMMENT '提醒描述',
    reminder_time DATETIME NOT NULL COMMENT '提醒时间',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending, completed, cancelled',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_reminder_time (reminder_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒事项表';
```

**Mapper 接口**: `src/main/java/com/example/quickplan/mapper/ReminderMapper.java`

```java
package com.example.quickplan.mapper;

import com.example.quickplan.entity.Reminder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ReminderMapper {
    
    @Insert("INSERT INTO reminders (id, user_id, title, description, reminder_time, status, created_at, updated_at) " +
            "VALUES (#{id}, #{userId}, #{title}, #{description}, #{reminderTime}, #{status}, #{createdAt}, #{updatedAt})")
    int insert(Reminder reminder);
    
    @Select("SELECT * FROM reminders WHERE id = #{id}")
    Reminder selectById(String id);
    
    @Select("SELECT * FROM reminders WHERE user_id = #{userId} ORDER BY reminder_time DESC")
    List<Reminder> selectByUserId(String userId);
}
```

**Entity 类**: `src/main/java/com/example/quickplan/entity/Reminder.java`

```java
package com.example.quickplan.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Reminder {
    private String id;
    private String userId;
    private String title;
    private String description;
    private LocalDateTime reminderTime;
    private String status; // pending, completed, cancelled
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## 🔄 API 调用流程

```
1. 用户在前端选择图片
   ↓
2. 前端使用本地 PaddleOCR 识别文字
   ↓
3. 前端发送 POST /api/ai/ocr/reminder
   请求体: {
       "memoryId": "conversation_xxx",
       "userId": "user_001",
       "ocrText": "明天下午3点开会,讨论项目进度"
   }
   ↓
4. 后端 AiController.createReminderFromOCR() 接收请求
   ↓
5. 调用 AiService.processOCRAndCreateReminder()
   ↓
6. 使用大模型分析 OCR 文本，提取提醒信息
   ↓
7. 调用 ReminderService.createReminder() 创建提醒
   ↓
8. 返回响应给前端:
   {
       "success": true,
       "message": "提醒创建成功",
       "data": {
           "reminderId": "reminder_xxx",
           "title": "参加会议",
           "time": "2025-10-27 15:00",
           "description": "讨论项目进度"
       }
   }
```

---

## 📝 重要提示

### 1. AI Prompt 优化建议

- 根据实际使用情况调整 `buildOCRPrompt()` 中的提示词
- 可以添加更多上下文信息，如当前时间、用户历史提醒等
- 如果 AI 返回的 JSON 格式不稳定，可以使用正则表达式或更健壮的解析逻辑

### 2. 时间解析优化

- 当前只支持 `yyyy-MM-dd HH:mm` 格式
- 可以扩展支持更多格式，如 "明天下午3点"、"下周一上午10点" 等自然语言
- 建议使用专门的时间解析库，如 `prettytime-nlp` 或 `natty`

### 3. 错误处理

- 添加更详细的日志记录
- 对 AI 返回的异常结果进行兜底处理
- 如果 OCR 文本质量太差，可以返回友好提示让用户重新上传

### 4. 性能优化

- 如果 AI 调用耗时较长，考虑使用异步处理
- 可以添加缓存机制，避免重复识别相同图片
- 考虑添加限流，防止恶意大量请求

### 5. 安全性

- 验证 userId 和 memoryId 的有效性
- 对 OCR 文本内容进行敏感信息过滤
- 添加用户权限验证，确保只能操作自己的提醒

---

## 🧪 测试建议

### 测试用例 1: 明确时间的提醒

**OCR 文本**:
```
会议通知
时间：2025年10月28日 下午2:30
地点：会议室 A
主题：季度总结
```

**预期结果**:
```json
{
    "success": true,
    "message": "提醒创建成功",
    "data": {
        "title": "季度总结会议",
        "time": "2025-10-28 14:30",
        "description": "地点：会议室 A"
    }
}
```

### 测试用例 2: 模糊时间的提醒

**OCR 文本**:
```
记得明天早上买菜
需要买：西红柿、鸡蛋、青菜
```

**预期结果**: AI 应该能推断出大概时间（如明天上午 8:00）

### 测试用例 3: 无明确提醒的文本

**OCR 文本**:
```
这是一段普通的文字内容，
没有任何提醒信息。
```

**预期结果**: AI 应该返回提示或拒绝创建提醒

---

## 📦 依赖说明

确保 `pom.xml` 或 `build.gradle` 中包含以下依赖：

```xml
<!-- LangChain4j -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j</artifactId>
    <version>最新版本</version>
</dependency>

<!-- JSON 处理 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- MyBatis -->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

---

## 🚀 部署前检查

- [ ] 数据库表已创建
- [ ] Mapper 接口正常工作
- [ ] AI 模型配置正确 (API Key、模型名称等)
- [ ] 前后端接口联调通过
- [ ] 日志记录完善
- [ ] 异常处理完整
- [ ] 单元测试通过
- [ ] 接口文档已更新

---

## 📞 联系方式

如有问题，请联系开发团队或查看项目文档。
