#!/bin/bash

usage() {
    echo "*************************************************************************************************************"
    echo "usage: "
    echo "  $0 new_version"
    echo "*************************************************************************************************************"
}

# $1 new version
# $2 file
replace_first_version() {
  sed -i "0,/<version>.*<\/version>/s//<version>${1}<\/version>/" $2
}

# $1 pom file
get_current_version() {
    grep -Po -m1 '(?<=<version>).*(?=</version>)' $1
}

if [ $# -lt 1 ]
then
    usage
    exit 1
fi

BASEDIR=$(dirname $(readlink -f "$0"))/..
cd $BASEDIR

CUR_VERSION=$(get_current_version pom.xml)
NEXT_VERSION=$1
NEXT_VERSION_CUT=$(echo $1| cut -f1 -d'_')

echo "Changing version from $CUR_VERSION to $NEXT_VERSION"
# replace first <version> value in poms
find . -name "pom.xml" | while read pom; do replace_first_version $NEXT_VERSION "$pom"; done

# replace version in files that need it (Dockerfile, README, widgets/page/fragments models used for tests)
find . -name "package.json" | xargs sed -i "s/${CUR_VERSION}/${NEXT_VERSION}/g"
find . -name "Dockerfile" | xargs sed -i "s/${CUR_VERSION}/${NEXT_VERSION}/g"
find . -name "README.md" | xargs sed -i "s/${CUR_VERSION}/${NEXT_VERSION}/g"
find -regex ".*/e2e/config/.*.js" | xargs sed -i "s/${CUR_VERSION}/${NEXT_VERSION_CUT}/g"
find -regex '/frontend/app/index.html' | xargs sed -i "s/meta name=\"version\" content=\"${CUR_VERSION}/meta name=\"version\" content=\"${NEXT_VERSION}/g"
find tests/src/test/resources -name "*.json" | xargs sed -i "s/${CUR_VERSION}/${NEXT_VERSION}/g"
