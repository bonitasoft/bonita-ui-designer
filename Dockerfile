# image to create a standalone form builder
# to build, use `docker build -t bonita/form-builder .`
# to run on random port, use `docker run -p 8080 bonita/form-builder`
# to run on a fixed port, 8000 for example, use `docker run -p 8000:8080 bonita/form-builder`
FROM java:7-jre-alpine
EXPOSE 8080
WORKDIR /data
ADD ./backend/webapp/target/ui-designer-1.3.21-standalone.jar /data/ui-designer-standalone.jar
CMD java -jar ui-designer-standalone.jar
