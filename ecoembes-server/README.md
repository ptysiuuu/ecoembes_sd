# Ecoembes Central Server

This is the main Ecoembes backend server that coordinates dumpster management and recycling plant operations.

## Prerequisites
- Java 21 or higher
- Gradle 8.x

## Running the application

```bash
./gradlew bootRun
```

The server will start on port **8081**.

## API Documentation

Once the server is running, visit:
- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI spec: http://localhost:8081/v3/api-docs

## Configuration

Edit `src/main/resources/application.properties` to configure:
- Server port
- Database settings
- External service URLs (PlasSB and ContSocket servers)

## Testing

```bash
./gradlew test
```

## Building

```bash
./gradlew build
```

The JAR file will be created in `build/libs/`.

