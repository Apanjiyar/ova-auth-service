# OVA Auth Service

A Spring Boot authentication microservice for the Online Voting Application (OVA) project. This service handles user registration, login, JWT-based authentication, role management, and integrates with Spring Cloud Config and Netflix Eureka for a microservices architecture.

---

## Tech Stack

| Category            | Technology                                          |
|---------------------|-----------------------------------------------------|
| Language            | Java 17                                             |
| Framework           | Spring Boot 3.4.5                                   |
| Security            | Spring Security 6                                  |
| Database ORM        | Spring Data JPA / Hibernate                        |
| Database            | PostgreSQL                                          |
| JWT                 | JJWT 0.12.6                                        |
| Microservices       | Spring Cloud Config Client, Netflix Eureka Client   |
| Validation          | Hibernate Validator (Jakarta Validation)             |
| Phone Validation    | Google libphonenumber 8.13.40                      |
| Build Tool          | Maven                                              |
| Lombok             | Enabled                                            |

---

## Project Structure

```
src/main/java/com/ms/authservice/
├── AuthServiceApplication.java          # Main Spring Boot entry point
├── config/
│   └── SecurityConfig.java             # Security filter chain & public URIs
├── component/
│   ├── JwtFilter.java                  # JWT authentication filter
│   ├── ApplicationInitializer.java      # Startup: seeds roles & default users
│   └── DefaultUsersConfig.java         # Loads default users from JSON
├── controller/
│   └── AuthController.java             # REST endpoints for auth operations
├── dto/
│   ├── ApiResponse.java                 # Standardized API response wrapper
│   ├── Meta.java                       # Metadata (timestamp, requestId, pagination)
│   ├── ErrorDetail.java                # Field-level error details
│   ├── LoginRequest.java               # Login request DTO
│   ├── RegisterRequest.java            # Registration request DTO
│   ├── RegisterResponse.java           # User registration response DTO
│   └── AssignRolesRequest.java         # Role assignment request DTO
├── entity/
│   ├── BaseEntity.java                  # Abstract base with id, createdAt, updatedAt
│   ├── User.java                        # User entity (implements UserDetails)
│   └── Role.java                        # Role entity
├── enums/
│   ├── RoleEnum.java                    # SUPER_ADMIN, ADMIN, USER
│   └── ErrorCode.java                   # Standardized error codes
├── exception/
│   ├── BaseException.java               # Abstract base exception
│   ├── BusinessException.java           # Business logic errors (422)
│   ├── BadRequestException.java         # Bad request errors (400)
│   ├── UnauthorizedException.java       # Auth failures (401)
│   ├── ResourceNotFoundException.java  # Resource not found (404)
│   └── GlobalExceptionHandler.java      # Centralized exception handling
├── repository/
│   ├── UserRepository.java              # User JPA repository
│   └── RoleRepository.java              # Role JPA repository
├── service/
│   ├── AuthService.java                 # Core auth business logic
│   └── UserDetailsServiceImpl.java      # Spring Security UserDetailsService
├── util/
│   ├── JwtUtil.java                     # JWT token generation & validation
│   ├── ApiResponseUtil.java             # ApiResponse factory helper
│   └── ApplicationUtil.java             # Phone number validation utility
└── validation/
    ├── ValidPassword.java               # Custom password validation annotation
    ├── PasswordValidator.java           # Password rule validator (upper, lower, digit, special, 8+ chars)
    ├── EmailOrPhoneRequired.java        # Custom annotation requiring email OR phone
    └── EmailOrPhoneValidator.java       # Cross-field validation for email/phone
```

---

## Entities

### User
- `username` (unique, required) - used as primary login identifier
- `email` (unique, optional)
- `phone` (unique, optional)
- `emailVerified` / `phoneVerified` (booleans)
- `password` (BCrypt encoded)
- `roles` (Many-to-Many with Role, EAGER fetch)
- Implements `UserDetails` for Spring Security integration

### Role
- `name` (SUPER_ADMIN, ADMIN, USER)

Both extend `BaseEntity` which provides:
- `id` (auto-generated)
- `createdAt` (auto-set on create)
- `updatedAt` (auto-set on update)

---

## API Endpoints

| Method | Endpoint              | Auth Required | Description                     |
|--------|----------------------|---------------|---------------------------------|
| POST   | `/auth/register`      | No            | Register a new user             |
| POST   | `/auth/login`         | No            | Login and receive JWT token      |
| POST   | `/auth/assign-roles`  | Yes (ADMIN)   | Assign roles to a user          |
| GET    | `/auth/get-user-info` | Yes           | Get current authenticated user   |

---

### POST `/auth/register`

**Request:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "phone": "+91-9876543210",
  "password": "Password@123",
  "roles": ["USER"]
}
```

**Notes:**
- Either `email` or `phone` is required (at least one).
- Phone format: `+[country-code]-[10-digit-number]` (e.g., `+91-9876543210`).
- Password rules: min 8 chars, uppercase, lowercase, digit, special char (`@#$%^&+=!`).
- By default, user gets `USER` role automatically.

