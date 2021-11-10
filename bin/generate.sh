#!/bin/bash

echo "Generating additional artifacts"

pushd .
cd java-lib/modelset-lib
# mvn package
mvn compile
mvn test exec:java -Dexec.mainClass=modelset.process.ComputeEcoreStats
mvn test exec:java -Dexec.mainClass=modelset.process.ComputeTxt
popd
