# test-server.ps1
# Automates the creation of a Paper test server using PaperMC v3 API (via fill.papermc.io)

$PROJECT = "paper"
$MINECRAFT_VERSION = "1.21.11" 
$USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
$SERVER_FOLDER = "test-server"

# 1. Reset Behavior: Delete existing folder and start fresh
if (Test-Path $SERVER_FOLDER) {
    Write-Host "Removing existing test server folder..." -ForegroundColor Yellow
    Remove-Item -Path $SERVER_FOLDER -Recurse -Force
}

New-Item -ItemType Directory -Path $SERVER_FOLDER
Write-Host "Created fresh folder: $SERVER_FOLDER" -ForegroundColor Green

# 2. Get Project Version from Gradle
Write-Host "Fetching project version..." -ForegroundColor Cyan
$gradleProps = ./gradlew properties -q | Out-String
$versionLine = $gradleProps -split "`r?`n" | Where-Object { $_ -match "^version:" }
if (-not $versionLine) {
    Write-Error "Could not determine project version from Gradle."
    exit 1
}
$PROJECT_VERSION = ($versionLine -replace "version: ", "").Trim()
Write-Host "Detected Version: $PROJECT_VERSION" -ForegroundColor Green

# 3. PaperMC v3 API Download Logic
Write-Host "Searching for stable Paper build for $MINECRAFT_VERSION..." -ForegroundColor Cyan

function Get-PaperDownloadUrl($version) {
    $url = "https://fill.papermc.io/v3/projects/$PROJECT/versions/$version/builds"
    try {
        $json = & curl.exe -L -s -H "User-Agent: $USER_AGENT" -H "Accept: application/json" $url | Out-String
        if ($json -match "Attention Required! | Cloudflare" -or -not $json.Trim()) {
             return $null
        }
        $response = $json | ConvertFrom-Json
        if ($response.ok -eq $false) { return $null }
        foreach ($build in $response) {
            if ($build.channel -eq "STABLE" -and $build.downloads."server:default") {
                return $build.downloads."server:default".url
            }
        }
    } catch {
        return $null
    }
    return $null
}

$paperUrl = Get-PaperDownloadUrl $MINECRAFT_VERSION
$foundVersion = $MINECRAFT_VERSION

if ($null -eq $paperUrl) {
    Write-Host "No stable build for version $MINECRAFT_VERSION, searching for latest stable version..." -ForegroundColor Yellow
    $projectUrl = "https://fill.papermc.io/v3/projects/$PROJECT"
    try {
        $json = & curl.exe -L -s -H "User-Agent: $USER_AGENT" -H "Accept: application/json" $projectUrl | Out-String
        $projectInfo = $json | ConvertFrom-Json
        $allVersions = @()
        foreach ($group in $projectInfo.versions.PSObject.Properties) { $allVersions += $group.Value }
        $versions = $allVersions | Sort-Object { [version]($_ -replace "^(\d+\.\d+)(\..*)?$", '$1$2') } -Descending
        foreach ($v in $versions) {
            $paperUrl = Get-PaperDownloadUrl $v
            if ($null -ne $paperUrl) {
                $foundVersion = $v
                Write-Host "Found stable build for version $v" -ForegroundColor Green
                break
            }
        }
    } catch {
        Write-Error "Failed to reach PaperMC API."
        exit 1
    }
}

if ($null -ne $paperUrl) {
    Write-Host "Downloading Paper version $foundVersion..." -ForegroundColor Cyan
    & curl.exe -L -H "User-Agent: $USER_AGENT" -o "$SERVER_FOLDER/paper.jar" $paperUrl
    if (Test-Path "$SERVER_FOLDER/paper.jar") {
        Write-Host "Download completed." -ForegroundColor Green
    } else {
        Write-Error "Download failed."
        exit 1
    }
} else {
    Write-Error "No stable builds available for any version."
    exit 1
}

# 4. Build and Copy Plugin (Automatically)
Write-Host "Building Portel v$PROJECT_VERSION..." -ForegroundColor Cyan
./gradlew shadowJar
if ($LASTEXITCODE -ne 0) {
    Write-Error "Gradle build failed."
    exit 1
}

$expectedJar = "build/libs/Portel-$PROJECT_VERSION.jar"
if (Test-Path $expectedJar) {
    $pluginsPath = "$SERVER_FOLDER/plugins"
    if (-not (Test-Path $pluginsPath)) { New-Item -ItemType Directory -Path $pluginsPath }
    Copy-Item $expectedJar "$pluginsPath/Portel.jar" -Force
    Write-Host "Plugin v$PROJECT_VERSION copied to test server." -ForegroundColor Green
} else {
    Write-Error "Could not find built JAR at $expectedJar"
    exit 1
}

# 5. Create helper scripts
# start-server.ps1 (Internal)
$START_SCRIPT_CONTENT = @"
# start-server.ps1
Set-Location -Path `$PSScriptRoot
java -Xmx2G -Xms2G -jar paper.jar nogui
"@
Set-Content -Path "$SERVER_FOLDER/start-server.ps1" -Value $START_SCRIPT_CONTENT

# start-server.ps1 (Root)
$ROOT_START_SCRIPT = @"
# start-server.ps1
Set-Location -Path `$PSScriptRoot/$SERVER_FOLDER
./start-server.ps1
"@
Set-Content -Path "start-server.ps1" -Value $ROOT_START_SCRIPT

# copy-plugin.ps1 (Root)
$COPY_PLUGIN_CONTENT = @"
# copy-plugin.ps1
Write-Host "Building plugin..." -ForegroundColor Cyan
./gradlew shadowJar
`$expectedJar = "build/libs/Portel-$PROJECT_VERSION.jar"
if (Test-Path `$expectedJar) {
    if (-not (Test-Path "test-server/plugins")) { New-Item -ItemType Directory -Path "test-server/plugins" }
    Copy-Item `$expectedJar "test-server/plugins/Portel.jar" -Force
    Write-Host "Plugin v$PROJECT_VERSION copied to test server." -ForegroundColor Green
} else {
    Write-Error "JAR not found."
}
"@
Set-Content -Path "copy-plugin.ps1" -Value $COPY_PLUGIN_CONTENT

# 6. Final Setup (EULA)
Set-Content -Path "$SERVER_FOLDER/eula.txt" -Value "eula=true"

Write-Host "`nTest server setup complete!" -ForegroundColor Green
Write-Host "Version: $foundVersion (Paper) / $PROJECT_VERSION (Portel)" -ForegroundColor Cyan
Write-Host "To launch the server, run: ./start-server.ps1" -ForegroundColor Cyan
