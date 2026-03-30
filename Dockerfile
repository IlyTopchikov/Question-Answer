# Сборка JAR (JDK 21, как в pom.xml)
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn  -DskipTests package

# Запуск (Cloud Run и др. подставляют переменную PORT)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Fly подставляет PORT; JVM ограничиваем под маленькие машины
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
