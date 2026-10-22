$ErrorActionPreference = 'Stop'
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$frontendRoot = Join-Path $repositoryRoot 'frontend'
$screenshotRoot = Join-Path $repositoryRoot 'docs\qa\mobile-qa-2026-10-22-fixes'
New-Item -ItemType Directory -Path $screenshotRoot -Force | Out-Null

$jarPath = Join-Path $repositoryRoot 'target\talentbridge-0.0.1-SNAPSHOT.jar'
$javaHome = $env:JAVA_HOME
if ([string]::IsNullOrWhiteSpace($javaHome) -or -not (Test-Path $javaHome)) {
    if (Test-Path 'C:\Program Files\Java\jdk-21.0.10') {
        $javaHome = 'C:\Program Files\Java\jdk-21.0.10'
    } elseif (Test-Path 'C:\Program Files\Java\latest') {
        $javaHome = 'C:\Program Files\Java\latest'
    } else {
        $javaCmd = Get-Command java.exe -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($javaCmd) {
            $javaHome = Split-Path (Split-Path $javaCmd.Source -Parent) -Parent
        }
    }
}
$javaExecutable = Join-Path $javaHome 'bin\java.exe'
if (-not (Test-Path $javaExecutable)) {
    $javaExecutable = (Get-Command java.exe).Source
}

$oldJavaHome = $env:JAVA_HOME
$oldPath = $env:Path
$oldSpringProfile = $env:SPRING_PROFILES_ACTIVE
$oldApiBase = $env:TALENTBRIDGE_E2E_API_BASE_URL
$oldIsolated = $env:TALENTBRIDGE_E2E_DATA_ISOLATED
$oldScreenshotDirectory = $env:TALENTBRIDGE_E2E_SCREENSHOT_DIR

$backend = $null

try {
    $env:JAVA_HOME = $javaHome
    $env:Path = (Join-Path $javaHome 'bin') + ';' + $oldPath
    $env:SPRING_PROFILES_ACTIVE = 'e2e'
    $env:SPRING_DATASOURCE_URL = 'jdbc:h2:mem:talentbridge_e2e;DB_CLOSE_DELAY=-1;MODE=MySQL'
    $env:SPRING_DATASOURCE_DRIVER_CLASS_NAME = 'org.h2.Driver'
    $env:SPRING_DATASOURCE_USERNAME = 'sa'
    $env:SPRING_DATASOURCE_PASSWORD = ''

    Write-Host "Starting Backend Spring Boot H2 isolated..."
    $stdoutPath = Join-Path $screenshotRoot 'backend.stdout.log'
    $stderrPath = Join-Path $screenshotRoot 'backend.stderr.log'

    $backend = Start-Process -FilePath $javaExecutable `
        -ArgumentList @('-jar', "`"$jarPath`"", '--spring.profiles.active=e2e') `
        -WorkingDirectory $repositoryRoot `
        -RedirectStandardOutput $stdoutPath `
        -RedirectStandardError $stderrPath `
        -WindowStyle Hidden `
        -PassThru

    $ready = $false
    for ($attempt = 0; $attempt -lt 60; $attempt++) {
        if ($backend.HasExited) {
            throw "Backend exited early. Check logs at $screenshotRoot"
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
    if (-not $ready) { throw "Backend not ready after 60s." }

    Write-Host "Backend ready on port 18080. Running Playwright E2E mobile verification..."

    $env:TALENTBRIDGE_E2E_API_BASE_URL = 'http://127.0.0.1:18080/api/v1'
    $env:TALENTBRIDGE_E2E_DATA_ISOLATED = 'true'
    $env:TALENTBRIDGE_E2E_SCREENSHOT_DIR = $screenshotRoot
    if (-not $env:PLAYWRIGHT_CHANNEL) { $env:PLAYWRIGHT_CHANNEL = 'chrome' }

    Push-Location $frontendRoot
    try {
        npx playwright test e2e/mobile-qa-verification.spec.ts --config playwright.config.ts
    } finally {
        Pop-Location
    }
} finally {
    if ($backend -and -not $backend.HasExited) {
        Write-Host "Stopping Backend H2..."
        Stop-Process -Id $backend.Id -Force
        Wait-Process -Id $backend.Id -ErrorAction SilentlyContinue
    }
    $env:JAVA_HOME = $oldJavaHome
    $env:Path = $oldPath
    $env:SPRING_PROFILES_ACTIVE = $oldSpringProfile
    $env:TALENTBRIDGE_E2E_API_BASE_URL = $oldApiBase
    $env:TALENTBRIDGE_E2E_DATA_ISOLATED = $oldIsolated
    $env:TALENTBRIDGE_E2E_SCREENSHOT_DIR = $oldScreenshotDirectory
}

Write-Host "Mobile QA verification completed. Screenshots saved to $screenshotRoot"
