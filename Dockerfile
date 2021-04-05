FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine
# Create a non-root group and user
# RUN addgroup -S appgroup && adduser -S appuser -G appgroup
# Tell docker that all future commands should run as the appuser user
# USER appuser
RUN addgroup -S tsys && adduser -S dhaval -G tsys
USER dhaval:tsys
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} fraud-checker-service.jar
# It's important that the -D parameters are before your app.jar
# otherwise they are not recognized.
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=development", "/fraud-checker-service.jar"]

# To build this image use, in the directory where Dockerfile is present
# $> docker build -t com.tsys/fraud-checker-service .

# To run this image use
# $> docker run -p 9001:9001 com.tsys/fraud-checker-service