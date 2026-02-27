FROM eclipse-temurin:21-jdk-jammy
VOLUME /tmp
COPY build/libs/user-service-*.jar UserService.jar
ENTRYPOINT ["java", "-jar", "UserService.jar"]