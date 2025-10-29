@echo off
chcp 65001
echo ====================================
echo    OCR 模型自动转换脚本
echo ====================================
echo.

echo [1/5] 检查 PaddleLite 是否已安装...
python -c "import paddle_lite" 2>nul
if %errorlevel% neq 0 (
    echo PaddleLite 未安装，正在安装...
    pip install paddlelite==2.10 -i https://pypi.tuna.tsinghua.edu.cn/simple
) else (
    echo ✓ PaddleLite 已安装
)

echo.
echo [2/5] 检查模型文件是否存在...
cd C:\Users\18241\Desktop
if not exist "ch_PP-OCRv4_det_infer\inference.pdmodel" (
    echo ✗ 检测模型文件不存在！
    pause
    exit /b 1
)
if not exist "ch_PP-OCRv4_rec_infer\inference.pdmodel" (
    echo ✗ 识别模型文件不存在！
    pause
    exit /b 1
)
echo ✓ 模型文件存在

echo.
echo [3/5] 转换检测模型...
paddle_lite_opt ^
  --model_file=.\ch_PP-OCRv4_det_infer\inference.pdmodel ^
  --param_file=.\ch_PP-OCRv4_det_infer\inference.pdiparams ^
  --optimize_out=.\ch_PP-OCRv4_det_infer ^
  --valid_targets=arm ^
  --optimize_out_type=naive_buffer

if %errorlevel% equ 0 (
    echo ✓ 检测模型转换成功
) else (
    echo ✗ 检测模型转换失败
    pause
    exit /b 1
)

echo.
echo [4/5] 转换识别模型...
paddle_lite_opt ^
  --model_file=.\ch_PP-OCRv4_rec_infer\inference.pdmodel ^
  --param_file=.\ch_PP-OCRv4_rec_infer\inference.pdiparams ^
  --optimize_out=.\ch_PP-OCRv4_rec_infer ^
  --valid_targets=arm ^
  --optimize_out_type=naive_buffer

if %errorlevel% equ 0 (
    echo ✓ 识别模型转换成功
) else (
    echo ✗ 识别模型转换失败
    pause
    exit /b 1
)

echo.
echo [5/5] 下载字典文件...
powershell -Command "Invoke-WebRequest -Uri 'https://gitee.com/paddlepaddle/PaddleOCR/raw/release/2.7/ppocr/utils/ppocr_keys_v1.txt' -OutFile 'ppocr_keys_v1.txt'"
if %errorlevel% equ 0 (
    echo ✓ 字典文件下载成功
) else (
    echo ✗ 字典文件下载失败
)

echo.
echo ====================================
echo    转换完成！
echo ====================================
echo.
echo 生成的文件:
dir /b *.nb 2>nul
echo ppocr_keys_v1.txt
echo.
echo 请将以下3个文件复制到项目中:
echo   app/src/main/assets/ocr/
echo     ├── ch_PP-OCRv4_det_infer.nb
echo     ├── ch_PP-OCRv4_rec_infer.nb
echo     └── ppocr_keys_v1.txt
echo.
pause
