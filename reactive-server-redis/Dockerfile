FROM openjdk:8-jdk-alpine as build
WORKDIR /workspace/app
COPY build/libs/*.jar .
RUN mkdir -p unpacked && (cd unpacked; jar -xf ../*.jar)

# Multi-stage build to split the dependencies and the app code into different layers
FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/unpacked
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Documenting ports to be exposed when running in Docker
EXPOSE 8080

ENTRYPOINT ["java","-cp","app:app/lib/*","pd.sensor.reactive.server.SensorServerApplicationKt"]