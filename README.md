# Ktor Beer 🍺

A modern REST API built with Ktor framework for managing beer data, featuring MongoDB integration and comprehensive
testing.

## Features

- **RESTful API** - Built with Ktor framework
- **MongoDB Integration** - Coroutine-based MongoDB driver for async operations
- **Dependency Injection** - Koin for clean dependency management
- **Functional Programming** - Arrow-kt for functional programming constructs
- **Comprehensive Testing** - Kotest framework
- **Code Coverage** - JaCoCo integration with 80% coverage threshold
- **API Documentation** - OpenAPI/Swagger integration
- **Docker Support** - Docker Compose configuration included
- **Hexagonal Architecture** - minimal hexagonal structure

## Getting Started

### Prerequisites

- Java 17+
- Docker & Docker Compose (for MongoDB)

### Running the Application

1. **Clone the repository**
   ```bash
   git clone git@github.com:enolive/ktor-beer.git
   cd ktor-beer
   ```

2. **Start App with Docker Compose**
   ```bash
   docker compose up -d
   ```
3. **Access the API**
   - API Base URL: `http://localhost:8080`
   - OpenAPI Documentation: `http://localhost:8080/openapi`

Alternatively, you can also just start **MongoDB** and run the application with **Gradle**

```bash
docker compose -f compose.dev.yml up -d
./gradlew run
```

### Testing

Run all tests with coverage:

```bash
./gradlew test jacocoTestReport
```

View coverage report:

```bash
open build/reports/jacoco/test/html/index.html
```

### API Testing

Use the provided HTTP request files:

- Beer-specific API requests `http-requests/Beers.http`

These can be executed directly in IntelliJ IDEA or any HTTP client.
