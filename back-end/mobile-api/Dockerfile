# Etapa 1: Build da aplicação com Maven e Java 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia as dependências e o código-fonte
COPY pom.xml .
COPY src ./src

# Compila o projeto gerando o arquivo .jar executável
RUN mvn clean package -DskipTests

# Etapa 2: Imagem final leve apenas com o Java 21 JRE para execução
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copia o .jar gerado na etapa de build
COPY --from=build /app/target/*.jar app.jar

# Porta padrão de escuta
EXPOSE 8080

# Inicia a aplicação Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
