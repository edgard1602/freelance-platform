# Freelance Platform — Backend

Plateforme de mise en relation freelances/clients — monolithe modulaire Spring Boot 4.

---

## Stack technique

| Couche | Technologie |
|---|---|
| Framework | Spring Boot 4.0.6 |
| Langage | Java 21 |
| Sécurité | Spring Security 7 + JWT (jjwt 0.12.6) |
| Persistance | Spring Data JPA + Hibernate 7 |
| Base de données | PostgreSQL 17 |
| Cache & Sessions | Redis 7 |
| Migrations | Flyway 11 |
| Mapping | MapStruct 1.6.3 |
| Temps-réel | WebSocket STOMP |
| Documentation | SpringDoc OpenAPI 3.1 |
| Tests | JUnit 5 + Testcontainers |

---

## Architecture

```
com.freelance.freelance_platform/
│
├── FreelancePlatformApplication.java
│
├── config/              ← AppProperties, WebMvcConfig, RedisConfig, WebSocketConfig
├── security/            ← JwtService, JwtAuthFilter, SecurityConfig
│
├── shared/              ← Kernel partagé entre tous les modules
│   ├── domain/          ← BaseEntity, SoftDeletableEntity
│   ├── enums/           ← UserRole, ProjectStatus, ApplicationStatus...
│   ├── exception/       ← ErrorCode, BusinessException, GlobalExceptionHandler
│   ├── response/        ← ApiResponse<T>, ApiError, PageResponse<T>
│   ├── event/           ← DomainEvent (communication inter-modules)
│   └── util/            ← SecurityUtils
│
├── identity/            ← Auth, utilisateurs, profils freelance
├── project/             ← Projets, candidatures, contrats
├── messaging/           ← Chat temps-réel WebSocket
├── notification/        ← Notifications SSE + ApplicationEvents
└── review/              ← Évaluations et notations
```

**Règle d'or** : les modules ne s'appellent jamais directement. Ils communiquent via `ApplicationEvent` — ce qui permet d'extraire n'importe quel module en microservice sans refactoring.

---

## Prérequis

- Java 21 (Eclipse Temurin recommandé)
- Docker Desktop
- Maven 3.9+ (ou utiliser `./mvnw`)

---

## Démarrage rapide

### 1. Cloner le projet

```bash
git clone https://github.com/TON_USERNAME/freelance-platform.git
cd freelance-platform
```

### 2. Configurer les variables d'environnement

```bash
cp .env.example .env
# Éditer .env selon vos besoins
```

### 3. Démarrer les services Docker

```bash
docker compose up -d
docker compose ps  # vérifier que les 3 services sont healthy
```

### 4. Lancer l'application

```bash
# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Windows PowerShell
.\mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
```

L'application démarre sur **http://localhost:8080/api**

---

## Services Docker

| Service | Port | Description |
|---|---|---|
| PostgreSQL 17 | 5432 | Base de données principale |
| Redis 7 | 6379 | Cache + stockage refresh tokens |
| Mailpit | 1025 / 8025 | SMTP local + UI emails |

---

## Endpoints principaux

### Authentification — `/auth`

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/auth/register` | Créer un compte | Non |
| POST | `/auth/login` | Se connecter | Non |
| POST | `/auth/refresh` | Rafraîchir le token | Non |
| POST | `/auth/logout` | Se déconnecter | Oui |

### Projets — `/projects`

| Méthode | Endpoint | Description | Rôle |
|---|---|---|---|
| POST | `/projects` | Créer un projet | CLIENT |
| GET | `/projects` | Lister les projets ouverts | Tous |
| GET | `/projects/{id}` | Détail d'un projet | Tous |
| GET | `/projects/my` | Mes projets | CLIENT |
| PUT | `/projects/{id}` | Modifier un projet | CLIENT |
| DELETE | `/projects/{id}` | Supprimer un projet | CLIENT |
| POST | `/projects/applications` | Postuler à un projet | FREELANCER |
| GET | `/projects/{id}/applications` | Candidatures d'un projet | CLIENT |
| POST | `/projects/applications/{id}/accept` | Accepter une candidature | CLIENT |

### Messagerie — `/messages`

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/messages` | Envoyer un message | Oui |
| GET | `/messages/conversation/{userId}` | Historique conversation | Oui |
| GET | `/messages/unread` | Messages non lus | Oui |
| GET | `/messages/unread/count` | Nombre non lus | Oui |
| PUT | `/messages/conversation/{id}/read` | Marquer comme lu | Oui |

