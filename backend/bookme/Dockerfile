FROM eclipse-temurin:21-alpine AS builder

WORKDIR /app

# Install Maven globally inside the Alpine builder container
RUN apk add --no-cache maven

# Copy only the project definition first to leverage Docker layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Build the application using global maven
RUN mvn clean package -DskipTests -B


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/bookme-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]