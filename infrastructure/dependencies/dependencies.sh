#!/bin/bash
set -euo pipefail

usage() {
    launch_command=`basename ${0}`
    echo "==========================================================================================================="
    echo "This script create a new file with dependencies list"
    echo ""
    echo "USAGE"
    echo "- ${launch_command} --version=<version> ---source-folder=<-source-folder> \\"
    echo "    [--file-name=<file-name>]"
    echo "- ${launch_command} --help"
    echo ""
    echo "MANDATORY ARGUMENTS"
    echo "    --version                 The version where generate the dependencies report"
    echo "    --source-folder           The folder with dependencies files to parse"
    echo ""
    echo "OPTIONAL ARGUMENTS"
    echo "    --file-name               The name of generated file (default: ui-designer-dependencies.md)"
    echo "    --help                    Display this help"
    echo "EXAMPLE"
    echo "./dependencies.sh --version=7.10 --source-folder=../../zipArchives"
    echo "==========================================================================================================="
    exit 1
}

############################################ main code #####################################"""
for i in "$@"; do
    case $i in
        --version=*)
            VERSION="${i#*=}"
        shift
        ;;
        --file-name=*)
            FILENAME="${i#*=}"
        shift
        ;;
        --source-folder=*)
            SOURCE_FOLDER="${i#*=}"
        shift
        ;;
        --help)
            usage
            exit 1
        ;;
    esac
done

FILENAME=${FILENAME:=ui-designer-dependencies.md}
SCRIPT_DIR=$(dirname "$0")
BASEDIR=$(dirname $(readlink -f "$0"))/../..

if [ -z "${VERSION}" ]; then echo "ERROR version is needed"; usage; fi
if [ -z "${SOURCE_FOLDER}" ]; then echo "ERROR source-folder is needed"; usage; fi

# Create file if not exist
if [ ! -f ${FILENAME} ]; then
    touch ${FILENAME}
fi

FOLDER=${BASEDIR}/${SOURCE_FOLDER}

node ${SCRIPT_DIR}/node_modules/@bonitasoft/dependency-list-to-markdown/src/generateMarkdownContent.js --folder=${FOLDER} --outputFile ${FILENAME} --header="Bonita-ui-designer dependencies ${VERSION}" --description="List all dependencies uses for Bonita UI-Designer"
