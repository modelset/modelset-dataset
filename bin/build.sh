#!/bin/bash

echo "Building the ModelSet package"

GENERATE=$1

if [[ $GENERATE = "regenerate" ]]
  then
      ./bin/generate.sh      
fi


mkdir /tmp/modelset
echo "Copying..."

mkdir -p /tmp/modelset/datasets/dataset.ecore/data
mkdir -p /tmp/modelset/datasets/dataset.ecore/src/dataset/ecore/
mkdir -p /tmp/modelset/datasets/dataset.genmymodel/data
mkdir -p /tmp/modelset/datasets/dataset.genmymodel/src/dataset/genmymodel/


cp datasets/dataset.ecore/.project /tmp/modelset/datasets/dataset.ecore
cp datasets/dataset.ecore/build.properties /tmp/modelset/datasets/dataset.ecore
# cp datasets/dataset.ecore/.settings /tmp/modelset/datasets/dataset.ecore -r
cp datasets/dataset.ecore/META-INF /tmp/modelset/datasets/dataset.ecore -r
cp datasets/dataset.ecore/data/ecore.db /tmp/modelset/datasets/dataset.ecore/data/ecore.db
cp datasets/dataset.ecore/data/analysis.db /tmp/modelset/datasets/dataset.ecore/data/analysis.db
cp datasets/dataset.ecore/src/dataset/ecore/ValidateMetadata.java /tmp/modelset/datasets/dataset.ecore/src/dataset/ecore/

cp datasets/dataset.genmymodel/.project /tmp/modelset/datasets/dataset.genmymodel
cp datasets/dataset.genmymodel/build.properties /tmp/modelset/datasets/dataset.genmymodel
cp datasets/dataset.genmymodel/data/genmymodel.db /tmp/modelset/datasets/dataset.genmymodel/data/genmymodel.db
# cp dataset.genmymodel/data/analysis.db /tmp/modelset/datasets/dataset.genmymodel/data/analysis.db
#cp datasets/dataset.genmymodel/.settings /tmp/modelset/datasets/dataset.genmymodel -r
cp datasets/dataset.genmymodel/META-INF /tmp/modelset/datasets/dataset.genmymodel -r
cp datasets/dataset.genmymodel/src/dataset/genmymodel/ValidateMetadata.java  /tmp/modelset/datasets/dataset.genmymodel/src/dataset/genmymodel/ValidateMetadata.java 

cp -r README.md txt/ graph/ /tmp/modelset
./bin/copy-files.py .
echo "Zipping..."
cd /tmp
zip -r modelset.zip modelset
