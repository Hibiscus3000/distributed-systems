FROM eclipse-temurin:21-jdk-alpine as build
# COMPONENT = manager or worker
ARG COMPONENT

WORKDIR /workspace
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle settings.gradle 

WORKDIR /workspace/lib
COPY lib/build.gradle .
COPY lib/src src

WORKDIR /workspace/rabbit
COPY rabbit/build.gradle .
COPY rabbit/src src

WORKDIR /workspace/${COMPONENT}
COPY ${COMPONENT}/build.gradle .
COPY ${COMPONENT}/src src

RUN ["/workspace/gradlew", "build", "-x", "test"]

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /workspace
ARG COMPONENT
COPY --from=build workspace/${COMPONENT}/build/libs/${COMPONENT}-0.0.1-SNAPSHOT.jar app.jar 
ENTRYPOINT ["java", "-jar", "app.jar"]