### Notifications — `/notifications`

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/notifications` | Mes notifications | Oui |
| GET | `/notifications/unread/count` | Nombre non lues | Oui |
| PUT | `/notifications/read-all` | Tout marquer comme lu | Oui |

### Évaluations — `/reviews`

| Méthode | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/reviews` | Créer une évaluation | Oui |
| GET | `/reviews/user/{id}` | Évaluations d'un utilisateur | Oui |
| GET | `/reviews/user/{id}/rating` | Note moyenne | Oui |

---

## Documentation API

Swagger UI disponible en profil `dev` :

```
http://localhost:8080/api/swagger-ui.html
```

---

## WebSocket

Connexion STOMP via SockJS :

```javascript
const socket = new SockJS('http://localhost:8080/api/ws');
const client = Stomp.over(socket);

client.connect({ Authorization: 'Bearer ' + token }, () => {
    // Écouter les messages
    client.subscribe('/user/queue/messages', (msg) => {
        console.log(JSON.parse(msg.body));
    });

    // Écouter les notifications
    client.subscribe('/user/queue/notifications', (notif) => {
        console.log(JSON.parse(notif.body));
    });
});
```

---

## Migrations Flyway

| Version | Description |
|---|---|
| V1 | Schéma initial complet (toutes les tables) |
| V2 | Fix colonne `updated_at` — table `skills` |
| V3 | Fix colonne `updated_at` — table `messages` |
| V4 | Fix colonne `updated_at` — table `notifications` |
| V5 | Fix colonne `updated_at` — table `reviews` |

---

## Profils Spring

| Profil | Usage | Base de données |
|---|---|---|
| `dev` | Développement local | `localhost:5432/freelance_dev` |
| `prod` | Production | Variables d'environnement |
| `docker` | Docker Compose | Noms de services compose |

---

## Sécurité

- **JWT stateless** — pas de session serveur
- **Refresh tokens** stockés dans Redis avec TTL automatique
- **BCrypt** pour le hashage des mots de passe
- **RBAC** via `@PreAuthorize` — rôles `FREELANCER`, `CLIENT`, `ADMIN`
- **CORS** configurable via `app.cors.allowed-origins`
- **Soft delete** sur `User` et `Project` — données jamais supprimées physiquement

---

## Structure des réponses HTTP

### Succès
```json
{
  "success": true,
  "message": "Opération réussie",
  "data": { ... },
  "timestamp": "2026-05-16T10:00:00Z"
}
```

### Erreur
```json
{
  "code": "AUTH_001",
  "message": "Email ou mot de passe incorrect",
  "timestamp": "2026-05-16T10:00:00Z"
}
```

### Erreur de validation
```json
{
  "code": "VAL_001",
  "message": "Données invalides",
  "errors": [
    { "field": "email", "message": "Format email invalide" },
    { "field": "password", "message": "Le mot de passe doit contenir au moins 8 caractères" }
  ],
  "timestamp": "2026-05-16T10:00:00Z"
}
```

---

## Codes d'erreur

| Code | Description | HTTP |
|---|---|---|
| AUTH_001 | Email ou mot de passe incorrect | 401 |
| AUTH_002 | Token expiré | 401 |
| AUTH_003 | Token invalide | 401 |
| AUTH_004 | Email non vérifié | 403 |
| AUTH_005 | Compte suspendu | 403 |
| USER_001 | Utilisateur introuvable | 404 |
| PROJ_001 | Projet introuvable | 404 |
| PROJ_002 | Projet fermé aux candidatures | 409 |
| APP_001 | Candidature introuvable | 404 |
| APP_002 | Déjà postulé à ce projet | 409 |
| CONT_001 | Contrat introuvable | 404 |
| REV_001 | Auto-évaluation interdite | 400 |
| REV_002 | Évaluation déjà existante | 409 |
| SEC_001 | Action non autorisée | 403 |
| VAL_001 | Données invalides | 400 |
| VAL_002 | Email déjà utilisé | 409 |
| SRV_001 | Erreur interne | 500 |

---

## Versioning Git

Ce projet suit les conventions **GitHub Flow** + **Conventional Commits**.

```
main        ← stable, deployable en prod
develop     ← intégration continue
feat/*      ← nouvelles fonctionnalités
fix/*       ← corrections de bugs
chore/*     ← maintenance
```

Exemple de commit :
```
feat(identity): add JWT refresh token rotation
fix(project): prevent duplicate applications
chore(deps): upgrade mapstruct to 1.6.3
```

---

## Licence

MIT — Projet académique IPNET Institute of Technology
