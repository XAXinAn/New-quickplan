# 🔄 OCR 模型转换指南 (最新版)

## ⚠️ 重要提示

之前文档中的 `opt` 工具下载链接已失效 (404 错误)!  
本文档提供**最新可用**的下载和转换方法。

---

## 📌 您当前的状态

✅ 已下载并解压的文件:
```
ch_PP-OCRv4_det_infer/
├── inference.pdiparams (4,583 KB)
├── inference.pdmodel (163 KB)
└── inference.pdiparams.info (24 KB)

ch_PP-OCRv4_rec_infer/
├── inference.pdiparams (10,515 KB)
├── inference.pdmodel (166 KB)
└── inference.pdiparams.info (30 KB)
```

❓ 还需要完成:
1. 下载字典文件 `ppocr_keys_v1.txt`
2. 将 `.pdmodel` 和 `.pdiparams` 转换为 `.nb` 格式

---

## 🎯 方案选择

### 🌟 推荐方案 1: 使用预转换好的模型 (最简单)

如果您不想自己转换,可以尝试以下方式:

1. **搜索资源**:
   - 在 CSDN 搜索: `PaddleOCR Android nb 模型`
   - 在百度网盘搜索: `ch_PP-OCRv4 nb`
   - 在 GitHub Issues 搜索相关分享

2. **使用官方 Demo 中的模型**:
   ```bash
   # 克隆 PaddleOCR 仓库
   git clone https://github.com/PaddlePaddle/PaddleOCR.git
   
   # Android Demo 中可能包含已转换的模型
   cd PaddleOCR/deploy/android_demo
   ```

---

### 🔧 方案 2: 自己转换 (需要 Linux 或 Mac)

#### 第 1 步: 下载 PaddleLite opt 工具

**最新下载地址 (v2.14-rc - 2024年7月)**:

##### Linux 系统:
```bash
# 下载完整的预测库 (包含 opt 工具)
wget https://github.com/PaddlePaddle/Paddle-Lite/releases/download/v2.14-rc/inference_lite_lib.android.armv8.clang.c++_shared.with_extra.with_cv.tar.gz

# 如果 GitHub 访问慢,使用镜像:
wget https://ghproxy.com/https://github.com/PaddlePaddle/Paddle-Lite/releases/download/v2.14-rc/inference_lite_lib.android.armv8.clang.c++_shared.with_extra.with_cv.tar.gz

# 解压
tar -xzf inference_lite_lib.android.armv8.clang.c++_shared.with_extra.with_cv.tar.gz

# opt 工具在 bin 目录
cd inference_lite_lib.android.armv8.with_extra.with_cv/bin
chmod +x ./opt

# 测试工具是否可用
./opt --help
```

##### macOS 系统 (M1/M2/M3):
```bash
# 下载 macOS ARM64 版本
wget https://github.com/PaddlePaddle/Paddle-Lite/releases/download/v2.14-rc/inference_lite_lib.macOS.clang.arm64.tar.gz

# 解压
tar -xzf inference_lite_lib.macOS.clang.arm64.tar.gz
cd inference_lite_lib.macOS.arm64/bin
chmod +x ./opt

# 测试
./opt --help
```

##### macOS 系统 (Intel x86):
```bash
# 下载 macOS x86 版本
wget https://github.com/PaddlePaddle/Paddle-Lite/releases/download/v2.14-rc/inference_lite_lib.macOS.clang.x86.tar.gz

tar -xzf inference_lite_lib.macOS.clang.x86.tar.gz
cd inference_lite_lib.macOS.x86/bin
chmod +x ./opt
```

##### Windows 系统:
Windows 用户有两个选择:

**选项 A: 使用 WSL (推荐)**
```powershell
# 在 PowerShell 中安装 WSL
wsl --install

# 重启后,在 WSL 中执行 Linux 的下载命令
wsl
# 然后执行上面 Linux 系统的命令
```

