# Build stage
FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts ./
COPY build.gradle.kts ./
COPY src/ src/

RUN ./gradlew build -x test --no-daemon

# Runtime stage
FROM amazoncorretto:17-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*-all.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