**Response (201 Created):**
```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "User registered successfully",
  "data": {
    "username": "johndoe",
    "email": "john@example.com",
    "emailVerified": false,
    "phone": "+91-9876543210",
    "phoneVerified": false,
    "roles": ["USER"]
  },
  "errors": [],
  "meta": {
    "timestamp": "2026-04-16T10:30:00Z",
    "requestId": "uuid-here"
  }
}
```

---

### POST `/auth/login`

**Request:**
```json
{
  "identifier": "johndoe",
  "password": "Password@123"
}
```

**Notes:**
- `identifier` can be `username`, `email`, or `phone`.

**Response (200 OK):**
```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Login successful",
  "data": {
    "token": "<jwt-token>"
  }
}
```

---

### POST `/auth/assign-roles`

**Request:**
```json
{
  "username": "johndoe",
  "roles": ["ADMIN", "USER"]
}
```

**Notes:**
- Requires `ADMIN` role.
- Replaces all existing roles for the user.

---

### GET `/auth/get-user-info`

**Response (200 OK):**
```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "User info retrieved successfully",
  "data": {
    "username": "johndoe",
    "email": "john@example.com",
    "emailVerified": false,
    "phone": "+91-9876543210",
    "phoneVerified": false,
    "roles": ["ADMIN", "USER"]
  }
}
```

---

## Security

- **Stateless sessions** - no server-side session storage.
- **JWT authentication** via custom `JwtFilter` that intercepts requests and validates Bearer tokens.
- **BCrypt** password encoding.
- **Public endpoints:** `/auth/login`, `/auth/register`.
- All other endpoints require a valid JWT in the `Authorization: Bearer <token>` header.
- Role-based access via `@PreAuthorize("hasRole('ADMIN')")`.

### JWT Configuration (via Spring Cloud Config)
```yaml
app:
  jwt:
    secret: <your-256-bit-secret>
    expiration-time-ms: 86400000  # 24 hours
```

---

## Standardized API Response Format

Every API response follows this structure:

```json
{
  "success": true|false,
  "code": "SUCCESS|ERROR_CODE",
  "message": "Human-readable message",
  "data": <payload>,
  "errors": [
    { "field": "email", "message": "Email already exists" }
  ],
  "meta": {
    "timestamp": "ISO-8601",
    "requestId": "uuid",
    "page": 1,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10
  }
}
```

---

## Exception Handling

All exceptions are handled centrally by `GlobalExceptionHandler` which returns a consistent `ApiResponse`:

| Exception                    | HTTP Status | Error Code         |
|------------------------------|-------------|--------------------|
| `BadRequestException`        | 400         | BAD_REQUEST        |
| `MethodArgumentNotValidException` | 400     | VALIDATION_ERROR   |
| `ConstraintViolationException` | 400        | VALIDATION_ERROR   |
| `UnauthorizedException`       | 401         | UNAUTHORIZED       |
| `AccessDeniedException`       | 403         | FORBIDDEN          |
| `ResourceNotFoundException`   | 404         | RESOURCE_NOT_FOUND |
| `BusinessException`          | 422         | BUSINESS_ERROR     |
| `Exception` (catch-all)      | 500         | INTERNAL_SERVER_ERROR |

---

## Startup Initialization

On application startup, `ApplicationInitializer` runs as a `CommandLineRunner` and:

1. **Seeds roles** from `RoleEnum` into the `roles` table if they don't exist.
2. **Seeds default users** from `src/main/resources/data/default-users.json` if they don't exist.

### Default Users (`default-users.json`)
```json
[
  {
    "username": "arunpanjiyar",
    "email": "arunpanjiyar76@gmail.com",
    "phone": "+91-9599135426",
    "password": "Apanjiyar123@",
    "roles": ["SUPER_ADMIN", "ADMIN", "USER"]
  }
]
```

---

## Custom Validations

### Password Validation (`@ValidPassword`)
Validates password contains:
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (`@#$%^&+=!`)
- Minimum 8 characters

### Email or Phone Required (`@EmailOrPhoneRequired`)
Class-level constraint on `RegisterRequest` ensuring at least one of `email` or `phone` is provided.

### Phone Number Validation (`ApplicationUtil.validatePhoneNumber`)
Format: `+[country-code]-[10-digit-number]` — regex: `^\+[0-9]{1,3}-[0-9]{10}$`

---

## Configuration

The service uses **Spring Cloud Config** and imports config from `configserver:http://localhost:8888`. Profile is set to `local`.

Required config (expected from Config Server):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_service_db_local
    username: auth_service_admin
    password: auth_service_admin
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

app:
  jwt:
    secret: <256-bit-secret>
    expiration-time-ms: 86400000
```

> See `docs/db_cred_and_permission.md` for detailed PostgreSQL user and permission setup.

---

## Building & Running

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

---

## Testing

```bash
./mvnw test
```

A basic smoke test exists at `src/test/java/com/ms/authservice/AuthServiceApplicationTests.java`.

---

## Microservices Integration

- **Netflix Eureka Client** — registers with Eureka for service discovery.
- **Spring Cloud Config Client** — fetches configuration from a Config Server at `http://localhost:8888`.

---

## Documentation

- `docs/db_cred_and_permission.md` — PostgreSQL setup guide for local development.
