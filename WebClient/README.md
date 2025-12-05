# Ecoembes Web Client

## Overview
This is the web client application for the Ecoembes system. It provides a user-friendly web interface for employees to manage dumpsters, check recycling plant capacities, and assign dumpsters to plants.

## Architecture
The Web Client follows the **Web Client Architecture** pattern with the following components:

### Components
1. **WebClientApplication** - Spring Boot application entry point
2. **WebClientController** - Client Controller that handles HTTP requests from browser
3. **IAuctionsServiceProxy** - Service Proxy interface for backend communication
4. **HTTPServiceProxy** - Concrete implementation using REST/HTTP
5. **Model** - Session-scoped model holding token and current URL

### Design Patterns
- **Service Proxy Pattern**: `IAuctionsServiceProxy` and `HTTPServiceProxy` encapsulate communication with the backend server
- **MVC Pattern**: Controller-View separation with Thymeleaf templates
- **Session Management**: Session-scoped Model bean maintains user state

## Configuration
Edit `src/main/resources/application.properties`:
```properties
# Backend API URL
ecoembes.api.base-url=http://localhost:8081

# Application port
server.port=8082
```

## Running the Application

### Prerequisites
- Java 21
- Backend Ecoembes server running on port 8081

### Start the Web Client
```bash
./gradlew :WebClient:bootRun
```

Access the application at: `http://localhost:8082`

## Features

### Authentication
- Login with email and password
- Session token management
- Logout functionality

### Dumpster Management
- **Create Dumpster**: Register new dumpsters with location and capacity
- **Query Usage**: View dumpster usage statistics over a date range
- **Check Status**: View current status of dumpsters by postal code and date

### Plant Management
- **View All Plants**: List all recycling plants in the system
- **Check Capacity**: View available capacity at plants for a specific date

### Assignment Management
- **Assign Dumpsters**: Assign dumpsters to recycling plants with container count

## Default Credentials
```
Email: admin@ecomebes.com
Password: admin123
```

## Project Structure
```
WebClient/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ecoembes/webclient/
│   │   │       ├── WebClientApplication.java
│   │   │       ├── controller/
│   │   │       │   └── WebClientController.java
│   │   │       ├── proxy/
│   │   │       │   ├── IAuctionsServiceProxy.java
│   │   │       │   └── HTTPServiceProxy.java
│   │   │       └── model/
│   │   │           └── Model.java
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── login.html
│   │       │   ├── index.html
│   │       │   ├── dumpsters/
│   │       │   ├── plants/
│   │       │   └── assignments/
│   │       └── static/
│   │           └── css/
│   │               └── style.css
│   └── test/
└── build.gradle
```

## Technology Stack
- **Spring Boot 3.5.7**
- **Thymeleaf** - Template engine for server-side HTML rendering
- **Spring WebFlux** - WebClient for making HTTP requests to backend
- **Java 21**

## API Communication
The Web Client communicates with the backend server using REST API calls through the `HTTPServiceProxy` class. All requests include an `Authorization` header with the session token.

## UI/UX
- Responsive design with modern CSS
- Color-coded status indicators (green/orange/red)
- User-friendly forms with validation
- Dashboard with quick access to all features

