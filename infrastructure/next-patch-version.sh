#!/usr/bin/env bash

SCRIPTDIR=$(cd $(dirname $0) && pwd)
BASEDIR=$SCRIPTDIR/..

cd $BASEDIR

get_current_version() {
  # TODO: could be replaced  (safer) with curr_version=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)
    grep -Po -m1 '(?<=<version>).*(?=</version>)' pom.xml
}

get_next_patch_version() {
    curr_version=$(get_current_version)
    git fetch --prune origin +refs/tags/*:refs/tags/* >& /dev/null
    minor_version=$(echo $curr_version | grep -P '\d{1,}\.\d{1,}' -o)
    latest_tag=$(git tag -l ${minor_version}.* | sort --version-sort | tail -1)
    if [ -z "$latest_tag" ]; then
        # No existing tag on this minor line — start from the pom version (strip -SNAPSHOT)
        echo "${curr_version%-SNAPSHOT}"
    else
        # increment last version number (patch)
        echo "${latest_tag%.*}.$((${latest_tag##*.}+1))"
    fi
}

echo $(get_next_patch_version)
