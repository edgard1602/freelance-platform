#!/bin/bash
# ================================================================
#  install-and-run.sh
#  Script d'installation et démarrage — Freelance Platform
#  Usage : chmod +x install-and-run.sh && ./install-and-run.sh
# ================================================================

set -e

# Couleurs
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

title()  { echo -e "\n${CYAN}========================================${NC}"; echo -e "${CYAN}  $1${NC}"; echo -e "${CYAN}========================================${NC}"; }
ok()     { echo -e "  ${GREEN}[OK]${NC} $1"; }
info()   { echo -e "  ${YELLOW}[INFO]${NC} $1"; }
err()    { echo -e "  ${RED}[ERREUR]${NC} $1"; }

# ── 1. Vérifier Java 21 ───────────────────────────────────────────
title "Vérification Java 21"

# Chercher Java 21 dans les emplacements courants sur Mac
JAVA_PATH=""
POSSIBLE_PATHS=(
    "/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
    "/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
    "$HOME/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
)

for path in "${POSSIBLE_PATHS[@]}"; do
    if [ -d "$path" ]; then
        JAVA_PATH="$path"
        break
    fi
done

# Chercher avec /usr/libexec/java_home
if [ -z "$JAVA_PATH" ]; then
    JAVA_PATH=$(/usr/libexec/java_home -v 21 2>/dev/null || echo "")
fi

if [ -n "$JAVA_PATH" ]; then
    export JAVA_HOME="$JAVA_PATH"
    export PATH="$JAVA_HOME/bin:$PATH"
    ok "Java 21 trouvé : $JAVA_PATH"
    java -version
else
    err "Java 21 introuvable !"
    info "Télécharger sur : https://adoptium.net/temurin/releases/?version=21"
    info "Choisir : macOS AArch64 (Apple Silicon) ou x64 (Intel) — JDK .pkg"
    info "Installer puis relancer ce script"
    exit 1
fi

# ── 2. Vérifier Docker ────────────────────────────────────────────
title "Vérification Docker"

if ! command -v docker &> /dev/null; then
    err "Docker introuvable !"
    info "Télécharger sur : https://www.docker.com/products/docker-desktop"
    info "Installer Docker Desktop pour Mac puis relancer ce script"
    exit 1
fi

ok "Docker trouvé : $(docker -v)"

# Vérifier que Docker est démarré
if ! docker ps &> /dev/null; then
    err "Docker Desktop n'est pas démarré !"
    info "Lancer Docker Desktop depuis le Launchpad et attendre qu'il soit prêt"
    read -p "  Appuyer sur Entrée une fois Docker démarré..."
fi

ok "Docker Desktop est démarré"

# ── 3. Cloner le projet ───────────────────────────────────────────
title "Clonage du projet"

PROJECT_DIR="$HOME/Desktop/freelance-platform"

if [ -d "$PROJECT_DIR" ]; then
    info "Le dossier existe déjà — mise à jour..."
    cd "$PROJECT_DIR"
    git pull origin main
    ok "Projet mis à jour"
else
    info "Clonage en cours..."
    read -p "  Entrer l'URL du repo GitHub : " REPO_URL
    git clone "$REPO_URL" "$PROJECT_DIR"
    cd "$PROJECT_DIR"
    ok "Projet cloné dans : $PROJECT_DIR"
fi

# ── 4. Rendre mvnw exécutable ────────────────────────────────────
chmod +x ./mvnw

# ── 5. Démarrer Docker Compose ────────────────────────────────────
title "Démarrage des services Docker"

info "Démarrage de PostgreSQL, Redis et Mailpit..."
docker compose up -d

sleep 5

# Attendre que les services soient healthy
MAX_RETRIES=12
RETRIES=0

while [ $RETRIES -lt $MAX_RETRIES ]; do
    HEALTHY=$(docker compose ps | grep -c "healthy" || true)
    if [ "$HEALTHY" -ge 3 ]; then
        break
    fi
    info "Attente des services... ($RETRIES/$MAX_RETRIES)"
    sleep 5
    RETRIES=$((RETRIES + 1))
done

ok "PostgreSQL    -> healthy (port 5432)"
ok "Redis         -> healthy (port 6379)"
ok "Mailpit       -> healthy (port 1025 / UI: http://localhost:8025)"

# ── 6. Lancer l'application ───────────────────────────────────────
title "Démarrage de l'application"

info "Téléchargement des dépendances Maven (première fois : 2-5 min)..."
echo ""
info "L'application sera disponible sur  : http://localhost:8080/api"
info "Swagger UI                          : http://localhost:8080/api/swagger-ui.html"
info "Mailpit UI                          : http://localhost:8025"
echo ""
info "Appuyer sur Ctrl+C pour arrêter l'application"
echo ""

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
