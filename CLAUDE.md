# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew build

# Run application
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.example.sftp_sample.SftpSampleApplicationTests"

# Run a single test method
./gradlew test --tests "com.example.sftp_sample.SftpSampleApplicationTests.contextLoads"
```

## Architecture

Spring Boot 4.0.6 / Java 17 application built around **Spring Integration** for SFTP file transfer workflows.

**Key dependencies:**
- `spring-boot-starter-integration` — Spring Integration core (message channels, flows, adapters)
- `spring-integration-http` — HTTP inbound/outbound channel adapters
- `spring-boot-starter-webmvc` — embedded Tomcat + Spring MVC (REST endpoints)

**Intended pattern:** Spring Integration flows connect HTTP endpoints to SFTP operations using `MessageChannel` pipelines with inbound/outbound channel adapters. To add SFTP support, `spring-integration-sftp` must be added to `build.gradle`.

**Package naming note:** The package is `com.example.sftp_sample` (underscore) rather than `com.example.sftp-sample` because hyphens are invalid in Java package names.

**Configuration:** `src/main/resources/application.yaml` — currently only sets `spring.application.name`. SFTP host/port/credentials and integration channel configuration go here.
