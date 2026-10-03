$ErrorActionPreference = 'Stop'
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$frontendRoot = Join-Path $repositoryRoot 'frontend'
$logRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("talentbridge-e2e-" + [guid]::NewGuid().ToString('N'))
$screenshotRoot = Join-Path $repositoryRoot 'artifacts\roleplay-qa'
New-Item -ItemType Directory -Path $logRoot, $screenshotRoot -Force | Out-Null
$stdoutPath = Join-Path $logRoot 'backend.stdout.log'
$stderrPath = Join-Path $logRoot 'backend.stderr.log'
$mavenWrapper = Join-Path $repositoryRoot 'mvnw.cmd'
$jarPath = Join-Path $repositoryRoot 'target\talentbridge-0.0.1-SNAPSHOT.jar'
$javaHome = 'C:\Program Files\Java\jdk-21'
$javaExecutable = Join-Path $javaHome 'bin\java.exe'
$oldJavaHome = $env:JAVA_HOME
$oldPath = $env:Path
$oldSpringProfile = $env:SPRING_PROFILES_ACTIVE
$oldApiBase = $env:TALENTBRIDGE_E2E_API_BASE_URL
$oldIsolated = $env:TALENTBRIDGE_E2E_DATA_ISOLATED
$oldScreenshotDirectory = $env:TALENTBRIDGE_E2E_SCREENSHOT_DIR
$oldDatasourceUrl = $env:SPRING_DATASOURCE_URL
$oldDatasourceDriver = $env:SPRING_DATASOURCE_DRIVER_CLASS_NAME
$oldDatasourceUsername = $env:SPRING_DATASOURCE_USERNAME
$oldDatasourcePassword = $env:SPRING_DATASOURCE_PASSWORD
$backend = $null

try {
    if (-not (Test-Path $javaExecutable)) {
        throw "JDK 21 chưa được tìm thấy tại $javaHome"
    }
    if (Get-NetTCPConnection -LocalPort 18080, 5174 -State Listen -ErrorAction SilentlyContinue) {
        throw 'Cổng 18080 hoặc 5174 đang có dịch vụ lắng nghe. Dừng để không dùng nhầm backend/UI khác.'
    }
    $env:JAVA_HOME = $javaHome
    $env:Path = (Join-Path $javaHome 'bin') + ';' + $oldPath
    $env:SPRING_PROFILES_ACTIVE = 'e2e'
    $env:SPRING_DATASOURCE_URL = 'jdbc:h2:mem:talentbridge_e2e;DB_CLOSE_DELAY=-1;MODE=MySQL'
    $env:SPRING_DATASOURCE_DRIVER_CLASS_NAME = 'org.h2.Driver'
    $env:SPRING_DATASOURCE_USERNAME = 'sa'
    $env:SPRING_DATASOURCE_PASSWORD = ''

    Push-Location $repositoryRoot
    try {
        & $mavenWrapper '-DskipTests' 'package'
        if ($LASTEXITCODE -ne 0) { throw "Maven package thất bại với exit code $LASTEXITCODE" }
    } finally {
        Pop-Location
    }
    if (-not (Test-Path $jarPath)) { throw "Không tìm thấy artifact backend: $jarPath" }

    $backend = Start-Process -FilePath $javaExecutable `
        -ArgumentList @('-jar', "`"$jarPath`"", '--spring.profiles.active=e2e') `
        -WorkingDirectory $repositoryRoot `
        -RedirectStandardOutput $stdoutPath `
        -RedirectStandardError $stderrPath `
        -WindowStyle Hidden `
        -PassThru

    $ready = $false
    for ($attempt = 0; $attempt -lt 120; $attempt++) {
        if ($backend.HasExited) {
            throw "Backend H2 đã dừng khi khởi động. Xem log tại $logRoot"
        }
        try {
            $health = Invoke-WebRequest -Uri 'http://127.0.0.1:18080/api/v1/skills' -TimeoutSec 2 -UseBasicParsing
            $ready = $health.StatusCode -eq 200
        } catch {
            $ready = $false
        }
        if ($ready) { break }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) { throw "Backend H2 không sẵn sàng sau 120 giây. Xem log tại $logRoot" }

    $env:TALENTBRIDGE_E2E_API_BASE_URL = 'http://127.0.0.1:18080/api/v1'
    $env:TALENTBRIDGE_E2E_DATA_ISOLATED = 'true'
    $env:TALENTBRIDGE_E2E_SCREENSHOT_DIR = $screenshotRoot

    Push-Location $frontendRoot
    try {
        npm run test:e2e
        if ($LASTEXITCODE -ne 0) { throw "API E2E thất bại với exit code $LASTEXITCODE" }
        npm run test:e2e:ui
        if ($LASTEXITCODE -ne 0) { throw "UI E2E thất bại với exit code $LASTEXITCODE" }
    } finally {
        Pop-Location
    }
} finally {
    if ($backend -and -not $backend.HasExited) {
        Stop-Process -Id $backend.Id -Force
        Wait-Process -Id $backend.Id -ErrorAction SilentlyContinue
    }
    $env:JAVA_HOME = $oldJavaHome
    $env:Path = $oldPath
    $env:SPRING_PROFILES_ACTIVE = $oldSpringProfile
    $env:TALENTBRIDGE_E2E_API_BASE_URL = $oldApiBase
    $env:TALENTBRIDGE_E2E_DATA_ISOLATED = $oldIsolated
    $env:TALENTBRIDGE_E2E_SCREENSHOT_DIR = $oldScreenshotDirectory
    $env:SPRING_DATASOURCE_URL = $oldDatasourceUrl
    $env:SPRING_DATASOURCE_DRIVER_CLASS_NAME = $oldDatasourceDriver
    $env:SPRING_DATASOURCE_USERNAME = $oldDatasourceUsername
    $env:SPRING_DATASOURCE_PASSWORD = $oldDatasourcePassword
}

Write-Host "E2E hoàn tất trên H2 tạm thời. Ảnh QA: $screenshotRoot. Log backend: $logRoot"
