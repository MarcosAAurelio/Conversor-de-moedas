FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src src
RUN sed -i 's/\r$//' mvnw \
    && chmod +x mvnw \
    && ./mvnw --batch-mode --no-transfer-progress -DskipTests package \
    && cp target/conversor-de-moedas-0.0.1-SNAPSHOT.jar /workspace/application.jar

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S -g 10001 app \
    && adduser -S -D -H -u 10001 -G app app
WORKDIR /app
COPY --from=build --chown=10001:10001 /workspace/application.jar /app/app.jar
COPY --chown=10001:10001 deploy/app/start-app.sh /usr/local/bin/start-app
RUN chmod 0555 /usr/local/bin/start-app

USER 10001:10001
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
    CMD wget -q -O /dev/null http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["/usr/local/bin/start-app"]
CMD ["-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
