# Imagem a partir do jar já buildado.
# O build não acontece dentro do Docker porque o service depende da interface pma:account,
# que só existe no repositório Maven local (instalada com ./mvnw install no pma-26-account).
#
#   ./mvnw clean package
#   docker build -t account .
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/account-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
