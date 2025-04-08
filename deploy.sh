#!/bin/bash

BRANCH="main"

# Process command line arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
    --dev)
      BRANCH="dev"
      shift
      ;;
    --branch)
      if [[ -n "$2" ]]; then
        BRANCH="$2"
        shift 2
      else
        echo "Error: --BRANCH requires a branch name"
        exit 1
      fi
      ;;
    *)
      shift
      ;;
  esac
done

git stash
git checkout $BRANCH
git pull
mvn spring-boot:run -Dmaven.test.skip=true
