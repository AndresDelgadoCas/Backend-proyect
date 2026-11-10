FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 10000

# Render gives 512 MB; without this the JVM only uses a quarter of it as heap.
CMD ["sh", "-c", "java -XX:MaxRAMPercentage=70 -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError -jar target/*.jar --spring.flyway.enabled=true"]
