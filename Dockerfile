FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
RUN mkdir -p /app/logs/archived && \
    chmod -R 755 /app/logs
COPY target/rag-chat-storage-service-1.0.0.jar app.jar
VOLUME ["/app/logs"]
ENTRYPOINT ["java", "-jar", "app.jar"]