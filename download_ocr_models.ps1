# PaddleOCR 模型下载脚本 (Windows PowerShell)
# 使用方法: 在 PowerShell 中运行 .\download_ocr_models.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   PaddleOCR 模型文件下载工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 设置下载目录
$downloadDir = "ocr_models_download"
$projectDir = "app\src\main\assets\ocr"

# 创建下载目录
if (!(Test-Path $downloadDir)) {
    New-Item -ItemType Directory -Path $downloadDir | Out-Null
    Write-Host "✓ 创建下载目录: $downloadDir" -ForegroundColor Green
}

# 模型文件 URL
$detectionModelUrl = "https://paddleocr.bj.bcebos.com/PP-OCRv4/chinese/ch_PP-OCRv4_det_infer.tar"
$recognitionModelUrl = "https://paddleocr.bj.bcebos.com/PP-OCRv4/chinese/ch_PP-OCRv4_rec_infer.tar"
# 字典文件 URL (如果主链接失败会自动尝试镜像)
$dictionaryUrl = "https://raw.githubusercontent.com/PaddlePaddle/PaddleOCR/release/2.7/ppocr/utils/ppocr_keys_v1.txt"
$dictionaryUrlMirror = "https://ghproxy.com/https://raw.githubusercontent.com/PaddlePaddle/PaddleOCR/release/2.7/ppocr/utils/ppocr_keys_v1.txt"

# 下载文件函数
function Download-File {
    param (
        [string]$Url,
        [string]$OutputPath,
        [string]$Description
    )
    
    Write-Host ""
    Write-Host "正在下载: $Description" -ForegroundColor Yellow
    Write-Host "URL: $Url" -ForegroundColor Gray
    Write-Host "保存到: $OutputPath" -ForegroundColor Gray
    
    try {
        # 使用 WebClient 下载并显示进度
        $webClient = New-Object System.Net.WebClient
        
        # 注册进度事件
        Register-ObjectEvent -InputObject $webClient -EventName DownloadProgressChanged -SourceIdentifier WebClient.DownloadProgressChanged -Action {
            $percent = $EventArgs.ProgressPercentage
            Write-Progress -Activity "下载中..." -Status "$percent% 完成" -PercentComplete $percent
        } | Out-Null
        
        # 下载文件
        $webClient.DownloadFile($Url, $OutputPath)
        
        # 取消注册事件
        Unregister-Event -SourceIdentifier WebClient.DownloadProgressChanged -ErrorAction SilentlyContinue
        Remove-Job -Name WebClient.DownloadProgressChanged -ErrorAction SilentlyContinue
        
        Write-Host "✓ 下载完成: $Description" -ForegroundColor Green
        
        # 显示文件大小
        $fileSize = (Get-Item $OutputPath).Length / 1MB
        Write-Host "  文件大小: $([math]::Round($fileSize, 2)) MB" -ForegroundColor Gray
        
        return $true
    }
    catch {
        Write-Host "✗ 下载失败: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

Write-Host ""
Write-Host "开始下载 3 个必需文件..." -ForegroundColor Cyan
Write-Host ""

# 1. 下载文字检测模型
$detFile = Join-Path $downloadDir "ch_PP-OCRv4_det_infer.tar"
$success1 = Download-File -Url $detectionModelUrl -OutputPath $detFile -Description "文字检测模型 (Detection Model)"

# 2. 下载文字识别模型
$recFile = Join-Path $downloadDir "ch_PP-OCRv4_rec_infer.tar"
$success2 = Download-File -Url $recognitionModelUrl -OutputPath $recFile -Description "文字识别模型 (Recognition Model)"

# 3. 下载字符字典
$dictFile = Join-Path $downloadDir "ppocr_keys_v1.txt"
Write-Host ""
Write-Host "正在下载: 字符字典 (Dictionary)" -ForegroundColor Yellow
Write-Host "尝试主链接..." -ForegroundColor Gray
$success3 = Download-File -Url $dictionaryUrl -OutputPath $dictFile -Description "字符字典 (Dictionary)"

# 如果主链接失败,尝试镜像链接
if (-not $success3) {
    Write-Host "主链接失败,尝试使用镜像链接..." -ForegroundColor Yellow
    $success3 = Download-File -Url $dictionaryUrlMirror -OutputPath $dictFile -Description "字符字典 (Dictionary - 镜像)"
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   下载完成总结" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($success1 -and $success2 -and $success3) {
    Write-Host "✓ 所有文件下载成功!" -ForegroundColor Green
    Write-Host ""
    Write-Host "下载的文件位于: $downloadDir" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "⚠️  重要提示:" -ForegroundColor Red
    Write-Host "1. 下载的 .tar 文件需要先解压" -ForegroundColor Yellow
    Write-Host "2. 然后使用 PaddleLite opt 工具转换为 .nb 格式" -ForegroundColor Yellow
    Write-Host "3. 详细转换步骤请参考 'OCR功能使用指南.md'" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "如果您已经有转换好的 .nb 文件,请将以下文件复制到项目:" -ForegroundColor Cyan
    Write-Host "  目标目录: $projectDir" -ForegroundColor Gray
    Write-Host "  • ch_PP-OCRv4_det_infer.nb" -ForegroundColor Gray
    Write-Host "  • ch_PP-OCRv4_rec_infer.nb" -ForegroundColor Gray
    Write-Host "  • ppocr_keys_v1.txt" -ForegroundColor Gray
    Write-Host ""
    
    # 询问是否打开下载目录
    $openFolder = Read-Host "是否打开下载目录? (Y/N)"
    if ($openFolder -eq "Y" -or $openFolder -eq "y") {
        Invoke-Item $downloadDir
    }
}
else {
    Write-Host "✗ 部分文件下载失败,请检查网络连接后重试" -ForegroundColor Red
    Write-Host ""
    Write-Host "如果下载速度太慢,可以尝试:" -ForegroundColor Yellow
    Write-Host "1. 使用浏览器直接下载 (复制 URL 到浏览器)" -ForegroundColor Gray
    Write-Host "2. 使用迅雷等下载工具" -ForegroundColor Gray
    Write-Host "3. 搜索国内网盘分享链接" -ForegroundColor Gray
}

Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
