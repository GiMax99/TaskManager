FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon -x test \
    && cp "$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1)" /workspace/app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /task-manager
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /workspace/app.jar task-manager.jar
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/task-manager/task-manager.jar"]
