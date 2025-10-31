# World API - Akka HTTP Project

A typical Akka HTTP REST API project using:
- **JDK 25**
- **Scala 3.3.6**
- **Akka 2.10.6**
- **Akka HTTP 10.7.2**

## Project Structure

```
world/
├── build.sbt                          # Build configuration
├── akka.sbt                           # Akka-related dependency/config split
├── project/
│   ├── build.properties               # SBT version
│   └── plugins.sbt                    # SBT plugins
├── src/
│   ├── main/
│   │   ├── scala/
│   │   │   └── com/aboveland/
│   │   │       ├── Main.scala                     # Application entry point
│   │   │       ├── api/                           # HTTP API layer
│   │   │       │   ├── HttpServer.scala           # HTTP server bootstrap
│   │   │       │   ├── config/
│   │   │       │   │   └── AppConfig.scala        # Application configuration loader
│   │   │       │   ├── routes/
│   │   │       │   │   └── Routes.scala           # Route aggregation (/world, /api/v1)
│   │   │       │   ├── handlers/
│   │   │       │   │   ├── HealthHandler.scala    # Health endpoints
│   │   │       │   │   └── WorldHandler.scala     # World-related endpoints
│   │   │       │   ├── services/
│   │   │       │   │   ├── HealthService.scala    # Health service
│   │   │       │   │   └── WorldService.scala     # World service (actor interaction)
│   │   │       │   ├── middleware/
│   │   │       │   │   ├── CorsDirectives.scala   # CORS handling
│   │   │       │   │   ├── ErrorHandlingDirectives.scala # Error handling
│   │   │       │   │   └── LoggingDirectives.scala # Request/response logging
│   │   │       │   ├── models/
│   │   │       │   │   └── ApiResponse.scala      # API response helpers
│   │   │       │   └── utils/
│   │   │       │       └── JsonSupport.scala      # JSON formats (Spray JSON)
│   │   │       ├── actors/                        # Akka Typed Actors
│   │   │       │   ├── DedicatedServer.scala
│   │   │       │   ├── DedicatedServerManager.scala
│   │   │       │   └── HelloWorldActor.scala
│   │   │       ├── config/
│   │   │       │   └── AkkaConfig.scala           # Akka config helpers
│   │   │       └── models/                        # Core domain models
│   │   │           ├── BaseServer.scala
│   │   │           ├── ErrorResponse.scala
│   │   │           ├── PlayerNumber.scala
│   │   │           └── ServerState.scala
│   │   └── resources/
│   │       ├── application.conf       # Application configuration
│   │       └── logback.xml            # Logging configuration
│   └── test/
│       └── scala/
│           └── com/aboveland/
│               ├── api/
│               │   ├── routes/
│               │   │   └── RoutesSpec.scala
│               │   └── handlers/
│               │       └── UserHandlerSpec.scala
│               └── actors/
│                   └── HelloWorldActorSpec.scala
├── .gitignore
└── README.md
```

## Running the Application

1. **Compile the project:**
   ```bash
   sbt compile
   ```

2. **Run the application:**
   ```bash
   sbt run
   ```

3. **Run tests:**
   ```bash
   sbt test
   ```

4. **Generate IDE files:**
   ```bash
   sbt bloopInstall
   ```

## Key Features

- **RESTful API**: Health checks and world-management endpoints
- **Actor-based world management**: `DedicatedServerManager` orchestrates `DedicatedServer` actors
- **Layered Architecture**: Routes → Handlers → Services → Actors/Models
- **JSON Support**: Spray JSON with custom formats in `JsonSupport`
- **Error Handling**: Centralized exception/rejection handling with uniform JSON errors
- **CORS & Logging**: Out-of-the-box CORS and detailed request/response logging
- **Testing**: Unit tests for routes and actors
- **Configuration**: Typesafe Config via `application.conf`

## API Endpoints

### Health Endpoints (under `/api/v1`)
- `GET /api/v1/health` — Overall health
- `GET /api/v1/health/ready` — Readiness check
- `GET /api/v1/health/live` — Liveness check

### World Endpoints (under `/world`)
- `POST /world/get` — Get world data
- `POST /world/register` — Register or update a dedicated server
  - Body: `BaseServer`
- `POST /world/number` — Update player count for a server
  - Body: `PlayerNumber`
- `POST /world/portal` — Get world portal status
- `POST /world/getDungeonMax` — Get max dungeon capacity
- `POST /world/checkDSState` — Check dedicated server state

Root path `/` redirects to `/api/v1/health`.

## Domain Models (selected)

- `com.aboveland.models.BaseServer` — Dedicated server metadata (sid, machineId, port, map, etc.)
- `com.aboveland.models.PlayerNumber` — Player count update (sid, players)
- `com.aboveland.models.ServerState` — Internal state held by `DedicatedServer`
- `com.aboveland.models.ErrorResponse` — Error code/message used in actor responses

## Dependencies

- **Akka Actor / Akka Typed**: Core actor functionality
- **Akka Stream**: Reactive streams
- **Akka HTTP**: HTTP server framework
- **Spray JSON**: JSON serialization
- **Typesafe Config**: Configuration management
- **Logback**: Logging framework
- **ScalaTest**: Testing framework
- **Akka HTTP TestKit**: HTTP testing utilities

## Package Structure

The project uses the package root `com.aboveland`:
- `com.aboveland.api` — HTTP server, config, routes, handlers, services, middleware, API models, utils
- `com.aboveland.actors` — Actor system (`DedicatedServerManager`, `DedicatedServer`, `HelloWorldActor`)
- `com.aboveland.models` — Core domain models (`BaseServer`, `PlayerNumber`, `ServerState`, `ErrorResponse`)
- `com.aboveland.config` — Akka configuration helpers

