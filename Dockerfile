### Multi-stage Dockerfile
### Stage 1: build the WAR using Maven
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy Maven files first (for caching)
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
RUN mvn -B dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -B -DskipTests package

### Stage 2: runtime image with Tomcat
FROM tomcat:10.1-jdk21

ENV CATALINA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/urandom"

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /workspace/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
