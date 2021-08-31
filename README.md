# Fraud Checker Service

### Pre-requisite
- Ensure to have Java 11 on your machine

- Before provisioning any code with Jenkinsfile, update all your files to prevent name-space conflict, 
since all resources will be provisioned in a shared AWS account
```bash
$ ./updateUsername.sh
```

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