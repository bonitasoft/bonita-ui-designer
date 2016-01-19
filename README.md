# Bonita UI designer

## Build
You can build entire project using maven.
    
    mvn clean package
    
This will build frontend and backend and create two artifacts :
backend/target/ui-designer-1.3.0-SNAPSHOT.war
backend/target/ui-designer-1.3.0-SNAPSHOT-standalone.jar

## Test
while running `mvn clean package` only unit tests are run. 
If you want to run the integration tests, run the following command

    mvn clean install -Pintegration-test
    
Also frontend e2e test could be launched via

    mvn clean install -Pe2e

## Run
To run build standalone jar, just launch it as a standard jar :

    java -jar backend/target/ui-designer-1.3.0-SNAPSHOT-standalone.jar [-Dworkspace=/path/to/workspace] [-Drepository.widgets=/path/widgets/repository] [-Drepository.pages=/path/pages/repository]

Application is now available at http://localhost:8080/designer/

You can specify workspace location where pages and widgets are stored. Default value is {user.home}/.bonita 

Available options can be listed by running 

    java -jar backend/target/ui-designer-1.3.0-SNAPSHOT-standalone.jar -h
    
Other available options are listed here : http://tomcat.apache.org/maven-plugin-2.0/executable-war-jar.html 
    
## Develop
Backend and frontend side could be launched in dev mode by using _server.sh_ script. 
It launch _gulp serve_ backend task, waits for jetty to be running then launches _gulp serve_ frontend task.

```shell
$ ./server.sh
```

You can access the application at http://localhost:3000/index-dev.html, backend side is accessible at http://localhost:8080/

## Docker
You can run the ui-designer locally using Docker (of course, you need to install Docker to do so).
The Dockerfile is a really simple one, using a base image with java7, adding the build standalone jar and starting it.
Before building docker image, you need to build the project.

To build the image :

    docker build -t bonita/ui-designer .

To run the image built with the previous command :

    docker run -p 8080 bonita/ui-designer

This will start the builder on a random port on your docker host (*either the local host if you're running linux, or the boot2docker VM if you're on MacOS*). For boot2docker, you can find your VM IP with :

    boot2docker ip

You can find the random port used with :

    docker ps

For example, the application can start on [http://192.168.59.103:49153/designer](http://192.168.59.103:49153/designer)
(*192.168.59.103 being the default boot2docker IP and 49153 the random port used*).

You can also run the image on a fixed port, 8000 for example, with :

    docker run -p 8000:8080 bonita/ui-designer
    
