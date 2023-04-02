FROM tomcat:10.1.7-jre17-temurin-jammy

COPY backend-0.0.1-SNAPSHOT.jar /www.jar

ENTRYPOINT ["java","-jar","/www.jar"]

EXPOSE 8080