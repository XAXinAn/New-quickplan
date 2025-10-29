#!/bin/bash
# PaddleOCR 模型下载脚本 (Linux/Mac)
# 使用方法: chmod +x download_ocr_models.sh && ./download_ocr_models.sh

echo "========================================"
echo "   PaddleOCR 模型文件下载工具"
echo "========================================"
echo ""

# 设置下载目录
DOWNLOAD_DIR="ocr_models_download"
PROJECT_DIR="app/src/main/assets/ocr"

# 创建下载目录
mkdir -p "$DOWNLOAD_DIR"
echo "✓ 创建下载目录: $DOWNLOAD_DIR"

# 模型文件 URL
DETECTION_URL="https://paddleocr.bj.bcebos.com/PP-OCRv4/chinese/ch_PP-OCRv4_det_infer.tar"
RECOGNITION_URL="https://paddleocr.bj.bcebos.com/PP-OCRv4/chinese/ch_PP-OCRv4_rec_infer.tar"
DICTIONARY_URL="https://raw.githubusercontent.com/PaddlePaddle/PaddleOCR/release/2.7/ppocr/utils/ppocr_keys_v1.txt"

# 下载函数
download_file() {
    local url=$1
    local output=$2
    local description=$3
    
    echo ""
    echo "正在下载: $description"
    echo "URL: $url"
    echo "保存到: $output"
    
    if command -v wget &> /dev/null; then
        wget -O "$output" "$url" --show-progress
    elif command -v curl &> /dev/null; then
        curl -L -o "$output" "$url" --progress-bar
    else
        echo "错误: 未找到 wget 或 curl 命令"
        return 1
    fi
    
    if [ $? -eq 0 ]; then
        echo "✓ 下载完成: $description"
        file_size=$(du -h "$output" | cut -f1)
        echo "  文件大小: $file_size"
        return 0
    else
        echo "✗ 下载失败: $description"
        return 1
    fi
}

echo ""
echo "开始下载 3 个必需文件..."
echo ""

# 1. 下载文字检测模型
download_file "$DETECTION_URL" "$DOWNLOAD_DIR/ch_PP-OCRv4_det_infer.tar" "文字检测模型 (Detection Model)"
SUCCESS1=$?

# 2. 下载文字识别模型
download_file "$RECOGNITION_URL" "$DOWNLOAD_DIR/ch_PP-OCRv4_rec_infer.tar" "文字识别模型 (Recognition Model)"
SUCCESS2=$?

# 3. 下载字符字典
download_file "$DICTIONARY_URL" "$DOWNLOAD_DIR/ppocr_keys_v1.txt" "字符字典 (Dictionary)"
SUCCESS3=$?

echo ""
echo "========================================"
echo "   下载完成总结"
echo "========================================"
echo ""

if [ $SUCCESS1 -eq 0 ] && [ $SUCCESS2 -eq 0 ] && [ $SUCCESS3 -eq 0 ]; then
    echo "✓ 所有文件下载成功!"
    echo ""
    echo "下载的文件位于: $DOWNLOAD_DIR"
    echo ""
    echo "⚠️  重要提示:"
    echo "1. 下载的 .tar 文件需要先解压"
    echo "2. 然后使用 PaddleLite opt 工具转换为 .nb 格式"
    echo "3. 详细转换步骤请参考 'OCR功能使用指南.md'"
    echo ""
    echo "解压命令:"
    echo "  tar -xvf $DOWNLOAD_DIR/ch_PP-OCRv4_det_infer.tar -C $DOWNLOAD_DIR"
    echo "  tar -xvf $DOWNLOAD_DIR/ch_PP-OCRv4_rec_infer.tar -C $DOWNLOAD_DIR"
    echo ""
    echo "如果您已经有转换好的 .nb 文件,请将以下文件复制到项目:"
    echo "  目标目录: $PROJECT_DIR"
    echo "  • ch_PP-OCRv4_det_infer.nb"
    echo "  • ch_PP-OCRv4_rec_infer.nb"
    echo "  • ppocr_keys_v1.txt"
    echo ""
else
    echo "✗ 部分文件下载失败,请检查网络连接后重试"
    echo ""
    echo "如果下载速度太慢,可以尝试:"
    echo "1. 使用浏览器直接下载 (复制 URL 到浏览器)"
    echo "2. 搜索国内网盘分享链接"
fi

echo ""
