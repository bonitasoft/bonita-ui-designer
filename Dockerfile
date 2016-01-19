# image to create a standalone form builder
# to build, use `docker build -t bonita/form-builder .`
# to run on random port, use `docker run -p 8080 bonita/form-builder`
# to run on a fixed port, 8000 for example, use `docker run -p 8000:8080 bonita/form-builder`

# base image with java 7 installed
FROM dockerfile/java:openjdk-7-jre

MAINTAINER cedric@ninja-squad.com

# expose page builder port
EXPOSE 8080

# run standalone ui designer on container start
CMD java -jar ui-designer-standalone.jar

# add build jar in the current directory of the image (/data)
ADD ./backend/webapp/target/ui-designer-1.3.0-SNAPSHOT-standalone.jar /data/ui-designer-standalone.jar



