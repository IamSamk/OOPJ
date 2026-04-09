param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path,
    [string]$JavaHome = "$env:USERPROFILE\.jdk\jdk-25",
    [string]$MavenCmd = "$env:USERPROFILE\.maven\maven-3.9.14\bin\mvn.cmd",
    [switch]$SkipCommonInstall,
    [switch]$RestartPorts
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $ProjectRoot)) {
    throw "Project root not found: $ProjectRoot"
}

$resolvedMaven = $MavenCmd
$mavenBin = ""
if (Test-Path $MavenCmd) {
    $resolvedMaven = (Resolve-Path $MavenCmd).Path
    $mavenBin = Split-Path -Parent $resolvedMaven
} else {
    $resolvedMaven = "mvn"
}

$services = @(
    @{ Name = "user-service"; Port = 8081 },
    @{ Name = "event-service"; Port = 8082 },
    @{ Name = "booking-service"; Port = 8083 },
    @{ Name = "pricing-service"; Port = 8084 },
    @{ Name = "payment-service"; Port = 8085 },
    @{ Name = "notification-service"; Port = 8086 }
)

Write-Host "Project Root : $ProjectRoot" -ForegroundColor Cyan
Write-Host "JAVA_HOME    : $JavaHome" -ForegroundColor Cyan
Write-Host "Maven        : $resolvedMaven" -ForegroundColor Cyan

if (-not $SkipCommonInstall) {
    Write-Host "Installing common module first..." -ForegroundColor Yellow
    Set-Location $ProjectRoot
    & $resolvedMaven -f "common/pom.xml" install -DskipTests
}

foreach ($service in $services) {
    $serviceName = $service.Name
    $servicePort = $service.Port

    $listeners = Get-NetTCPConnection -LocalPort $servicePort -State Listen -ErrorAction SilentlyContinue
    if ($listeners) {
        $owningProcesses = $listeners | Select-Object -ExpandProperty OwningProcess -Unique
        if ($RestartPorts) {
            foreach ($processId in $owningProcesses) {
                try {
                    Stop-Process -Id $processId -Force -ErrorAction Stop
                    Write-Host "Stopped existing process $processId on port $servicePort." -ForegroundColor Yellow
                } catch {
                    Write-Host "Could not stop process $processId on port ${servicePort}: $($_.Exception.Message)" -ForegroundColor Red
                }
            }
        } else {
            Write-Host "Skipping $serviceName because port $servicePort is already in use by PID(s): $($owningProcesses -join ', ')." -ForegroundColor Yellow
            continue
        }
    }

    $bootstrapCommand = @"
`$env:JAVA_HOME = '$JavaHome'
if ('$mavenBin' -ne '') {
    `$env:PATH = '$mavenBin;' + `$env:JAVA_HOME + '\bin;' + `$env:PATH
} else {
    `$env:PATH = `$env:JAVA_HOME + '\bin;' + `$env:PATH
}
Set-Location '$ProjectRoot'
Write-Host 'Starting $serviceName on port $servicePort (dev profile)...' -ForegroundColor Green
& '$resolvedMaven' -f '$serviceName/pom.xml' spring-boot:run '-Dspring-boot.run.profiles=dev'
"@

    Start-Process -FilePath "powershell.exe" -ArgumentList @(
        "-NoExit",
        "-ExecutionPolicy", "Bypass",
        "-Command", $bootstrapCommand
    ) | Out-Null
}

Write-Host "Opened terminals for all services." -ForegroundColor Green
Write-Host "If some services fail due to downstream calls, start dependencies first: event-service, pricing-service, payment-service, booking-service." -ForegroundColor DarkYellow
