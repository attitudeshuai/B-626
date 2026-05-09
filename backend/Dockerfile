# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Wait for MySQL
RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

COPY <<'EOF' /app/wait-for-mysql.sh
#!/bin/bash
echo "Waiting for MySQL..."
while ! nc -z mysql 3306; do
  sleep 1
done
echo "MySQL is ready!"
exec java -jar app.jar
EOF

RUN chmod +x /app/wait-for-mysql.sh

EXPOSE 8000

CMD ["/app/wait-for-mysql.sh"]
