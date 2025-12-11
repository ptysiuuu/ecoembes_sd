# PlasSB Recycling Plant Server

This server simulates a plastic recycling plant that processes containers.

## Prerequisites
- Java 21 or higher
- Gradle 8.x

## Running the application

```bash
./gradlew bootRun
```

The server will start on port **8083**.

## API Endpoints

- `GET /api/plants/{plantId}/capacity?date={date}` - Get plant capacity for a specific date
- `POST /api/plants/{plantId}/notify` - Receive notification of incoming dumpsters
  - Request body: `DumpsterNotificationDTO` with `plantId`, `dumpsterIds`, `totalContainers`, `arrivalDate`
  - Response: Confirmation message

## Database

Uses H2 in-memory database. Initial data is loaded from `src/main/resources/data.sql`.

## Testing

```bash
./gradlew test
```

