# 🏦 BankSim API

> **Simulation bancaire complète** — Backend Spring Boot reproduisant les processus métier d'une banque réelle : gestion des clients, comptes, transactions, crédits, cartes et audit.

---

## Table des matières

- [Aperçu](#aperçu)
- [Stack technique](#stack-technique)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Installation & Démarrage](#installation--démarrage)
- [Variables d'environnement](#variables-denvironnement)
- [Modules fonctionnels](#modules-fonctionnels)
- [Endpoints REST](#endpoints-rest)
- [Sécurité (JWT)](#sécurité-jwt)
- [Base de données](#base-de-données)
- [Tests](#tests)
- [Documentation API](#documentation-api)
- [Contribuer](#contribuer)

---

## Aperçu

BankSim API est un projet d'exercice Spring Boot qui simule le backend d'une banque. Il couvre l'ensemble du cycle de vie bancaire : inscription KYC, ouverture de comptes, virements ACID, octroi de crédits avec tableau d'amortissement, cartes bancaires et traçabilité complète via audit log.

**Trois rôles utilisateurs :**

| Rôle | Description |
|------|-------------|
| `ADMIN` | Accès total, paramétrage système, rapports |
| `AGENT` | Gestion clients, comptes, traitement des crédits |
| `CLIENT` | Consultation, virements, demandes de crédit |

---

## Stack technique

| Couche | Technologie |
|--------|-------------|
| Framework | Spring Boot 3.x |
| Sécurité | Spring Security + JWT (Access + Refresh Token) |
| Persistence | Spring Data JPA / Hibernate |
| Base de données | PostgreSQL (prod) / H2 (tests) |
| Migration BDD | Flyway |
| Documentation API | SpringDoc OpenAPI 3 (Swagger UI) |
| Validation | Jakarta Bean Validation |
| Audit | Spring AOP (`@Auditable`) |
| Tests | JUnit 5, Mockito, Testcontainers |
| Build | Maven |
| Java | 17+ |

---

## Architecture

```
src/main/java/com/banksim/
├── config/          # Beans Spring, CORS, Swagger, Schedulers
├── controller/      # Couche REST — endpoints HTTP
├── service/         # Logique métier, @Transactional
├── repository/      # Interfaces JpaRepository, requêtes JPQL
├── entity/          # Entités JPA (domaine)
├── dto/             # Request / Response (découplage API ↔ domaine)
├── mapper/          # Conversion Entity ↔ DTO (MapStruct)
├── exception/       # Exceptions métier + @ControllerAdvice global
├── security/        # Filtres JWT, UserDetailsService
├── audit/           # @Auditable annotation + AuditAspect
└── scheduler/       # Tâches planifiées (intérêts, échéances)
```

**Flux d'une requête :**
```
HTTP Request → JwtAuthFilter → Controller → Service (@Transactional) → Repository → PostgreSQL
                                                   ↓
                                            AuditAspect (@AfterReturning)
                                                   ↓
                                            AuditLog (BDD)
```

---

## Prérequis

- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (pour le profil `prod`)
- Docker (optionnel, pour Testcontainers)

---

## Installation & Démarrage

### 1. Cloner le projet

```bash
git clone https://github.com/votre-username/banksim-api.git
cd banksim-api
```

### 2. Configurer les variables d'environnement

Copier le fichier d'exemple et renseigner vos valeurs :

```bash
cp .env.example .env
```

### 3. Créer la base de données PostgreSQL

```sql
CREATE DATABASE banksim;
CREATE USER banksim_user WITH PASSWORD 'votre_mot_de_passe';
GRANT ALL PRIVILEGES ON DATABASE banksim TO banksim_user;
```

### 4. Lancer l'application

```bash
# Profil développement (H2 en mémoire)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Profil production (PostgreSQL)
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

L'API est disponible sur `http://localhost:8080`.  
Swagger UI : `http://localhost:8080/swagger-ui.html`

### Avec Docker Compose (optionnel)

```bash
docker-compose up -d
```

---

## Variables d'environnement

| Variable | Description | Exemple |
|----------|-------------|---------|
| `DB_URL` | URL JDBC PostgreSQL | `jdbc:postgresql://localhost:5432/banksim` |
| `DB_USERNAME` | Utilisateur BDD | `banksim_user` |
| `DB_PASSWORD` | Mot de passe BDD | `secret` |
| `JWT_SECRET` | Clé de signature JWT (min 256 bits) | `your-256-bit-secret-key-here` |
| `JWT_EXPIRY_MS` | Durée de vie access token (ms) | `900000` *(15 min)* |
| `JWT_REFRESH_EXPIRY_MS` | Durée de vie refresh token (ms) | `604800000` *(7 jours)* |
| `MAIL_HOST` | Serveur SMTP | `smtp.gmail.com` |
| `MAIL_PORT` | Port SMTP | `587` |
| `MAIL_USERNAME` | Adresse email | `no-reply@banksim.com` |
| `MAIL_PASSWORD` | Mot de passe email | `app-password` |
| `APP_FRONTEND_URL` | URL frontend (liens emails) | `http://localhost:3000` |

---

## Modules fonctionnels

| # | Module | Fonctionnalités clés |
|---|--------|----------------------|
| 1 | **Auth & Sécurité** | JWT, refresh token, RBAC, blocage après N tentatives |
| 2 | **Clients (KYC)** | Workflow `PENDING → VERIFIED → SUSPENDED → CLOSED`, pièces justificatives |
| 3 | **Comptes** | Courant, Épargne, Terme, Joint — solde, découvert, relevé PDF |
| 4 | **Transactions** | Dépôt, retrait, virement interne/externe — atomicité ACID garantie |
| 5 | **Crédits** | Workflow approbation, tableau d'amortissement, remboursement anticipé |
| 6 | **Cartes** | Débit/crédit, plafonds, blocage, activation PIN |
| 7 | **Notifications** | Alertes email + BDD (solde bas, virement reçu, crédit approuvé…) |
| 8 | **Reporting** | Dashboard, export CSV/PDF, statistiques crédits |
| 9 | **Audit Log** | Traçabilité complète via AOP — qui, quoi, quand, ancienne/nouvelle valeur |

---

## Endpoints REST

> Base URL : `/api/v1` | 🔓 Public | 🟡 CLIENT | 🟢 AGENT | 🔴 ADMIN

### Authentification
| Méthode | Endpoint | Accès | Description |
|---------|----------|-------|-------------|
| `POST` | `/auth/register` | 🔓 | Inscription |
| `POST` | `/auth/login` | 🔓 | Connexion → JWT |
| `POST` | `/auth/refresh` | 🔓 | Renouvellement token |
| `POST` | `/auth/logout` | 🟡 | Déconnexion |
| `POST` | `/auth/forgot-password` | 🔓 | Lien de réinitialisation |

### Clients
| Méthode | Endpoint | Accès | Description |
|---------|----------|-------|-------------|
| `GET` | `/customers` | 🟢🔴 | Lister (paginé, filtré) |
| `POST` | `/customers` | 🟢🔴 | Créer un client |
| `GET` | `/customers/{id}` | 🟡🟢🔴 | Détail client |
| `PUT` | `/customers/{id}` | 🟡🟢🔴 | Modifier profil |
| `PATCH` | `/customers/{id}/kyc` | 🟢🔴 | Valider KYC |

### Comptes
| Méthode | Endpoint | Accès | Description |
|---------|----------|-------|-------------|
| `POST` | `/accounts` | 🟢🔴 | Ouvrir un compte |
| `GET` | `/accounts/{id}` | 🟡🟢🔴 | Détail + solde |
| `PATCH` | `/accounts/{id}/status` | 🟢🔴 | Changer statut |
| `GET` | `/accounts/{id}/transactions` | 🟡🟢🔴 | Historique paginé |
| `GET` | `/accounts/{id}/statement` | 🟡🟢🔴 | Relevé PDF |

### Transactions
| Méthode | Endpoint | Accès | Description |
|---------|----------|-------|-------------|
| `POST` | `/transactions/deposit` | 🟢🔴 | Dépôt |
| `POST` | `/transactions/withdraw` | 🟡🟢🔴 | Retrait |
| `POST` | `/transactions/transfer` | 🟡🟢🔴 | Virement interne |
| `GET` | `/transactions/{ref}` | 🟡🟢🔴 | Détail transaction |
| `POST` | `/transactions/{ref}/reverse` | 🔴 | Annulation (ADMIN) |

### Crédits
| Méthode | Endpoint | Accès | Description |
|---------|----------|-------|-------------|
| `POST` | `/loans/apply` | 🟡 | Demander un crédit |
| `GET` | `/loans/{id}/schedule` | 🟡🟢🔴 | Tableau d'amortissement |
| `PATCH` | `/loans/{id}/approve` | 🟢🔴 | Approuver |
| `PATCH` | `/loans/{id}/reject` | 🟢🔴 | Rejeter |
| `POST` | `/loans/{id}/repay` | 🟡🟢🔴 | Remboursement anticipé |

---

## Sécurité (JWT)

```
POST /auth/login
  → { accessToken, refreshToken }
  
Chaque requête protégée :
  Authorization: Bearer <accessToken>
  
POST /auth/refresh
  → { accessToken }   (avec refreshToken dans le body)
```

- Access token : `15 min` — signé HMAC SHA-256
- Refresh token : `7 jours` — opaque, stocké en BDD, révocable
- Mots de passe : BCrypt (strength 12)
- Blocage compte : après 5 tentatives échouées
- `@PreAuthorize("hasRole('ADMIN')")` sur les endpoints sensibles

---

## Base de données

Les migrations sont gérées par **Flyway**, dans `src/main/resources/db/migration/` :

```
V1__init_schema.sql
V2__seed_roles.sql
V3__add_audit_log.sql
...
```

**Entités principales et relations JPA :**

```
Users ──< RefreshToken
Users ──< Notifications
Users >── Customer ──< Account ──< Transaction
                   ──< Attachment      ──< Card
                   ──< Loan ──< LoanInstallment
                              ──> Account (décaissement)
Account >──< CustomerAccount >── Customer  (compte joint)
Users ──< AuditLog
```

> ⚠️ Tous les montants sont en `BigDecimal`. Utilisation de l'Optimistic Locking (`@Version`) sur `Account` pour éviter les race conditions sur le solde.

---

## Tests

```bash
# Tous les tests
mvn test

# Tests unitaires uniquement
mvn test -Dgroups="unit"

# Tests d'intégration (nécessite Docker pour Testcontainers)
mvn test -Dgroups="integration"

# Rapport de couverture JaCoCo
mvn verify
open target/site/jacoco/index.html
```

**Stratégie de tests :**

| Type | Outil | Cible |
|------|-------|-------|
| Unitaires | JUnit 5 + Mockito | Couche Service — logique métier isolée |
| Intégration | @SpringBootTest + Testcontainers | Couche Repository — vraie BDD PostgreSQL |
| Slice Controller | @WebMvcTest + MockMvc | Endpoints, sérialisation, sécurité |
| Couverture | JaCoCo | Objectif ≥ 80% sur Service + Repository |

---

## Documentation API

Swagger UI disponible en mode `dev` :

```
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs       ← JSON OpenAPI 3
```

---

## Contribuer

```bash
# Créer une branche feature
git checkout -b feature/nom-de-la-feature

# Commits conventionnels
git commit -m "feat(transactions): add reversal endpoint"
git commit -m "fix(auth): handle expired refresh token"
git commit -m "test(loans): add amortization schedule unit tests"

# Push et Pull Request
git push origin feature/nom-de-la-feature
```

**Conventions de commits :** `feat`, `fix`, `test`, `refactor`, `docs`, `chore`

---

<div align="center">

Projet d'exercice Spring Boot — Simulation bancaire

</div>

## Auteur

**cabitibaly** — [GitHub](https://github.com/cabitibaly)