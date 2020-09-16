FROM java:openjdk-8-jre-alpine
ARG version
ADD app/target/app-$version.jar app.jar
RUN echo "Asia/shanghai" > /etc/timezone
ENTRYPOINT ["java","-jar","/app.jar"]