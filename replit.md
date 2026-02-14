# Health & Environment Office - Capital Secretariat Portal

## Overview
A complete electronic portal system for a Health and Environment Office consisting of:
- **Backend**: Java Spring Boot 3.2.2 API with H2 in-memory database
- **Frontend**: Flutter web application (Dart)
- Both served from a single Spring Boot instance on port 5000

## Architecture
- Spring Boot serves the API at `/api/v1/*` and Flutter web build as static files
- H2 in-memory database (no external database required)
- JWT-based authentication
- SPA routing handled by Spring Boot's resource resolver (fallback to index.html)

## Project Structure
```
/backend/          - Spring Boot backend (Java 17, Maven)
  /src/main/java/  - Java source code
  /src/main/resources/application.yml - Backend configuration
  /pom.xml         - Maven build file
/lib/              - Flutter/Dart frontend source
/web/              - Flutter web entry point
/pubspec.yaml      - Flutter dependencies
/build/web/        - Flutter web build output
/start.sh          - Full build and start script
/assets/           - Frontend assets (images, translations)
```

## Build & Run
1. Flutter web is built with `flutter build web --release --base-href "/"`
2. Build output is copied to `backend/src/main/resources/static/`
3. Backend is built with `mvn package -DskipTests` in the backend directory
4. Application runs via `java -jar backend/target/health-office-backend-0.0.1-SNAPSHOT.jar`

## Default Credentials
- **Admin**: username=`admin`, password=`password`
- **Facility User**: phone=`777777777`, password=`password`

## Key Configuration
- Server port: 5000 (configured in application.yml)
- API base URL: `/api/v1` (relative, same-origin)
- Database: H2 in-memory (no external DB needed)
- JWT secret configured in application.yml

## Recent Changes
- Configured for Replit environment (port 5000, 0.0.0.0 binding)
- API URL changed to relative path for same-origin serving
- Flutter SDK requirement adjusted to ^3.8.0
- Fixed `Curves.backOut` -> `Curves.easeOutBack` for Flutter compatibility
- Fixed AdminDto builder in InspectionService (role -> roles)
- Spring Security configured to allow all static resources
