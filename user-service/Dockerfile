# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy the gradle configuration files
COPY settings.gradle .
COPY build.gradle .
COPY shared-proto/build.gradle shared-proto/
COPY user-service/build.gradle user-service/

# Copy the shared proto module
COPY shared-proto/src/ shared-proto/src/

# Copy the user service module
COPY user-service/src/ user-service/src/

# Install protoc
RUN apt-get update && apt-get install -y protobuf-compiler

# First build the shared-proto module
RUN gradle :shared-proto:build -x test --no-daemon

# Then build the user-service module
RUN gradle :user-service:build -x test --no-daemon

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/user-service/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]