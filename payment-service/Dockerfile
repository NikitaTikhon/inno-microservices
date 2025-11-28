FROM maven:3.9.4-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM bellsoft/liberica-openjre-debian:21-cds AS runtime
WORKDIR /paymentservice
COPY --from=build /app/target/payment-service-1.0.0.jar payment-service-1.0.0.jar

ENTRYPOINT ["java", "-jar", "payment-service-1.0.0.jar"]