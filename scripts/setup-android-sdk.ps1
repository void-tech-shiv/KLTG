# Configure the Android SDK for a local Windows checkout of GLACIA.
# Run from the repository root: .\scripts\setup-android-sdk.ps1
# Pass -SdkPath "D:\Android\Sdk" if Android Studio uses a custom directory.
param(
    [string]$SdkPath = ""
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$localProperties = Join-Path $root "local.properties"

function Test-AndroidSdk([string]$Path) {
    if ([string]::IsNullOrWhiteSpace($Path)) { return $false }
    $p = $Path.Trim().Trim('"')
    if (-not (Test-Path -LiteralPath $p -PathType Container)) { return $false }
    foreach ($subfolder in @("platforms", "platform-tools", "build-tools", "cmdline-tools")) {
        if (Test-Path -LiteralPath (Join-Path $p $subfolder) -PathType Container) {
            return $true
        }
    }
    return $false
}

$candidates = New-Object System.Collections.Generic.List[string]
if (-not [string]::IsNullOrWhiteSpace($SdkPath)) {
    $candidates.Add($SdkPath)
} else {
    if (Test-Path -LiteralPath $localProperties -PathType Leaf) {
        foreach ($line in [System.IO.File]::ReadAllLines($localProperties)) {
            if ($line -match '^\s*sdk\.dir\s*[=:]\s*(.+?)\s*$') {
                # Java properties may use either forward slashes or escaped backslashes.
                $existing = $Matches[1].Trim().Replace('\\', '\').Replace('/', '\')
                $candidates.Add($existing)
                break
            }
        }
    }
    foreach ($v in @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT)) {
        if (-not [string]::IsNullOrWhiteSpace($v)) { $candidates.Add($v) }
    }
    if (-not [string]::IsNullOrWhiteSpace($env:LOCALAPPDATA)) {
        $candidates.Add((Join-Path $env:LOCALAPPDATA "Android\Sdk"))
    }
    if (-not [string]::IsNullOrWhiteSpace($env:USERPROFILE)) {
        $candidates.Add((Join-Path $env:USERPROFILE "AppData\Local\Android\Sdk"))
    }
}

$sdk = $null
foreach ($candidate in $candidates) {
    if (Test-AndroidSdk $candidate) {
        $sdk = (Resolve-Path -LiteralPath $candidate).Path.TrimEnd('\', '/')
        break
    }
}

if (-not $sdk) {
    Write-Error @"
Android SDK folder not found.
Open Android Studio > Tools > SDK Manager and copy the 'Android SDK Location'.
Then run: .\scripts\setup-android-sdk.ps1 -SdkPath "PASTE_REAL_SDK_LOCATION"
SDK installation is separate from the Android Studio JDK (jbr) installation.
"@
    exit 1
}

# Prefer forward slashes for sdk.dir, so whitespace and Windows backslashes
# do not require property-file escaping. Do not commit local.properties.
$property = "sdk.dir=" + $sdk.Replace('\', '/')
$lines = @()
if (Test-Path -LiteralPath $localProperties -PathType Leaf) {
    $lines = @([System.IO.File]::ReadAllLines($localProperties) |
        Where-Object { $_ -notmatch '^\s*sdk\.dir\s*[=:]' })
}
$lines += $property
[System.IO.File]::WriteAllText(
    $localProperties,
    (($lines -join [Environment]::NewLine) + [Environment]::NewLine),
    (New-Object System.Text.UTF8Encoding($false))
)
$env:ANDROID_HOME = $sdk

Write-Host "Android SDK configured: $sdk" -ForegroundColor Green
Write-Host "Created/updated: $localProperties (gitignored)"
if (-not (Test-Path -LiteralPath (Join-Path $sdk "platforms\android-36"))) {
    Write-Warning "Android SDK Platform 36 is missing. Install it in SDK Manager."
}
if (-not (Test-Path -LiteralPath (Join-Path $sdk "ndk\27.2.12479018"))) {
    Write-Warning "Android NDK 27.2.12479018 is missing. Install it in SDK Manager."
}
if (-not (Test-Path -LiteralPath (Join-Path $sdk "cmake\3.22.1"))) {
    Write-Warning "CMake 3.22.1 is missing. Install it in SDK Manager."
}
Write-Host "Next: .\gradlew.bat projects --stacktrace"
