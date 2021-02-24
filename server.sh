#!/bin/sh

export PORTAL_ORIGIN=http://localhost:8081
export DATA_REPOSITORY_ORIGIN=http://localhost:4000
export EXPERIMENTAL=false
export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Ddesigner.bonita.portal.url=$PORTAL_ORIGIN -Ddesigner.bonita.bdm.url=$DATA_REPOSITORY_ORIGIN -Ddesigner.experimental=$EXPERIMENTAL"
# CURL is needed

url=http://localhost:8080/bonita/actuator/health
waitBackendPart() {
  while [ $(curl -sL $url -w "%{http_code}\n"  -o /dev/nul) -ne "200" ]
  do
    sleep 1
  done
  echo "Backend webapp started"
}

echo Using $PORTAL_ORIGIN as portal origin.
echo Using $DATA_REPOSITORY_ORIGIN as data repository origin.
echo You can edit this in server.sh
yarnCrossPlatform="yarn"
if [ "$OSTYPE" = "msys" ];then
   yarnCrossPlatform="yarn.cmd"
fi

(cd backend/webapp/ && $yarnCrossPlatform start &)
waitBackendPart
(cd frontend/ && $yarnCrossPlatform start)
