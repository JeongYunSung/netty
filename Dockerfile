FROM --platform=linux/amd64 amazoncorretto:17-alpine3.16

WORKDIR /app

COPY /build/libs/*.jar netty.jar

ENTRYPOINT ["java", "-jar", "-Dport=8080", "netty.jar"]