FROM openjdk:17
COPY target/*.jar /app.jar
CMD ["--server.port=8881"]
EXPOSE 8881
ENTRYPOINT ["java", "-jar", "/app.jar"]