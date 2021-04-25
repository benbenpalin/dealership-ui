FROM openjdk:8-alpine

COPY target/uberjar/dealership-ui.jar /dealership-ui/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/dealership-ui/app.jar"]
