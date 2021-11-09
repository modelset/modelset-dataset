
# ModelSet

ModelSet is a dataset of software models. This repository contains the databases with the labels and the scripts to generate the associate meta-data to build the final package.

You can find more information about ModelSet in the following open access paper: https://link.springer.com/article/10.1007%2Fs10270-021-00929-3.

## Developer information

* The raw data is not directly stored in this repository, because this is already downloaded from public repositories and we don't want to pollute GitHub with more copies.
* Use `./bin/download-data.sh` to get a copy of the raw data.
* The files are automatically analysed using MAR analysers, using `ComputeEcoreStats`.
  * This requires installing MAR from sources
  * Download MAR source code
  * mvn install