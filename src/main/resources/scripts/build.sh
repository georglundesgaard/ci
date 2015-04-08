#!/bin/bash

set -e
cp -a %s %s
cd %s
git pull
commit_id=`git rev-parse --short HEAD`
if [ "$commit_id" != "%s" ]; then
  echo "Changes detected. Executing build..."
  %s
  cd ..
  echo "last-commit-id=$commit_id" > %s
  echo "Build completed."
else
  echo "No changes. Nothing to do."
fi
