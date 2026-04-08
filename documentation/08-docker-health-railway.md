# Docker + Health Check (Railway Ready)

## What was done

- Added a production-ready multi-stage `Dockerfile` for Java 21.
- Added Spring Boot Actuator dependency to expose health endpoints.
- Configured health/liveness/readiness endpoint exposure in `application.properties`.
- Kept application runtime compatible for both local and Railway environments via environment variable fallbacks.
- Updated `README.md` with Docker usage and health endpoint details.

## Key changes

### 1) Dockerization

- New file: `Dockerfile`
- Multi-stage build:
  - **Build stage:** `maven:3.9.9-eclipse-temurin-21`
  - **Runtime stage:** `eclipse-temurin:21-jre-jammy`
- Build runs with tests skipped for image build speed:
  - `mvn -q -DskipTests clean package`
- Runtime image copies only final JAR.
- Default runtime port is set with environment variable support:
  - `ENV SERVER_PORT=8080`
  - App reads `SERVER_PORT`/`PORT` from Spring config.

### 2) Health check endpoint

- Added dependency in `pom.xml`:
  - `org.springframework.boot:spring-boot-starter-actuator`
- Added health configuration in `src/main/resources/application.properties`:
  - `management.endpoints.web.exposure.include=health,info`
  - `management.endpoint.health.probes.enabled=true`
  - `management.health.livenessstate.enabled=true`
  - `management.health.readinessstate.enabled=true`

Available endpoints:

- `/actuator/health`
- `/actuator/health/liveness`
- `/actuator/health/readiness`

## Notes

- Local run remains unchanged (`8081`, local Mongo fallback).
- Railway run uses environment variables (`SERVER_PORT`/`PORT`, `MONGO_URL`).
- No existing application business functionality was removed or altered.

## Other important information

- Suggested commit message:
  - `feat: add production Dockerfile and actuator health checks for local + Railway runtime`
