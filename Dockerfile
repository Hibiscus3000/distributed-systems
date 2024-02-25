FROM eclipse-temurin:21-jdk-alpine as build
# COMPONENT = manager or worker
ARG COMPONENT

WORKDIR /workspace
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle settings.gradle 

WORKDIR lib
COPY lib/build.gradle .
COPY lib/src src

WORKDIR /workspace/${COMPONENT}
COPY ${COMPONENT}/build.gradle .
COPY ${COMPONENT}/src src

RUN ["/workspace/gradlew", "build"]

FROM eclipse-temurin:21-jdk-alpine as extract
WORKDIR /workspace
ARG COMPONENT
COPY --from=build workspace/${COMPONENT}/build/libs/${COMPONENT}-0.0.1-SNAPSHOT.jar app.jar 
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /workspace
COPY --from=extract /workspace/dependencies ./
COPY --from=extract /workspace/spring-boot-loader ./
COPY --from=extract /workspace/snapshot-dependencies ./
COPY --from=extract /workspace/application ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
