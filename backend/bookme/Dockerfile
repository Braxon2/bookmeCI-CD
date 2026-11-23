FROM eclipse-temurin:21-alpine AS builder

WORKDIR /app


RUN apk add --no-cache maven


COPY pom.xml .


RUN mvn dependency:go-offline -B


COPY src/main ./src/main


# CI already runs the full test suite before images are built.
RUN mvn clean package -Dmaven.test.skip=true -B


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/bookme-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
