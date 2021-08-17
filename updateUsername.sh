#!/usr/bin/env sh

# Perform string substitution for "REPLACE_USERNAME" with actual username in all files
sed  -i '' "s/REPLACE-USERNAME/$(whoami)/g" *.md Jenkinsfile **/*.*