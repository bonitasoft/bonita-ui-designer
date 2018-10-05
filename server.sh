#!/bin/sh

export PORTAL_ORIGIN=http://localhost:8081
export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Dbonita.portal.origin=$PORTAL_ORIGIN"

# CURL is needed

url=http://localhost:8080/rest/pages
waitJetty() {
  while [ $(curl -sL $url -w "%{http_code}\n"  -o /dev/nul) -ne "200" ]
  do
    sleep 1
  done
  echo "Jetty started"
}

echo Using $bonita.portal.origin as portal origin. You can edit it in community/server.sh
yarnCrossPlatform="yarn"
if [ "$OSTYPE" = "msys" ];then
   yarnCrossPlatform="yarn.cmd"
fi

(cd backend/webapp/ && $yarnCrossPlatform start &)
waitJetty
(cd frontend/ && $yarnCrossPlatform start)
