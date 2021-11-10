#!/bin/bash

echo "Building the ModelSet package"

GENERATE=$1

if [[ $GENERATE = "regenerate" ]]
  then
      ./bin/generate.sh      
fi


mkdir /tmp/modelset
echo "Copying..."
cp -r README.md datasets/ txt/ /tmp/modelset
./bin/copy-files.py
echo "Zipping..."
zip -r modelset.zip /tmp/modelset
