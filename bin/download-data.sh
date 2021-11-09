#! /bin/sh

mkdir raw-data

cd raw-data
wget http://sanchezcuadrado.es/files/modelset/repo-ecore-all.tar.gz
wget http://sanchezcuadrado.es/files/modelset/repo-genmymodel-uml.tar.gz

tar xzf repo-ecore-all.tar.gz
tar xzf repo-genmymodel-uml.tar.gz
