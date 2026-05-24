# BookMe — Backend Service

> A robust, scalable booking platform backend inspired by Booking.com.

BookMe serves as the core engine for property management, allowing hosts to list accommodations with complex seasonal pricing logic and enabling users to browse and reserve stays. The system is designed with a focus on security, data integrity, and high-performance media handling.

---

## Table of Contents

- [Features](#features)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [Building & Running](#building--running)

---

## Features

- **Property & Accommodation Management** — Comprehensive CRUD operations for properties, including detailed descriptions and metadata.
- **Seasonal Pricing Logic** — Advanced pricing engine that handles dynamic rates based on dates and seasons.
- **Secure Authentication** — Role-based access control (RBAC) implemented via Spring Security and JWT (JSON Web Tokens).
- **Media Management** — Secure image uploads and storage utilizing AWS S3 integration.
- **Automated Database Migrations** — Version-controlled schema changes using Spring Flyway.
- **Advanced Persistence** — Optimized data access layer with Hibernate and PostgreSQL.

---

## Technologies

| Layer          | Technology            |
| -------------- | --------------------- |
| Language       | Java 21               |
| Framework      | Spring Boot 4.0.1     |
| Security       | Spring Security + JWT |
| Database       | PostgreSQL            |
| ORM            | Hibernate             |
| Cloud Storage  | AWS S3                |
| Migration Tool | Spring Flyway         |
| Build Tool     | Maven 3.9.15          |

---

## Prerequisites

Before getting started, ensure you have the following installed:

- [Java Development Kit (JDK) 21+](https://adoptium.net/)
- [Apache Maven 3.9.15+](https://maven.apache.org/)
- A running [PostgreSQL](https://www.postgresql.org/) database instance

---

## Installation & Setup

**1. Clone the repository:**

```bash
git clone https://github.com/Braxon2/BookMe-app.git
```

**2. Navigate to the project root:**

```bash
cd BookMe-app
cd bookme
```

---

## Configuration

**1. Database Setup**

Create a PostgreSQL database, for example:

```sql
CREATE DATABASE bookme_db;
```

**2. Application Properties**

Update `src/main/resources/application.properties` with your credentials:

```properties
# JWT Configuration
jwt.secret-key=your_jwt_secret_key_here
jwt.expiration=your_jwt_expiration_time_here

# Hibernate & JPA
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate

# Database Connection
spring.datasource.driverClassName=org.postgresql.Driver
spring.datasource.url=database_url_here
spring.datasource.username=your_username
spring.datasource.password=your_password

# Flyway Migrations
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1.1

# AWS S3 Storage
aws.accessKeyId=your_aws_access_key_id
aws.secretKey=your_aws_access_secret_key
aws.s3.region=your_region
aws.s3.bucketName=name_of_your_bucket
```

---

## Building & Running

With your PostgreSQL instance running and `application.properties` configured, start the application with:

```bash
mvn spring-boot:run
```

The service will start and Flyway will automatically apply any pending database migrations on launch.
