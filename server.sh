#!/bin/sh

# CURL is needed

url=http://localhost:8080/rest/pages
waitJetty() {
  while [ $(curl -sL $url -w "%{http_code}\n"  -o /dev/nul) -ne "200" ]
  do
    sleep 1
  done
  echo "Jetty started"
}

gulp serve --gulpfile backend/webapp/gulpfile.js &
waitJetty
gulp serve --gulpfile frontend/gulpfile.js

