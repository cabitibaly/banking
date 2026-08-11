# 🏦 BankSim API

> **Simulation bancaire complète** — Backend Spring Boot reproduisant les processus métier d'une banque réelle : gestion des clients, comptes, transactions, crédits, cartes et audit.

---

## Table des matières

- [Aperçu](#aperçu)
- [Stack technique](#stack-technique)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Installation & Démarrage](#installation--démarrage)
- [Modules fonctionnels](#modules-fonctionnels)
- [Endpoints REST](#endpoints-rest)
- [Sécurité (JWT)](#sécurité-jwt)

---

## Aperçu

Banking API est un projet d'exercice Spring Boot qui simule le backend d'une banque. Il couvre l'ensemble du cycle de vie bancaire : inscription KYC, ouverture de comptes, virements ACID, octroi de crédits avec tableau d'amortissement, cartes bancaires et traçabilité complète via audit log.

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
git clone https://github.com/cabitibaly/banking.git
cd banking
```

L'API est disponible sur `http://localhost:8080`.

### Avec Docker Compose (optionnel)

```bash
docker-compose up -d
```
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
| 8 | **Audit Log** | Traçabilité complète via AOP — qui, quoi, quand, ancienne/nouvelle valeur |

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

- Access token : `1h` — signé HMAC SHA-256
- Refresh token : `7 jours` — opaque, stocké en BDD, révocable
- Mots de passe : BCrypt (strength 12)

---

**Stratégie de tests :**

| Type | Outil | Cible |
|------|-------|-------|
| Unitaires | JUnit 5 + Mockito | Couche Service — logique métier

---

<div align="center">

Projet d'exercice Spring Boot — Simulation bancaire

</div>

## Auteur

**cabitibaly** — [GitHub](https://github.com/cabitibaly)