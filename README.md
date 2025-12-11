# Ecoembes Project - Separated Services

This repository contains 4 independent Spring Boot/Java projects that form the Ecoembes waste management system.

## Project Structure

```
Ecoembes-Separated/
├── ecoembes-server/      # Main backend server (Port 8081)
├── plassb-server/        # Plastic recycling plant (Port 8083)
├── contsocket-server/    # Container recycling plant (Port 9090)
├── webclient/            # Web UI (Port 8082)
├── start-all-services.ps1
└── stop-all-services.ps1
```

## Quick Start

### Option 1: Start all services with one script (Windows)
```powershell
.\start-all-services.ps1
```

### Option 2: Start services individually

1. **Ecoembes Server** (must start first):
```bash
cd ecoembes-server
.\gradlew bootRun
```

2. **PlasSB Server**:
```bash
cd plassb-server
.\gradlew bootRun
```

3. **ContSocket Server**:
```bash
cd contsocket-server
.\gradlew run
```

4. **Web Client**:
```bash
cd webclient
.\gradlew bootRun
```

## Service URLs

- **Ecoembes Server API**: http://localhost:8081
  - Swagger UI: http://localhost:8081/swagger-ui.html
- **Web Client**: http://localhost:8082
- **PlasSB Server**: http://localhost:8083
- **ContSocket Server**: Socket on port 9090

## Prerequisites

- Java 21 or higher
- Gradle 8.x (included via wrapper)

## Default Login Credentials

- Email: `admin@ecomebes.com`
- Password: `admin123`

## Architecture

The system uses several design patterns:
- **Service Gateway Pattern**: Communication with external recycling plants
- **Factory Pattern**: Creating service gateway instances
- **DAO Pattern**: Data persistence
- **Proxy Pattern**: Web client communicates with backend
- **MVC Pattern**: Web application structure

## Opening in IntelliJ IDEA

### Method 1: Open all projects in one window
1. Open IntelliJ IDEA
2. File → Open
3. Navigate to `Ecoembes-Separated` folder
4. Select all 4 project folders (hold Ctrl/Cmd)
5. Click OK

### Method 2: Individual projects
1. File → Open
2. Select one project folder (e.g., `ecoembes-server`)
3. Repeat for other projects in separate windows

## Creating Compound Run Configuration in IntelliJ

1. Run → Edit Configurations
2. Click "+" → Compound
3. Name it "Run All Ecoembes Services"
4. Add all 4 configurations:
   - ecoembes-server [bootRun]
   - plassb-server [bootRun]
   - contsocket-server [run]
   - webclient [bootRun]
5. Apply and OK

Now you can start all services with one click!

## Testing

Each project has its own tests:
```bash
cd <project-folder>
.\gradlew test
```

## Building

Build all projects:
```bash
cd ecoembes-server && .\gradlew build
cd ..\plassb-server && .\gradlew build
cd ..\contsocket-server && .\gradlew build
cd ..\webclient && .\gradlew build
```

## Stopping Services

### Windows
```powershell
.\stop-all-services.ps1
```

### Manual
Stop each Gradle process with `Ctrl+C` in their respective terminals.

## Documentation

Each service has its own README with specific details:
- [Ecoembes Server](ecoembes-server/README.md)
- [PlasSB Server](plassb-server/README.md)
- [ContSocket Server](contsocket-server/README.md)
- [Web Client](webclient/README.md)

## License

[Your License Here]

