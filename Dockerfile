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

# No default value set, pass one from docker build or gradle build
ARG BUILD_VERSION

# Set default value or pass one from gradle build
ARG JAR_FILE=build/libs/*.jar
# COPY will copy the application jar file into the image
COPY ${JAR_FILE} fraud-checker-service.jar

# It's important that the -D parameters are before your app.jar
# otherwise they are not recognized.
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=development", "/fraud-checker-service.jar"]

# To build this image use, in the directory where Dockerfile is present
# $> docker build -t dhavaldalal/fraud-checker-service:1.0.0 --build-arg BUILD_VERSION=1.0.0 .

# To run this image use
# We need --expose=9001 as EXPOSE is not a part of this Dockerfile
# $> docker run --expose=9001 -p 9001:9001 dhavaldalal/fraud-checker-service:1.0.0

# To debug the container
# $> docker run -it --rm --entrypoint sh dhavaldalal/fraud-checker-service:1.0.0