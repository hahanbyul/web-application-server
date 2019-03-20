FROM maven:3-jdk-13

WORKDIR /app
ADD pom.xml /app
RUN mvn clean package
ADD . /app
