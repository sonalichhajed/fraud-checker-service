# Fraud Checker Service

### Pre-requisite
- Ensure to have Java 11 on your machine

### Build the application
```bash
$ ./gradlew clean build 
```

### Run the application
```bash
$ ./gradlew bootRun
```

### Build the docker image
```bash
$ docker build -t fraud-checker-service:latest .
```

### Run the docker image
```bash
$ docker run -d -p 9001:9001 fraud-checker-service:latest
```