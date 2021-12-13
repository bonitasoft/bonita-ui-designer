#!/bin/sh

export PORTAL_ORIGIN=http://localhost:8081
export DATA_REPOSITORY_ORIGIN=http://localhost:4000
export EXPERIMENTAL=false

echo "Using $PORTAL_ORIGIN as portal origin."
echo "Using $DATA_REPOSITORY_ORIGIN as data repository origin."

yarnCrossPlatform="yarn"
if [ "$OSTYPE" = "msys" ];then
   yarnCrossPlatform="yarn.cmd"
fi

(cd backend/webapp/ && \
  mvn spring-boot:start \
    -Dspring-boot.run.arguments="--designer.bonita.portal.url=$PORTAL_ORIGIN --designer.bonita.bdm.url=$DATA_REPOSITORY_ORIGIN --designer.experimental=$EXPERIMENTAL" \
    -Dwait=500 #The number of milli-seconds to wait between each attempt to check if the spring application is ready.
)
(cd frontend/ && $yarnCrossPlatform start)

# use mvn spring-boot:stop to kill backend if required
