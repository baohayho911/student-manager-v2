#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
(cd frontend && npm ci && npm run build)
rm -rf backend/src/main/resources/static
mkdir -p backend/src/main/resources/static
cp -R frontend/dist/. backend/src/main/resources/static/
(cd backend && ./mvnw clean verify)
mkdir -p release
cp backend/target/student-manager-2.0.0.jar release/
echo 'Build complete. Run: cd backend && java -jar ../release/student-manager-2.0.0.jar --server.port=8081'
