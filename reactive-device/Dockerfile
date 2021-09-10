FROM openjdk:8-jdk-slim as build
WORKDIR /workspace/app
COPY build/libs/*.jar .
RUN mkdir -p unpacked && (cd unpacked; jar -xf ../*.jar)

# Multi-stage build to split the dependencies and the app code into different layers
FROM openjdk:8-jdk-slim
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/unpacked
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","pd.sensor.reactive.device.SensorDeviceApplicationKt"]