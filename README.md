# Ticket Booking Platform

A Spring Boot microservices project for ticket booking with services for users, events, pricing, booking, payment, and notifications.

## Services and Ports

| Service | Module | Port |
| --- | --- | --- |
| User Service | user-service | 8081 |
| Event Service | event-service | 8082 |
| Booking Service | booking-service | 8083 |
| Pricing Service | pricing-service | 8084 |
| Payment Service | payment-service | 8085 |
| Notification Service | notification-service | 8086 |

## Tech Stack

- Java 25
- Maven 3.9+
- Spring Boot 3.4.4
- PostgreSQL
- Kafka
- Redis
- H2 (dev profile)

## Prerequisites

1. Java 25 installed and available in PATH
2. Maven installed
3. From project root, verify:

```powershell
java -version
mvn -v
```

## Quick Start (Recommended): Run Without External Dependencies

This mode uses the dev profile and H2 in-memory database.

### 1) Build and install shared module

```powershell
Set-Location "c:\Users\Samarth Kadam\Documents\OOPJ"
mvn -f common/pom.xml install -DskipTests
```

### 2) Start any service in dev mode

```powershell
mvn -f user-service/pom.xml spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Replace user-service with any module:
- event-service
- booking-service
- pricing-service
- payment-service
- notification-service

### 3) Start all services (open one terminal per service)

```powershell
mvn -f event-service/pom.xml spring-boot:run "-Dspring-boot.run.profiles=dev"
mvn -f pricing-service/pom.xml spring-boot:run "-Dspring-boot.run.profiles=dev"
mvn -f payment-service/pom.xml spring-boot:run "-Dspring-boot.run.profiles=dev"
mvn -f booking-service/pom.xml spring-boot:run "-Dspring-boot.run.profiles=dev"
mvn -f notification-service/pom.xml spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### 4) One-command launcher (PowerShell)

From project root:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-all-dev.ps1
```

Optional flags:

```powershell
# Skip re-installing common module
powershell -ExecutionPolicy Bypass -File .\scripts\start-all-dev.ps1 -SkipCommonInstall

# If ports are already occupied, stop old processes and restart services
powershell -ExecutionPolicy Bypass -File .\scripts\start-all-dev.ps1 -RestartPorts

# Override Java and Maven paths
powershell -ExecutionPolicy Bypass -File .\scripts\start-all-dev.ps1 -JavaHome "C:\Users\Samarth Kadam\.jdk\jdk-25" -MavenCmd "C:\Users\Samarth Kadam\.maven\maven-3.9.14\bin\mvn.cmd"
```

## Full Local Stack Mode (PostgreSQL + Kafka + Redis)

Use this mode when you want behavior closer to production.

### Required local services

- PostgreSQL on localhost:5432
- Kafka on localhost:9092
- Redis on localhost:6379

### Required PostgreSQL databases

Create these databases:
- user_db
- event_db
- booking_db
- pricing_db
- payment_db
- notification_db

Expected DB credentials in service configs:
- username: ticketing
- password: ticketing

### Start services in default profile

```powershell
mvn -f common/pom.xml install -DskipTests
mvn -f user-service/pom.xml spring-boot:run
mvn -f event-service/pom.xml spring-boot:run
mvn -f pricing-service/pom.xml spring-boot:run
mvn -f payment-service/pom.xml spring-boot:run
mvn -f booking-service/pom.xml spring-boot:run
mvn -f notification-service/pom.xml spring-boot:run
```

## Build Commands

From project root:

```powershell
mvn clean test-compile
mvn clean test
```

## Useful Notes

- Run from each module pom file to avoid root-aggregator main-class issues.
- If a service fails with dependency resolution for common, run:

```powershell
mvn -f common/pom.xml install -DskipTests
```

- Dev profile avoids the need for PostgreSQL, Kafka, and Redis.

## Troubleshooting

### Frontend availability

This repository currently contains backend microservices only. There is no separate React/Angular/Next frontend module yet.

If you ran all services and are looking for a UI, there is no browser frontend page to open in this repository right now.

You can still test APIs directly using Postman, curl, or VS Code REST client against these base URLs:

- http://localhost:8081 (user-service)
- http://localhost:8082 (event-service)
- http://localhost:8083 (booking-service)
- http://localhost:8084 (pricing-service)
- http://localhost:8085 (payment-service)
- http://localhost:8086 (notification-service)

### Port already in use

On Windows:

```powershell
Get-NetTCPConnection -LocalPort 8081 -State Listen
```

Change the service port in that module's application.yml or stop the conflicting process.

To stop and relaunch all services automatically, use:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-all-dev.ps1 -RestartPorts
```

### PostgreSQL connection refused

Use dev profile or start PostgreSQL and create required databases.

### Kafka or Redis unavailable

Use dev profile or start local Kafka and Redis.

## Project Structure

- common
- user-service
- event-service
- booking-service
- pricing-service
- payment-service
- notification-service

## Security Note

The project includes JWT-based auth settings in configuration. For production, rotate secrets and move credentials to environment variables or a secret manager.
