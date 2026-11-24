
# Approval Workflow System

A backend application built using Spring Boot that manages requests through a configurable, multi-level approval process.

## Features

- User authentication and authorization using JWT.
- Role-based access control.
- Department and user management.
- Configurable multi-level approval workflows.
- Request creation, approval, and rejection.
- Approval history tracking.
- Pagination and sorting for listing APIs.
- Protection against concurrent approval operations.
- Global exception handling.
- API documentation using Swagger UI.

## Tech Stack

- **Backend:** Java, Spring Boot
- **Database:** MySQL
- **ORM:** Spring Data JPA / Hibernate
- **Security:** Spring Security, JWT, BCrypt
- **API Documentation:** Swagger
- **Build Tool:** Maven

## Project Structure

```text
src/main/java/com/kd/aws/
├── config/
├── controller/
├── dto/
├── entity/
├── exception/
├── repository/
├── service/
└── mapper/
```

## Getting Started

### Prerequisites

- Java
- Maven
- MySQL

### Setup

1. Clone the repository:

   ```bash
   git clone <repository-url>
   ```

2. Navigate to the project directory:

   ```bash
   cd approval-workflow-system
   ```

3. Create a MySQL database:

   ```sql
   CREATE DATABASE aws;
   ```

4. Configure your database credentials and JWT secret in `application.properties` or environment variables.

5. Run the application:

   ```bash
   mvn spring-boot:run
   ```

## API Documentation

After starting the application, access Swagger UI at:

```text
http://localhost:8080/swagger-ui/index.html
```

## Purpose

This project was developed to practice and demonstrate backend development concepts using Java and Spring Boot, including REST APIs, authentication, authorization, database relationships, multi-level approval workflows, and concurrency handling.
