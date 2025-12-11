# Ecoembes Web Client

Web-based user interface for the Ecoembes system.

## Prerequisites
- Java 21 or higher
- Gradle 8.x
- Ecoembes Server running on port 8081

## Running the application

```bash
./gradlew bootRun
```

The web client will start on port **8082**.

Access the application at: http://localhost:8082

## Configuration

Edit `src/main/resources/application.properties` to configure:
- Server port
- Backend API URL (Ecoembes Server)

## Features

- Employee login/logout
- Create and manage dumpsters
- Query dumpster usage
- Check recycling plant capacities
- Assign dumpsters to plants

## Architecture

This application uses:
- **Proxy Pattern** - Service proxy communicates with backend API
- **MVC Pattern** - Controllers handle web requests, views render UI
- **Thymeleaf** - Server-side template engine

## Testing

```bash
./gradlew test
```

