# DeutschCraft Debug Launcher
# Run this in PowerShell to launch with console output and logging

$ErrorActionPreference = "Continue"
$host.ui.RawUI.WindowTitle = "DeutschCraft Debug Launcher"

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = "$env:TEMP\deutschcraft_debug_$timestamp.log"
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "   DeutschCraft Debug Launcher" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Log file: $logFile"
Write-Host "Working dir: $projectDir"
Write-Host ""
Write-Host "Close this window to stop" -ForegroundColor Yellow
Write-Host ""
Write-Host "-----------------------------------------" -ForegroundColor Gray
Write-Host "Starting build + run..." -ForegroundColor Green
Write-Host "-----------------------------------------" -ForegroundColor Gray
Write-Host ""

Set-Location $projectDir

# Run gradle with output to both console and log file
.\gradlew.bat :desktopApp:run --info 2>&1 | Tee-Object -FilePath $logFile

$exitCode = $LASTEXITCODE

if ($exitCode -ne 0) {
    Write-Host ""
    Write-Host "=========================================" -ForegroundColor Red
    Write-Host "   BUILD/Run FAILED (exit code: $exitCode)" -ForegroundColor Red
    Write-Host "=========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Showing last 50 lines of log:" -ForegroundColor Yellow
    Write-Host "-----------------------------------------" -ForegroundColor Gray
    Get-Content $logFile -Tail 50
    Write-Host ""
    Write-Host "Full log: $logFile"
    Write-Host ""
    Read-Host "Press Enter to close"
} else {
    Write-Host ""
    Write-Host "App finished. Log saved to: $logFile"
    Read-Host "Press Enter to close"
}
