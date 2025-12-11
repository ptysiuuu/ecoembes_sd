# Verification Script - Check if all projects are properly set up

Write-Host "=" * 70 -ForegroundColor Cyan
Write-Host "ECOEMBES PROJECT STRUCTURE VERIFICATION" -ForegroundColor Cyan
Write-Host "=" * 70 -ForegroundColor Cyan
Write-Host ""

$basePath = $PSScriptRoot
$projects = @("ecoembes-server", "plassb-server", "contsocket-server", "webclient")
$allGood = $true

foreach ($project in $projects) {
    Write-Host "Checking: $project" -ForegroundColor Yellow
    $projectPath = Join-Path $basePath $project

    # Check if project folder exists
    if (-not (Test-Path $projectPath)) {
        Write-Host "  ❌ Project folder not found!" -ForegroundColor Red
        $allGood = $false
        continue
    }

    # Check for essential files
    $buildGradle = Join-Path $projectPath "build.gradle"
    $settingsGradle = Join-Path $projectPath "settings.gradle"
    $gradlewBat = Join-Path $projectPath "gradlew.bat"
    $srcFolder = Join-Path $projectPath "src\main\java"

    if (Test-Path $buildGradle) {
        Write-Host "  ✅ build.gradle found" -ForegroundColor Green
    } else {
        Write-Host "  ❌ build.gradle missing!" -ForegroundColor Red
        $allGood = $false
    }

    if (Test-Path $settingsGradle) {
        Write-Host "  ✅ settings.gradle found" -ForegroundColor Green
    } else {
        Write-Host "  ❌ settings.gradle missing!" -ForegroundColor Red
        $allGood = $false
    }

    if (Test-Path $gradlewBat) {
        Write-Host "  ✅ gradlew.bat found" -ForegroundColor Green
    } else {
        Write-Host "  ❌ gradlew.bat missing!" -ForegroundColor Red
        $allGood = $false
    }

    if (Test-Path $srcFolder) {
        $javaFiles = (Get-ChildItem $srcFolder -Recurse -Filter "*.java" -File -ErrorAction SilentlyContinue).Count
        Write-Host "  ✅ Source folder found ($javaFiles Java files)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Source folder missing!" -ForegroundColor Red
        $allGood = $false
    }

    Write-Host ""
}

Write-Host "=" * 70 -ForegroundColor Cyan

if ($allGood) {
    Write-Host "✅ ALL PROJECTS ARE PROPERLY CONFIGURED!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Yellow
    Write-Host "1. Open IntelliJ IDEA" -ForegroundColor White
    Write-Host "2. File → Open → Select 'Ecoembes-Separated' folder" -ForegroundColor White
    Write-Host "3. Wait for Gradle sync" -ForegroundColor White
    Write-Host "4. Create Compound Run Configuration (see INTELLIJ_SETUP.md)" -ForegroundColor White
    Write-Host "5. Run all services with one click!" -ForegroundColor White
} else {
    Write-Host "❌ SOME ISSUES FOUND - Please check the errors above" -ForegroundColor Red
}

Write-Host ""
Write-Host "For detailed instructions, see:" -ForegroundColor Cyan
Write-Host "  - START_HERE.md" -ForegroundColor White
Write-Host "  - INTELLIJ_SETUP.md" -ForegroundColor White
Write-Host "  - README.md" -ForegroundColor White
Write-Host ""
Write-Host "Press any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

