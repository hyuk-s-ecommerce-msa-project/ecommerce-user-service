FROM eclipse-temurin:21-jdk-jammy
VOLUME /tmp
COPY build/libs/user-service-1.1.6.jar UserService.jar
ENTRYPOINT ["java", "-jar", "UserService.jar"]