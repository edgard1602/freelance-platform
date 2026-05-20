# ================================================================
#  install-and-run.ps1
#  Script d'installation et démarrage — Freelance Platform
#  Usage : clic droit -> "Exécuter avec PowerShell"
# ================================================================

$ErrorActionPreference = "Stop"

function Write-Title($text) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  $text" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
}

function Write-OK($text) {
    Write-Host "  [OK] $text" -ForegroundColor Green
}

function Write-ERR($text) {
    Write-Host "  [ERREUR] $text" -ForegroundColor Red
}

function Write-INFO($text) {
    Write-Host "  [INFO] $text" -ForegroundColor Yellow
}

# ── 1. Vérifier Java 21 ───────────────────────────────────────────
Write-Title "Vérification Java 21"

$javaPath = $null
$possiblePaths = @(
    "C:\Program Files\Eclipse Adoptium\jdk-21*",
    "C:\Program Files\Java\jdk-21*",
    "$env:LOCALAPPDATA\Programs\Eclipse Adoptium\jdk-21*"
)

foreach ($path in $possiblePaths) {
    $found = Get-Item $path -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) {
        $javaPath = $found.FullName
        break
    }
}

if ($javaPath) {
    $env:JAVA_HOME = $javaPath
    $env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
    Write-OK "Java 21 trouvé : $javaPath"
} else {
    Write-ERR "Java 21 introuvable !"
    Write-INFO "Télécharger sur : https://adoptium.net/temurin/releases/?version=21"
    Write-INFO "Choisir : Windows x64 JDK .msi"
    Write-INFO "Cocher 'Set JAVA_HOME' et 'Add to PATH' pendant l'installation"
    Read-Host "Appuyer sur Entrée après l'installation de Java 21"
    exit 1
}

# ── 2. Vérifier Docker ────────────────────────────────────────────
Write-Title "Vérification Docker"

try {
    $dockerVersion = docker -v 2>&1
    Write-OK "Docker trouvé : $dockerVersion"
} catch {
    Write-ERR "Docker introuvable !"
    Write-INFO "Télécharger sur : https://www.docker.com/products/docker-desktop"
    Write-INFO "Installer et redémarrer, puis relancer ce script"
    Read-Host "Appuyer sur Entrée pour quitter"
    exit 1
}

# Vérifier que Docker est démarré
try {
    docker ps 2>&1 | Out-Null
    Write-OK "Docker Desktop est démarré"
} catch {
    Write-ERR "Docker Desktop n'est pas démarré !"
    Write-INFO "Lancer Docker Desktop et attendre qu'il soit prêt"
    Read-Host "Appuyer sur Entrée une fois Docker démarré"
}

# ── 3. Cloner le projet ───────────────────────────────────────────
Write-Title "Clonage du projet"

$projectDir = "$env:USERPROFILE\Desktop\freelance-platform"

if (Test-Path $projectDir) {
    Write-INFO "Le dossier existe déjà — mise à jour..."
    Set-Location $projectDir
    git pull origin main
    Write-OK "Projet mis à jour"
} else {
    Write-INFO "Clonage en cours..."
    # Remplacer par l'URL réelle du repo
    $repoUrl = Read-Host "  Entrer l'URL du repo GitHub (ex: https://github.com/username/freelance-platform.git)"
    git clone $repoUrl $projectDir
    Set-Location $projectDir
    Write-OK "Projet cloné dans : $projectDir"
}

# ── 4. Démarrer Docker Compose ────────────────────────────────────
Write-Title "Démarrage des services Docker"

Write-INFO "Démarrage de PostgreSQL, Redis et Mailpit..."
docker compose up -d

Start-Sleep -Seconds 5

# Vérifier que les services sont healthy
$maxRetries = 12
$retries = 0
$allHealthy = $false

while ($retries -lt $maxRetries -and -not $allHealthy) {
    $services = docker compose ps --format json 2>&1
    $healthyCount = (docker compose ps | Select-String "healthy").Count

    if ($healthyCount -ge 3) {
        $allHealthy = $true
    } else {
        Write-INFO "Attente des services... ($retries/$maxRetries)"
        Start-Sleep -Seconds 5
        $retries++
    }
}

if ($allHealthy) {
    Write-OK "PostgreSQL    -> healthy (port 5432)"
    Write-OK "Redis         -> healthy (port 6379)"
    Write-OK "Mailpit       -> healthy (port 1025 / UI: 8025)"
} else {
    Write-INFO "Les services mettent du temps — vérifier avec : docker compose ps"
}

# ── 5. Lancer l'application ───────────────────────────────────────
Write-Title "Démarrage de l'application"

Write-INFO "Téléchargement des dépendances Maven (première fois : 2-5 min)..."
Write-INFO "L'application sera disponible sur : http://localhost:8080/api"
Write-INFO "Swagger UI : http://localhost:8080/api/swagger-ui.html"
Write-INFO "Mailpit UI : http://localhost:8025"
Write-Host ""
Write-INFO "Appuyer sur Ctrl+C pour arrêter l'application"
Write-Host ""

.\mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
