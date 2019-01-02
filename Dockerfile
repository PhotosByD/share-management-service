FROM openjdk:8-jre-slim

RUN mkdir /app

WORKDIR /app

ADD ./api/target/share-api-1.0.0-SNAPSHOT.jar /app

EXPOSE 8084

CMD java -jar share-api-1.0.0-SNAPSHOT.jar