#!/bin/bash
set -e

echo "=== Building Flutter web ==="
cd /home/runner/workspace
flutter build web --release --base-href "/" 2>&1 | tail -5

echo "=== Copying Flutter build to Spring Boot static resources ==="
rm -rf /home/runner/workspace/backend/src/main/resources/static
mkdir -p /home/runner/workspace/backend/src/main/resources/static
cp -r /home/runner/workspace/build/web/* /home/runner/workspace/backend/src/main/resources/static/

echo "=== Building Spring Boot backend ==="
cd /home/runner/workspace/backend
mvn package -DskipTests -q 2>&1 | tail -5

echo "=== Starting application on port 5000 ==="
java -jar /home/runner/workspace/backend/target/health-office-backend-0.0.1-SNAPSHOT.jar
