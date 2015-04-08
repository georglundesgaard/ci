#!/bin/bash

set -e
cp -a %s %s
cd %s
git pull
commit_id=`git rev-parse --short HEAD`
if [ "$commit_id" != "%s" ]; then
  %s
  cd ..
  echo "last-commit-id=$commit_id" > %s
else
  echo "No changes. Nothing to do."
fi
