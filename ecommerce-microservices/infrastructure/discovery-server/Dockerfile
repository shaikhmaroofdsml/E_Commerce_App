# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY infrastructure/config-server/pom.xml infrastructure/config-server/pom.xml
COPY infrastructure/discovery-server/pom.xml infrastructure/discovery-server/pom.xml
COPY infrastructure/api-gateway/pom.xml infrastructure/api-gateway/pom.xml
COPY microservices/customer-service/pom.xml microservices/customer-service/pom.xml
COPY microservices/product-service/pom.xml microservices/product-service/pom.xml
COPY microservices/order-service/pom.xml microservices/order-service/pom.xml
COPY microservices/payment-service/pom.xml microservices/payment-service/pom.xml
COPY microservices/notification-service/pom.xml microservices/notification-service/pom.xml
COPY microservices/admin-service/pom.xml microservices/admin-service/pom.xml

RUN mvn dependency:go-offline -pl infrastructure/discovery-server -am -q

COPY infrastructure/discovery-server/src infrastructure/discovery-server/src

RUN mvn package -pl infrastructure/discovery-server -am -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="ecommerce-team"
LABEL service="discovery-server"

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=build /workspace/infrastructure/discovery-server/target/*.jar app.jar

RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8761

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
