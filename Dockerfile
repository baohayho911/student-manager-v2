FROM node:22-bookworm-slim AS frontend
WORKDIR /ui
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9.11-eclipse-temurin-21 AS backend
WORKDIR /build
COPY backend/pom.xml ./
RUN mvn -B dependency:go-offline
COPY backend/src ./src
RUN rm -rf src/main/resources/static
COPY --from=frontend /ui/dist ./src/main/resources/static
RUN mvn -B clean verify

FROM eclipse-temurin:21-jre-jammy
RUN groupadd --gid 10001 app && useradd --uid 10001 --gid app --no-create-home app
WORKDIR /app
COPY --from=backend --chown=app:app /build/target/student-manager-2.0.0.jar /app/app.jar
COPY --chown=app:app deploy/start-render.sh /app/start-render.sh
RUN chmod 755 /app/start-render.sh
USER app
ENV SPRING_PROFILES_ACTIVE=render
EXPOSE 10000
ENTRYPOINT ["/app/start-render.sh"]
