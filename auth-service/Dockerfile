# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy the gradle configuration files
COPY settings.gradle .
COPY build.gradle .
COPY shared-proto/build.gradle shared-proto/
COPY auth-service/build.gradle auth-service/

# Copy the shared proto module
COPY shared-proto/src/ shared-proto/src/

# Copy the auth service module
COPY auth-service/src/ auth-service/src/

# Install protoc
RUN apt-get update && apt-get install -y protobuf-compiler

# First build the shared-proto module
RUN gradle :shared-proto:build -x test --no-daemon

# Then build the auth-service module
RUN gradle :auth-service:build -x test --no-daemon

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/auth-service/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]