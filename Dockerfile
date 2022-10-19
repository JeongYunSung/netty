FROM --platform=linux/amd64 amazoncorretto:17-alpine3.16

ARG DIR_PATH=app

ENV PORT=8080
ENV FILE_NAME=netty

RUN mkdir $DIR_PATH

WORKDIR $DIR_PATH

COPY /build/libs/*.jar $FILE_NAME.jar

ENTRYPOINT ["java", "-jar", "-Dport=$PORT", "$FILE_NAME.jar"]