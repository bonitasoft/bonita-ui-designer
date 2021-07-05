#!/bin/bash
usage() {
    command=$(basename $0)
    echo ""
    echo -e "\e[1mSYNOPSIS\e[0m"
    echo -e "  \e[4m$command\e[0m --crowdin-api-key=<key> --github-api-key=<key> [--branch=<branch name>] [--crowdin-project=<project>]"
    echo -e "  \e[4m$command\e[0m --help"
    echo ""
    echo -e "\e[1mDESCRIPTION\e[0m"
    echo "  Downloads translation from crowdin, integrate them to UI Designer source code and create a pull request with changes"
    echo ""
    echo -e "\e[1mOPTIONS\e[0m"
    echo "  --crowdin-api-key   the crowdin api key of crowdin project (mandatory)"
    echo "  --crowdin-project   the targeted crowdin project (default: bonita-bpm)"
    echo "  --github-api-key    the github api key of currently logged in user (mandatory)"
    echo "  --branch       the branch on which download translation keys (default: current branch)"
    echo "  --help              display this help"
    echo ""
}

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
BUILD_DIR=$SCRIPT_DIR/build
BASE_DIR=$SCRIPT_DIR/../..

CROWDIN_PROJECT="bonita"
BRANCH_NAME=$(git rev-parse --abbrev-ref HEAD)
for i in "$@"; do
    case $i in
        --crowdin-api-key=*)
        CROWDINKEY="${i#*=}"
        shift
        ;;
        --github-api-key=*)
        GITHUBKEY="${i#*=}"
        shift
        ;;
        --branch=*)
        BRANCH_NAME="${i#*=}"
        shift
        ;;
        --crowdin-project=*)
        CROWDIN_PROJECT="${i#*=}"
        shift
        ;;
         --help)
        usage
        exit 0
        ;;
    esac
done
if [ -z "$CROWDINKEY" ]; then
  echo "ERROR crowdin API key is needed";
  usage;
  exit 1
fi
if [ -z "$GITHUBKEY" ]; then
  echo "ERROR github API key is needed";
  usage;
  exit 1
fi

# $1 github API Key
pull_request() {
  PR="{\"title\": \"feat(l10n): [${BRANCH_NAME}] Translations update\", \"head\": \"feat/${BRANCH_NAME}/update-translations\", \"base\": \"${BRANCH_NAME}\"}"
  echo "Create new pull request $PR"
  curl -i -X POST -d "$PR" \
     https://api.github.com/repos/bonitasoft/bonita-ui-designer-internal/pulls?access_token=$1
}

# $1 directory
remove_comments() {
    find $1 -type f -exec sed -i '/^#/d' {} \;
}

echo "***********************************************************************************"
echo "UI DESIGNER TRANSLATION DOWNLOAD"
echo "***********************************************************************************"

echo "Preparing environment..."
if [ -d  $BUILD_DIR ]
then
  rm -rf $BUILD_DIR
fi
mkdir -p $BUILD_DIR

echo "Downloading translations..."
wget -O $BUILD_DIR/all.zip https://api.crowdin.com/api/project/$CROWDIN_PROJECT/download/all.zip?key=$CROWDINKEY
unzip $BUILD_DIR/all.zip -d $BUILD_DIR

git checkout -B feat/$BRANCH_NAME/update-translations

cp $BUILD_DIR/$BRANCH_NAME/ui-designer/community/*.po $BASE_DIR/backend/artifact-builder/src/main/resources/i18n
remove_comments $BASE_DIR/backend/artifact-builder/src/main/resources/i18n

# Count modified lines except those containing "PO-Revision-Date"
# When only "PO-Revision-Date" has been changed in each files, we do not create PR since no translations has been updated
modifiedlines=$(git diff --word-diff --unified=0 | grep -Ev "^diff --git|^index|^\+\+\+|^---|^@@|PO-Revision-Date" | wc -l)
if [ $modifiedlines -gt 0 ]
then
    git commit -a -m "feat(l10n): update translations"
    git push origin feat/$BRANCH_NAME/update-translations --force

    pull_request $GITHUBKEY
else
    echo "No changes. Translation update PR not created"
fi
