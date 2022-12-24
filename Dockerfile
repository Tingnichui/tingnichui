FROM openjdk:8
ADD target/tingnichui-1.0.0.jar app.jar
EXPOSE 1128
ENTRYPOINT java -jar /app.jar