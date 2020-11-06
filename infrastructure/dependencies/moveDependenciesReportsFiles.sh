#!/bin/bash
set -euo pipefail

mkdir -p bonita-ui-designer-dependencies
cd bonita-ui-designer-dependencies

echo "Copy all dependencies reports into 'bonita-ui-designer-dependencies'"

cp -R ../backend/contract/target/site ./bonita-ui-designer-backend-contract-maven-dependencies

cp -R ../backend/webapp/target/site/ ./bonita-ui-designer-backend-webapp-maven-dependencies

cp ../backend/webapp/target/ui-designer-backend-webapp.json .

cp ../frontend/ui-designer-frontend-bower-dependencies.json .
cp ../frontend/ui-designer-frontend-yarn-dependencies.json .

echo "Copy done"
ls -lRh .
