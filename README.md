
# ModelSet

ModelSet is a **dataset of software models**. This repository contains the databases with the labels and the scripts to generate the associate meta-data to build the final package.

You can find more information about ModelSet in the following Open Access paper: https://link.springer.com/article/10.1007%2Fs10270-021-00929-3.

## Usage

* Download the latest release from https://github.com/modelset/modelset-dataset/releases
* Unzip the package in some location of your workspace
* The structure of the package is the following:

  ```
  + datasets
    + dataset.ecore/data/ecore.db
    + dataset.genmymodel/data/genmymodel.db
  + raw-data
  + txt
  ```

* The `.db` files are SQLite databases containing the information about the models. The `raw-data` folder contains the models serialized in XMI. The `txt` folder includes the strings of the models (e.g., to train simple NLP models).

## JDBC

You can use standard JDBC to query the models, for instance, in Java:

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

## Python Library

To use ModelSet in a typical Python/Jupyter setting, you can use the ModelSet Python library:

* Checkout: https://github.com/modelset/modelset-py

## Examples

You can find some examples in https://github.com/modelset/modelset-apps

## Developer Information

* The raw data is not directly stored here because it already collects information from existing public repositories, and we don't want to pollute GitHub with duplicates.
* Use `./bin/download-data.sh` to get a copy of the raw data.
* The files are automatically analysed using MAR analysers using `ComputeEcoreStats`.
  * This requires installing MAR from sources. You can find more information [here](https://github.com/mar-platform/mar)
  * Download MAR source code and follow the [instructions](https://github.com/mar-platform/mar/wiki/Installation-instructions)
  * Execute `mvn install`
  
## Citation

If you find this dataset useful, please consider citing its associated paper: https://link.springer.com/article/10.1007/s10270-021-00929-3

```
@article{lopez2021modelset,
  title={ModelSet: a dataset for machine learning in model-driven engineering},
  author={L{\'o}pez, Jos{\'e} Antonio Hern{\'a}ndez and C{\'a}novas Izquierdo, Javier Luis and Cuadrado, Jes{\'u}s S{\'a}nchez},
  journal={Software and Systems Modeling},
  pages={1--20},
  year={2021},
  publisher={Springer}
}
```

## Contributing

We welcome contributions of all kinds, including extensions to the dataset, new empirical studies, and new features. If you want to contribute to ModelSet, please review our [contribution guidelines](CONTRIBUTING.md) and our [governance model](GOVERNANCE.md).

Note that we have a [code of conduct](CODE_OF_CONDUCT.md) that we expect project participants to adhere to. Please read it before contributing.

## License

This dataset is licensed under the [GNU Lesser General Public License v3.0](LICENSE.md).
