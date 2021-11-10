
# ModelSet

ModelSet is a dataset of software models. This repository contains the databases with the labels and the scripts to generate the associate meta-data to build the final package.

You can find more information about ModelSet in the following open access paper: https://link.springer.com/article/10.1007%2Fs10270-021-00929-3.

## Usage

* Download the latest release from https://github.com/modelset/modelset-dataset/releases
* Unzip the package some location in your workspace
* The structure of the package is the following.

```
+ datasets
  + dataset.ecore/data/ecore.db
  + dataset.genmymodel/data/genmymodel.db
+ raw-data
+ txt
```

* The `.db` files are SQLite databases containing the information about the models. The `raw-data` folder contains the models serialized in XMI and the `txt` folder the strings of the models (e.g., to train simple NLP models).
* Use standard JDBC to query the models (in Java):

```
Connection dataset = DriverManager.getConnection("jdbc:sqlite:/path/to/dbfile");
PreparedStatement stm = dataset.prepareStatement("select mo.id, mo.filename, mm.metadata from models mo join metadata mm on mo.id = mm.id");
stm.execute();

ResultSet rs = stm.getResultSet();
while (rs.next()) {
	String id = rs.getString(1);
	String filename = rs.getString(2);
	String metadata = rs.getString(3);
	System.out.println(id + ": " + metadata);
}			

```

## Python library

To use ModelSet in a typical Python/Jupyter setting, you can use the ModelSet python library:

* Checkout: https://github.com/modelset/modelset-py

## Examples

You can find some examples in https://github.com/modelset/modelset-apps

## Developer information

* The raw data is not directly stored in this repository, because this is already downloaded from public repositories and we don't want to pollute GitHub with more copies.
* Use `./bin/download-data.sh` to get a copy of the raw data.
* The files are automatically analysed using MAR analysers, using `ComputeEcoreStats`.
  * This requires installing MAR from sources
  * Download MAR source code
  * mvn install