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

USER academicai
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
