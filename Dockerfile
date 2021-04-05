# FROM tells Docker that this image is based on the OpenJDK 11 Alpine
# base image. This means we’ll be using Alpine Linux, which is
# lightweight and fast. It’s bundled up with a Java installation so we
# don’t have to worry about installing it separately.
FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine

# Create a non-root group and user
# RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN addgroup -S tsys && adduser -S dhaval -G tsys

# Tell docker that all future commands should run as the appuser user
# USER appuser
USER dhaval:tsys

ARG JAR_FILE=build/libs/*.jar
# COPY will copy the application jar file into the image
COPY ${JAR_FILE} fraud-checker-service.jar

# It's important that the -D parameters are before your app.jar
# otherwise they are not recognized.
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=development", "/fraud-checker-service.jar"]

# To build this image use, in the directory where Dockerfile is present
# $> docker build -t com.tsys/fraud-checker-service .

# To run this image use
# $> docker run -p 9001:9001 com.tsys/fraud-checker-service