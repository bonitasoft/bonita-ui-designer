#!/bin/bash

usage() {
  command=$(basename $0)
  echo ""
  echo -e "\e[1mSYNOPSIS\e[0m"
  echo -e "    $command --crowdin-api-key=\e[4mkey\e[0m [--branch=\e[4mbranch\e[0m] [--crowdin-project=\e[4mproject\e[0m] [--upload-translations]"
  echo ""
  echo -e "\e[1mDESCRIPTION\e[0m"
  echo "  Generate pot files containing translations keys for UI Designer"
  echo "  Upload them to crowdin in bonita-bpm crowdin project and specified branch"
  echo ""
  echo -e "\e[1mARGUMENTS\e[0m"
  echo ""
  echo -e "  --crowdin-api-key=\e[4mkey\e[0m    Crowdin api key of crowdin bonita-bpm project"
  echo ""
  echo -e "\e[1mOPTIONS\e[0m"
  echo -e "  --branch                 Crowdin branch on which we want to upload keys, default to \e[1mcurrent branch\e[0m"
  echo -e "  --upload-translations    Also upload translations to crowdin (keys + translated keys)"
  echo -e "  --crowdin-project        Crowdin project on which files will be uploaded (default: bonita)"
  echo ""
  exit 1;
}

# $1 base dir
# $2 output file
cat_pot() {
    cat `find $1 -name '*.pot' -print` | msguniq -s > $2
    check_errors $? "Error while concatenating pot files"
}

# $1 directory
npm_pot() {
    cd $1
    npm install && npm run pot
    check_errors $? "Error while generating pot files"
    cd -
}

# $1 previous command exit code
# $2 error message
check_errors() {
  if [ $1 -ne 0 ]
  then
    echo $2
    exit $1
  fi
}

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
BUILD_DIR=$SCRIPT_DIR/build
BASE_DIR=$SCRIPT_DIR/../..

PROJECT="bonita"
UPLOAD_TRANS=false
BRANCH_NAME=$(git rev-parse --abbrev-ref HEAD)
for i in "$@"; do
    case $i in
        --crowdin-api-key=*)
        CROWDINKEY="${i#*=}"
        shift
        ;;
        --upload-translations)
        UPLOAD_TRANS=true
        shift
        ;;
        --branch=*)
        BRANCH_NAME="${i#*=}"
        shift
        ;;
        --crowdin-project=*)
        PROJECT="${i#*=}"
        shift
        ;;
    esac
done
if [ -z "$CROWDINKEY" ]; then
  echo "ERROR crowdin API key is needed";
  usage;
fi

# $1 files
# $2 export_pattern
upload_sources() {
  # add file in case it does not exists yet
  curl --silent -o /dev/null -F "$1" -F "$2" -F "$3" https://api.crowdin.com/api/project/$PROJECT/add-file?key=$CROWDINKEY

  # update file
  curl -F "$1" -F "$2" -F "$3" https://api.crowdin.com/api/project/$PROJECT/update-file?key=$CROWDINKEY
}

# $1 directory path
add_crowdin_directory() {
  curl --silent -o /dev/null -F "name=$1" https://api.crowdin.com/api/project/$PROJECT/add-directory?key=$CROWDINKEY
}

# $1 crowdin target language
# $2 UI Designer language
upload_translations() {
  echo "Exporting $1 translation to $PROJECT crowdin project ..."

   curl -F "files[ui-designer/lang-template.pot]=@backend/webapp/src/main/resources/i18n/lang-template-$2.po" \
       -F "language=$1" \
       -F "auto_approve_imported=1" \
       -F "import_duplicates=1" \
       -F "import_eq_suggestions=1" \
       -F "branch=$BRANCH_NAME" \
	  https://api.crowdin.com/api/project/$PROJECT/upload-translation?key=$CROWDINKEY
}

echo "***********************************************************************************"
echo "UI DESIGNER TRANSLATION UPLOAD"
echo "***********************************************************************************"

echo "Preparing environment..."
if [ -d  $BUILD_DIR ]; then rm -rf $BUILD_DIR; fi
mkdir -p $BUILD_DIR


cd $BASE_DIR

echo "Building pot files..."
npm_pot frontend/
npm_pot backend/webapp/

echo "Concatenating pot files..."
cat_pot $BASE_DIR $BUILD_DIR/lang_template.pot

add_crowdin_directory "$BRANCH_NAME/ui-designer"
echo "Uploading pot to $PROJECT crowdin project ..."
upload_sources \
    "files[ui-designer/lang-template.pot]=@$BUILD_DIR/lang_template.pot" \
    "branch=$BRANCH_NAME" \
    "export_patterns[ui-designer/lang-template.pot]=/ui-designer/community/lang-template-%locale%.po"

if [ "$UPLOAD_TRANS" = true ]
then
  upload_translations fr fr-FR
  upload_translations es-ES es-ES
  upload_translations ja ja-JP
  upload_translations pt-BR pt-BR
fi
