# ContSocket Recycling Plant Server

This server simulates a container recycling plant using socket-based communication.

## Prerequisites
- Java 21 or higher
- Gradle 8.x

## Running the application

```bash
./gradlew run
```

The server will start on port **9090**.

## Protocol

The server accepts socket connections and responds to:
- Capacity queries: `GET_CAPACITY <plantId>`
- Notification of incoming dumpsters: `NOTIFY <plantId> <numDumpsters> <totalContainers> <arrivalDate>`
  - Response: `OK` on success, `ERROR: <message>` on failure

## Testing

```bash
./gradlew test
```

## Building

```bash
./gradlew build
```

