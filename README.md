# 🎓 UniReminder — Enterprise Bill & Event Notification Platform

[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x%20%7C%20JWT-6DB33F?logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![Spring Data JPA](https://img.shields.io/badge/Data%20Layer-Spring%20Data%20JPA%20%2F%20Hibernate-59666C)](https://spring.io/projects/spring-data-jpa)
[![MySQL](https://img.shields.io/badge/Database-MySQL%208.0-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Thymeleaf](https://img.shields.io/badge/Templates-Thymeleaf%203-005F0F?logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

> **An enterprise-grade, multi-tenant university administration and notification platform designed to streamline campus billing, tuition payment notices, academic event scheduling, and automated multi-channel email reminders.**

---

## 📌 Executive Summary

**UniReminder** bridges the communication gap between university administrators, academic event managers, parents, and students. By automating bill notices, event coordination, and payment tracking, the system minimizes payment delinquency and keeps campus stakeholders informed in real time.

The application features an enterprise **Dual-Tier Architecture**:
1. **Stateless RESTful API (`/api/**`)** secured with **JSON Web Tokens (JJWT 0.12.3)** for mobile/external consumer integration.
2. **Server-Side Rendered (SSR) Web Portal** built with **Thymeleaf**, **HTML5/CSS3**, and **Spring Security Form-Login** with dedicated role-based dashboards.
3. **Automated Cron Scheduling Engine (`@Scheduled`)** powering periodic due-date evaluations, reminder broadcasts, and audit delivery logs.

---

## 🏛️ System Architecture

```
                    ┌──────────────────────────────────────────────┐
                    │            Client Interaction                │
                    │   • Web Browsers (Thymeleaf SSR UI)          │
                    │   • API Clients / Mobile Consumers           │
                    └──────────────────────┬───────────────────────┘
                                           │
                                           ▼
                    ┌──────────────────────────────────────────────┐
                    │           Spring Security 6 Gate             │
                    │   ├── Order(1): /api/** (Stateless + JWT)    │
                    │   └── Order(2): /** (Session + Form Login)   │
                    └──────────────────────┬───────────────────────┘
                                           │
                        ┌──────────────────┴──────────────────┐
                        ▼                                     ▼
        ┌───────────────────────────────┐     ┌───────────────────────────────┐
        │       Web MVC Controllers     │     │      REST API Controllers     │
        │   • BillWebController         │     │   • AuthController (JWT)      │
        │   • EventWebController        │     │   • BillController            │
        │   • UniversityAdminController │     │   • NotificationController    │
        │   • ParentWebController       │     │   • UserController            │
        └───────────────┬───────────────┘     └───────────────┬───────────────┘
                        │                                     │
                        └──────────────────┬──────────────────┘
                                           │
                                           ▼
                    ┌──────────────────────────────────────────────┐
                    │               Service Layer                  │
                    │   • BillService       • EventService         │
                    │   • EmailService      • SchedulerService     │
                    │   • UserService       • ReviewService        │
                    └───────────────┬──────────────────────┬───────┘
                                    │                      │
                                    ▼                      ▼
                    ┌───────────────────────────┐ ┌───────────────────────────┐
                    │   Spring Data JPA Repos   │ │   JavaMailSender Service  │
                    │   (MySQL 8.0 Persistence) │ │  (Thymeleaf HTML Emails)  │
                    └───────────────────────────┘ └───────────────────────────┘
```

---

## 🔑 Key Architectural Capabilities

### 1. Dual Security Filter Chains (Spring Security 6)
- **REST Security Chain (`@Order(1)`)**: Configured with `SessionCreationPolicy.STATELESS` and a custom `JwtAuthenticationFilter` verifying Bearer tokens via `JwtTokenProvider`.
- **Web UI Security Chain (`@Order(2)`)**: Handles cookie/session authentication, login forms, CSRF policies, and fine-grained endpoint authorization based on user roles.

### 2. Fine-Grained Role-Based Access Control (RBAC)
The platform establishes 5 distinct stakeholder roles:
- 🏢 **Company Admin (`ROLE_COMPANY_ADMIN`)**: Oversees universities on the platform, monitors systemic delivery logs, and manages top-tier configurations.
- 🏛️ **University Admin (`ROLE_UNIVERSITY_ADMIN`)**: Manages faculty users, issues bulk tuition bills, creates bill notices, and sends broadcast emails.
- 📅 **Event Manager (`ROLE_EVENT_MANAGER`)**: Publishes campus events, workshops, and extracurricular schedules with automatic calendar invites.
- 👨‍👩‍👧 **Parent (`ROLE_PARENT`)**: Monitors student financial records, reviews pending bill statements, and tracks payment deadlines.
- 🎓 **Student (`ROLE_STUDENT`)**: Views enrolled campus events, fee deadlines, payment statuses, and submits institutional feedback.

### 3. Automated Scheduling & Notification Engine
- **Cron-Driven Background Tasks**: Spring `@Scheduled` background worker (`SchedulerService`) automatically evaluates upcoming payment deadlines.
- **Rich HTML Email Dispatch**: Automated transactional emails powered by `JavaMailSender` and Thymeleaf HTML templates:
  - `bill-notification.html`: Individual fee invoice breakdowns.
  - `bill-notice-notification.html`: Impending due-date reminders.
  - `event-notification.html`: Campus event registrations and schedule updates.
  - `custom-email.html`: Direct administrative communications.
- **Delivery Logging**: Every automated email is recorded into `DeliveryLog` entities for enterprise auditability.

---

## 🛠️ Technology Stack

| Domain | Technology / Library |
| :--- | :--- |
| **Backend Framework** | Spring Boot 3.2.5 |
| **Runtime & Language** | Java 17 LTS |
| **Security & Authentication** | Spring Security 6, JJWT (io.jsonwebtoken 0.12.3) |
| **ORM & Persistence** | Spring Data JPA, Hibernate, MySQL Connector/J |
| **Database** | MySQL 8.0+ |
| **Templating Engine** | Thymeleaf 3, Thymeleaf Extras Spring Security 6 |
| **Email Service** | Spring Boot Starter Mail (JavaMailSender) |
| **Developer Ergonomics** | Project Lombok |
| **Build & Dependency Mgmt** | Apache Maven 3.9+ (`mvnw`) |

---

## 📡 REST API Reference Overview

All REST endpoints operate under `/api/**`. Secure endpoints require an `Authorization: Bearer <token>` header.

| Endpoint | Method | Role / Access | Description |
| :--- | :---: | :---: | :--- |
| `/api/auth/login` | `POST` | Public | Authenticates credentials and returns a signed JWT token |
| `/api/users/**` | `GET/POST/PUT` | Authenticated | User CRUD and profile synchronization |
| `/api/bills/**` | `GET/POST/PUT` | Admin / Parent | Bill generation, status updates, and settlement checks |
| `/api/bill-notices/**` | `GET/POST` | Admin / Student | Management of published tuition and fee notices |
| `/api/events/**` | `GET/POST/DELETE` | Event Manager / All | Academic and extracurricular campus event coordination |
| `/api/notifications/**` | `GET` | Authenticated | User-specific notification inbox and delivery status |
| `/api/reviews/**` | `GET/POST` | Student / Admin | Institutional service feedback and ratings |
| `/api/universities/**` | `GET/POST` | Company Admin | Multi-tenant university institution management |

---

## 🚀 Getting Started & Local Setup

### 1. Prerequisites
- **JDK 17**: Ensure Java 17 is installed (`java -version`).
- **MySQL Server 8.0+**: Running locally on port `3306`.
- **IntelliJ IDEA** (Community or Ultimate) or any modern Java IDE.

### 2. Database Initialization
Create the MySQL database schema before launching the service:
```sql
CREATE DATABASE university_reminder CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Check `src/main/resources/application.properties` and verify your local MySQL credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/university_reminder?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=1234
```
*(Adjust the password to match your local MySQL configuration).*

### 3. Build & Run the Application

#### Option A: Running via Maven Wrapper (Terminal)
```bash
# Clone the repository
git clone https://github.com/JaniduKasuntha/Web-based-Bill-and-Event-Reminder-System.git
cd "Web based Bill and Event Reminder System"

# Run with Maven Wrapper
./mvnw clean spring-boot:run
```

#### Option B: Running in IntelliJ IDEA
1. Open IntelliJ IDEA $\rightarrow$ **File > Open**.
2. Select the repository root folder containing `pom.xml`.
3. Set Project SDK to **Java 17** (**File > Project Structure > Project > SDK 17**).
4. Reload Maven dependencies (**Maven Tool Window > Reload All Maven Projects**).
5. Navigate to `src/main/java/com/university/reminderapp/ReminderAppApplication.java` and click **Run**.

---

## 🌐 Accessing the Application

Once started, the application will listen on port **`8081`**:
- **Web Portal**: [http://localhost:8081](http://localhost:8081)
- **Login Page**: [http://localhost:8081/login](http://localhost:8081/login)

### 👥 Pre-Seeded Test Credentials
The built-in `DataInitializer` seeds test accounts automatically upon application startup:

| Account Type | Email / Username | Password | Role |
| :--- | :--- | :--- | :--- |
| **Company Administrator** | `company_admin@example.com` | `password123` | `ROLE_COMPANY_ADMIN` |
| **University Administrator** | `uni_admin@example.com` | `password123` | `ROLE_UNIVERSITY_ADMIN` |
| **Event Manager** | `event_manager@example.com` | `password123` | `ROLE_EVENT_MANAGER` |
| **Parent User** | `parent@example.com` | `password123` | `ROLE_PARENT` |
| **Student User** | `student@example.com` | `password123` | `ROLE_STUDENT` |

*(Optional SQL user injection script also available in [`insert_test_user.sql`](./insert_test_user.sql)).*

---

## 📂 Repository Structure

```
Web based Bill and Event Reminder System/
├── src/main/java/com/university/reminderapp/
│   ├── config/              # SecurityConfig (Dual FilterChain), DataInitializer
│   ├── controller/
│   │   ├── api/             # RESTful API Controllers (JWT-authenticated)
│   │   └── web/             # Spring MVC Controllers (Thymeleaf views)
│   ├── dto/                 # Request/Response Data Transfer Objects
│   ├── exception/           # GlobalExceptionHandler & custom domain exceptions
│   ├── model/               # JPA Entities (User, Bill, Event, Company, DeliveryLog)
│   ├── repository/          # Spring Data JPA Repository Interfaces
│   ├── security/            # JwtTokenProvider, JwtAuthFilter, CustomUserDetailsService
│   ├── service/             # Business Logic & SchedulerService (@Scheduled cron)
│   └── ReminderAppApplication.java
├── src/main/resources/
│   ├── application.properties # Server port 8081, MySQL, Hibernate settings
│   ├── static/              # CSS stylesheets and vanilla JavaScript assets
│   └── templates/           # Thymeleaf templates (dashboards, bills, events, emails)
├── insert_test_user.sql     # Seed SQL script
├── pom.xml                  # Maven dependencies & build configuration
└── README.md                # Documentation
```

---

## 🧑‍💻 Author & Academic Attribution

- **Developer**: Janidu Kasuntha Rajapaksha
- **Student ID**: IT24101236
- **GitHub Repository**: [https://github.com/JaniduKasuntha/Web-based-Bill-and-Event-Reminder-System](https://github.com/JaniduKasuntha/Web-based-Bill-and-Event-Reminder-System)
