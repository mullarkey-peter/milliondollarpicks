# Million Dollar Picks

## Project Overview

Million Dollar Picks is a microservices-based application that allows users to compete using fake money, inspired by Bill Simmons' weekly segment on his podcast. The application provides a platform for users to make predictions and track their betting performance in a playful, risk-free environment.

## Architecture

The project is built using a microservices architecture with the following key components:

### Services
- **Auth Service**: Handles user authentication and authorization
- **User Service**: Manages user profiles and registration
- **Planned Future Services**:
    - Event Service
    - Prediction Service
    - Betting Service
    - Notification Service
    - Reporting Service

### Key Technologies
- Spring Boot
- Java 17
- PostgreSQL
- GraphQL (Netflix DGS)
- gRPC
- Docker Compose
- Gradle

### Key Features
- Secure JWT-based authentication
- GraphQL API
- Microservices communication via gRPC
- Database migrations with Flyway

## Prerequisites

- Docker
- Docker Compose
- Java 17
- Gradle 8.x

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/your-username/million-dollar-picks.git
cd million-dollar-picks
```

### Running the Application

#### Using Docker Compose

```bash
# Build and start all services
docker-compose up --build

# To stop the services
docker-compose down
```

#### Local Development

1. Start PostgreSQL databases
    - Auth DB: Port 5434
    - User DB: Port 5433

2. Set up environment variables
   Create a `.env` file with:
   ```
   DB_USERNAME=postgres
   DB_PASSWORD=postgres
   JWT_SECRET=your-secret-key
   JWT_EXPIRATION=86400000
   ```

3. Run individual services
   ```bash
   # Auth Service
   cd auth-service
   ./gradlew bootRun

   # User Service
   cd user-service
   ./gradlew bootRun
   ```

## API Access

- **GraphiQL Endpoints**:
    - Auth Service: `http://localhost:8082/graphiql`
    - User Service: `http://localhost:8081/graphiql`

### Authentication Workflow

1. Create Credentials
   ```graphql
   mutation {
     createCredentials(
       username: "testuser", 
       password: "password", 
       userId: "1"
     )
   }
   ```

2. Login
   ```graphql
   mutation {
     login(username: "testuser", password: "password") {
       token
       userId
       success
     }
   }
   ```

## Development

### Building the Project

```bash
# Build all services
./gradlew build

# Build a specific service
./gradlew :auth-service:build
./gradlew :user-service:build
```

### Running Tests

```bash
# Run tests for all services
./gradlew test

# Run tests for a specific service
./gradlew :auth-service:test
./gradlew :user-service:test
```

## Deployment Considerations

- Use environment-specific configuration files
- Implement proper secret management
- Configure monitoring and logging
- Set up CI/CD pipelines

## Future Roadmap

- [ ] Add more microservices
- [ ] Implement comprehensive betting logic
- [ ] Add real-time notifications
- [ ] Create frontend applications
- [ ] Implement advanced prediction algorithms

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Contact

Your Name - [your-email@example.com](mailto:your-email@example.com)

Project Link: [https://github.com/your-username/million-dollar-picks](https://github.com/your-username/million-dollar-picks)