#!/bin/sh

export PORTAL_ORIGIN=http://localhost:8081
export DATA_REPOSITORY_ORIGIN=http://localhost:4000
export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Dbonita.portal.origin=$PORTAL_ORIGIN -Dbonita.data.repository.origin=$DATA_REPOSITORY_ORIGIN"

# CURL is needed

url=http://localhost:8080/rest/pages
waitJetty() {
  while [ $(curl -sL $url -w "%{http_code}\n"  -o /dev/nul) -ne "200" ]
  do
    sleep 1
  done
  echo "Jetty started"
}

echo Using $PORTAL_ORIGIN as portal origin.
echo Using $DATA_REPOSITORY_ORIGIN as data repository origin.
echo You can edit this in community/server.sh
yarnCrossPlatform="yarn"
if [ "$OSTYPE" = "msys" ];then
   yarnCrossPlatform="yarn.cmd"
fi

(cd backend/webapp/ && $yarnCrossPlatform start &)
waitJetty
(cd frontend/ && $yarnCrossPlatform start)
