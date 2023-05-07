FROM azul/zulu-openjdk-alpine:17
RUN adduser app --disabled-password
USER app
CMD ["java","-jar", "/app/worldwidewalter.jar"]
EXPOSE 8080
COPY backend/target/backend-*.jar /app/worldwidewalter.jar