# image to create a standalone ui-designer
# to build, use `docker build -t bonita/ui-designer .`
# to run on random port, use `docker run -p 8080 bonita/ui-designer`
# to run on a fixed port, 8000 for example, use `docker run -p 8000:8080 bonita/ui-designer`
FROM openjdk:8-jre-alpine
EXPOSE 8080
WORKDIR /data
ADD ./backend/webapp/target/ui-designer-1.11.61-standalone.jar /data/ui-designer-standalone.jar
CMD java -jar ui-designer-standalone.jar
