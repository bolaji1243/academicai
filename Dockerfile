FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN addgroup --system academicai && adduser --system --ingroup academicai academicai
COPY --from=build /workspace/build/libs/*.jar app.jar

RUN mkdir -p /app/uploads && chown -R academicai:academicai /app

USER academicai
EXPOSE 8080

ENTRYPOINT ["java", \
    "-Xms256m", \
    "-Xmx400m", \
    "-XX:MaxMetaspaceSize=128m", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-jar", "app.jar"]
