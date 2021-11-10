#!/bin/bash

echo "Building the ModelSet package"

pushd .
cd java-lib/modelset-lib
# mvn package
mvn compile
mvn test exec:java -Dexec.mainClass=modelset.process.ComputeEcoreStats
mvn test exec:java -Dexec.mainClass=modelset.process.ComputeTxt
popd

mkdir /tmp/modelset
echo "Copying..."
cp -r README.md datasets/ txt/ /tmp/modelset
./bin/copy-files.py
echo "Zipping..."
zip -r modelset.zip /tmp/modelset
