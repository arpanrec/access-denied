# syntax=docker/dockerfile:experimental
ARG JAVA_VERSION=21
FROM eclipse-temurin:${JAVA_VERSION} AS builder

WORKDIR /app

COPY . /app/

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :access-denied-backend:clean :access-denied-backend:bootJar --stacktrace --info -x test

FROM eclipse-temurin:${JAVA_VERSION}-jre
WORKDIR /app
#COPY --from=builder /app/access-denied-backend/build/libs/access-denied-backend-boot-* /app.jar

ADD access-denied-backend/build/libs/access-denied-backend-boot-*.jar /app.jar

CMD ["java", "-jar", "/app.jar"]
