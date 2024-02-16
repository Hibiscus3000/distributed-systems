FROM eclipse-temurin:21-jdk-alpine
# COMPONENT = manager or worker
ARG COMPONENT 
WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY settings.gradle settings.gradle 

WORKDIR lib
COPY lib/build.gradle .
COPY lib/src src

WORKDIR /workspace/${COMPONENT}
COPY ${COMPONENT}/build.gradle .
COPY ${COMPONENT}/src src

WORKDIR /workspace
COPY run.sh .
RUN chmod 777 run.sh
ENTRYPOINT ["./run.sh"]