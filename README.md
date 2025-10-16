# World API - Akka HTTP Project

A typical Akka HTTP REST API project structure using:
- **JDK 25**
- **Scala 3.3.6**
- **Akka 2.10.6**
- **Akka HTTP 10.7.2**

## Project Structure

```
world-api/
├── build.sbt                          # Build configuration
├── project/
│   ├── build.properties              # SBT version
│   └── plugins.sbt                   # SBT plugins
├── src/
│   ├── main/
│   │   ├── scala/
│   │   │   └── com/aboveland/
│   │   │       ├── Main.scala        # Application entry point
│   │   │       ├── api/              # HTTP API layer
│   │   │       │   ├── HttpServer.scala      # HTTP server
│   │   │       │   ├── config/
│   │   │       │   │   └── AppConfig.scala   # Application configuration
│   │   │       │   ├── routes/
│   │   │       │   │   └── Routes.scala      # Route aggregation
│   │   │       │   ├── handlers/
│   │   │       │   │   ├── UserHandler.scala # User request handlers
│   │   │       │   │   └── HealthHandler.scala # Health check handlers
│   │   │       │   ├── models/
│   │   │       │   │   ├── UserModels.scala  # User data models
│   │   │       │   │   └── ApiResponse.scala # API response models
│   │   │       │   ├── services/
│   │   │       │   │   ├── UserService.scala # User business logic
│   │   │       │   │   └── HealthService.scala # Health service
│   │   │       │   ├── repository/
│   │   │       │   │   └── UserRepository.scala # Data access layer
│   │   │       │   ├── middleware/
│   │   │       │   │   ├── CorsDirectives.scala # CORS handling
│   │   │       │   │   ├── ErrorHandlingDirectives.scala # Error handling
│   │   │       │   │   └── LoggingDirectives.scala # Request logging
│   │   │       │   └── utils/
│   │   │       │       └── JsonSupport.scala # JSON utilities
│   │   │       ├── actors/           # Akka Typed Actors
│   │   │       │   └── HelloWorldActor.scala
│   │   │       └── config/           # Configuration management
│   │   │           └── AkkaConfig.scala
│   │   └── resources/
│   │       ├── application.conf      # Application configuration
│   │       └── logback.xml          # Logging configuration
│   └── test/
│       └── scala/
│           └── com/aboveland/
│               ├── api/              # API tests
│               │   ├── routes/
│               │   │   └── RoutesSpec.scala
│               │   ├── handlers/
│               │   │   └── UserHandlerSpec.scala
│               │   └── integration/
│               └── actors/           # Actor tests
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

- **RESTful API**: Complete HTTP API with CRUD operations
- **Layered Architecture**: Clean separation of concerns (Routes → Handlers → Services → Repository)
- **JSON Support**: Automatic JSON serialization/deserialization with Spray JSON
- **Error Handling**: Comprehensive error handling and standardized API responses
- **CORS Support**: Cross-origin resource sharing configuration
- **Health Checks**: Built-in health, readiness, and liveness endpoints
- **Logging**: Structured logging with request/response tracking
- **Testing**: Unit and integration tests with Akka HTTP TestKit
- **Configuration**: Environment-based configuration management

## API Endpoints

### Health Endpoints
- `GET /api/v1/health` - Health status
- `GET /api/v1/health/ready` - Readiness check
- `GET /api/v1/health/live` - Liveness check

### User Endpoints
- `GET /api/v1/users` - Get all users
- `POST /api/v1/users` - Create a new user
- `GET /api/v1/users/{id}` - Get user by ID
- `PUT /api/v1/users/{id}` - Update user
- `DELETE /api/v1/users/{id}` - Delete user

## Dependencies

- **Akka Actor**: Core actor functionality
- **Akka Stream**: Reactive streams
- **Akka HTTP**: HTTP server framework
- **Spray JSON**: JSON serialization
- **Typesafe Config**: Configuration management
- **Logback**: Logging framework
- **ScalaTest**: Testing framework
- **Akka HTTP TestKit**: HTTP testing utilities

## Development

This is a production-ready Akka HTTP project structure that includes:
- ✅ RESTful API with CRUD operations
- ✅ JSON serialization/deserialization
- ✅ Error handling and validation
- ✅ CORS configuration
- ✅ Health monitoring endpoints
- ✅ Request/response logging
- ✅ Unit and integration tests
- ✅ Configuration management
- 🔄 Database integration (ready for extension)
- 🔄 Authentication/Authorization (ready for extension)
- 🔄 Message serialization (ready for extension)
- 🔄 Clustering and remoting (ready for extension)

## Package Structure

The project uses the package structure `com.aboveland.api` with the following organization:
- `com.aboveland.api` - Main API package
- `com.aboveland.api.routes` - Route definitions and aggregation
- `com.aboveland.api.handlers` - HTTP request handlers
- `com.aboveland.api.services` - Business logic services
- `com.aboveland.api.repository` - Data access layer
- `com.aboveland.api.models` - Data models and DTOs
- `com.aboveland.api.middleware` - HTTP middleware (CORS, logging, error handling)
- `com.aboveland.api.config` - Configuration management
- `com.aboveland.api.utils` - Utility classes