**选项 B: 使用虚拟机**  
安装 VMware 或 VirtualBox,创建 Ubuntu 虚拟机,然后执行 Linux 命令。

---

#### 第 2 步: 转换模型

将已解压的模型文件夹移动到 opt 工具所在目录,然后执行:

```bash
# 假设您的模型在当前目录
# 转换检测模型
./opt \
  --model_file=./ch_PP-OCRv4_det_infer/inference.pdmodel \
  --param_file=./ch_PP-OCRv4_det_infer/inference.pdiparams \
  --optimize_out=./ch_PP-OCRv4_det_infer \
  --valid_targets=arm

# 转换识别模型
./opt \
  --model_file=./ch_PP-OCRv4_rec_infer/inference.pdmodel \
  --param_file=./ch_PP-OCRv4_rec_infer/inference.pdiparams \
  --optimize_out=./ch_PP-OCRv4_rec_infer \
  --valid_targets=arm
```

转换成功后,会生成以下文件:
- `ch_PP-OCRv4_det_infer.nb` (检测模型)
- `ch_PP-OCRv4_rec_infer.nb` (识别模型)

---

#### 第 3 步: 下载字典文件

```bash
# 方式 1: 官方 GitHub Raw (可能需要翻墙)
curl -o ppocr_keys_v1.txt https://raw.githubusercontent.com/PaddlePaddle/PaddleOCR/release/2.7/ppocr/utils/ppocr_keys_v1.txt

# 方式 2: 使用镜像
curl -o ppocr_keys_v1.txt https://ghproxy.com/https://raw.githubusercontent.com/PaddlePaddle/PaddleOCR/release/2.7/ppocr/utils/ppocr_keys_v1.txt

# 方式 3: 手动下载并保存
# 访问: https://gitee.com/paddlepaddle/PaddleOCR/raw/release/2.7/ppocr/utils/ppocr_keys_v1.txt
# 右键另存为 ppocr_keys_v1.txt
```

---

#### 第 4 步: 放置到 Android 项目

将以下 3 个文件复制到项目的 `app/src/main/assets/ocr/` 目录:

```
app/src/main/assets/ocr/
├── ch_PP-OCRv4_det_infer.nb      ← 检测模型
├── ch_PP-OCRv4_rec_infer.nb      ← 识别模型
└── ppocr_keys_v1.txt             ← 字典文件
```

---

## 🆘 遇到问题?

### 问题 1: GitHub 无法访问或下载很慢

**解决方案**:
- 使用 `ghproxy.com` 镜像 (所有链接前加 `https://ghproxy.com/`)
- 使用 Gitee 镜像源
- 使用科学上网工具

### 问题 2: opt 工具提示 "permission denied"

```bash
# 确保有执行权限
chmod +x ./opt
```

### 问题 3: 转换时提示找不到文件

```bash
# 确保路径正确,使用绝对路径
ls -la ./ch_PP-OCRv4_det_infer/

# 应该看到 inference.pdmodel 和 inference.pdiparams
```

### 问题 4: Windows 没有 Linux 环境

推荐使用 WSL2:
```powershell
# PowerShell 管理员模式运行
wsl --install -d Ubuntu

# 重启电脑后,打开 "Ubuntu" 应用
# 然后执行 Linux 命令
```

---

## 📚 相关链接

- PaddleLite Releases: https://github.com/PaddlePaddle/Paddle-Lite/releases
- PaddleOCR 官方文档: https://github.com/PaddlePaddle/PaddleOCR
- opt 工具使用文档: https://paddle-lite.readthedocs.io/zh/latest/user_guides/model_optimize_tool.html

---

## ✅ 完成后检查

确保您有以下 3 个文件:
```
✓ ch_PP-OCRv4_det_infer.nb     (约 4-5 MB)
✓ ch_PP-OCRv4_rec_infer.nb     (约 10-11 MB)  
✓ ppocr_keys_v1.txt            (约 140 KB)
```

现在可以在 Android 项目中使用 OCR 功能了! 🎉
