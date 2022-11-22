
# ModelSet

ModelSet is a **labelled dataset of software models**. 

This repository contains:

1. The ModelSet databases with the labelled datasets. See the [Downloading ModelSet](#downloading-modelset) section for more information.
2. The scripts to create the databases and generate the release. See the [Building ModelSet](#building-modelset) section for more information.

You can find more information about ModelSet in the following Open Access paper: https://link.springer.com/article/10.1007%2Fs10270-021-00929-3.

## Downloading ModelSet

To download ModelSet follow these steps:

1. You can download the latest release from https://github.com/modelset/modelset-dataset/releases
2. Unzip the package in some location of your workspace
3. The structure of the decompressed package is the following:

    ```
    + datasets
      + dataset.ecore/data/ecore.db
      + dataset.genmymodel/data/genmymodel.db
    + graph
    + raw-data
    + txt
    ```
    3.1. The `datasets` folder contains the databases with the labelled models. The `.db` files are SQLite databases containing the information about the models. 

    The database schema includes a table called model with model data (i.e., unique identifier, source repository and filename) and a table called metadata with label data (i.e., unique identifier and a JSON object with the label information). The following figure illustrates the database schema:

    ```
    +-------------------+     +---------------------------------+
    |       model       |     |             metadata            |
    +-------------------+     +---------------------------------+
    | id : VARCHAR {PK} |     | id : VARCHAR {PK, FK(model.id)} |
    | source : VARCHAR  |     | json : TEXT                     |
    | filename : TEXT   |     +---------------------------------+
    +-------------------+
    ```

    3.2. The `graph` folder contains the graph representation of the models. 

    3.3. The `raw-data` folder contains the models serialized in XMI. 

    3.4. The `txt` folder includes the strings of the models (e.g., to train simple NLP models).

### Querying ModelSet via Java JDBC

Once you have downloaded ModelSet, you can use JDBC to query the databases. 

For instance, the following code illustrates how to query the database using JDBC:

```java
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

### Querying ModelSet in Python

To use ModelSet in a typical Python/Jupyter setting, we recommend you to use the [modelset-py](https://github.com/modelset/modelset-py) Python library we have developed. Visit the corresponding [repository](https://github.com/modelset/modelset-py) for more information.

### Examples

We provide some examples of how to use ModelSet in the [examples](https://github.com/modelset/modelset-apps) repository.

## Building ModelSet

> **Note**: these steps are only required if you want to create a new release of ModelSet. If you just want to use ModelSet, you can download the latest release (see [Downloading ModelSet](#downloading-modelset) section in this file)

To create the ModelSet release, you have to follow these steps:

1. Execute `./bin/download-data.sh` to recover the model files which will be stored in the `raw-data` folder. These files are not stored here as they have already published in existing GitHub repositories. 
2. Execute `./bin/generate.sh` to generate additional artifacts.

    2.1. This requires installing MAR from sources. You can find more information [here](https://github.com/mar-platform/mar)

    2.2. Download MAR source code and follow the [instructions](https://github.com/mar-platform/mar/wiki/Installation-instructions)

    2.3. Execute `mvn install`
3. Execute `./bin/build.sh` to build the ModelSet release package.
4. The ModelSet release package will have the name `modelset.zip`.

  
## Citation

If you find this dataset useful, please consider citing its associated paper: https://link.springer.com/article/10.1007/s10270-021-00929-3

```
@article{lopez2021modelset,
  title   = {{ModelSet: a dataset for machine learning in model-driven engineering}},
  author  = {L{\'o}pez, Jos{\'e} Antonio Hern{\'a}ndez and 
            C{\'a}novas Izquierdo, Javier Luis and 
            Cuadrado, Jes{\'u}s S{\'a}nchez},
  journal = {Softw. Syst. Model.},
  volume  = {21},
  number  = {3},
  pages   = {967--986},
  year    = {2022},
  url     = {https://doi.org/10.1007/s10270-021-00929-3},
}
```

## Contributing

We welcome contributions of all kinds, including extensions to the dataset, new empirical studies, and new features. If you want to contribute to ModelSet, please review our [contribution guidelines](CONTRIBUTING.md) and our [governance model](GOVERNANCE.md).

Note that we have a [code of conduct](CODE_OF_CONDUCT.md) that we expect project participants to adhere to. Please read it before contributing.

## License

This dataset is licensed under the [GNU Lesser General Public License v3.0](LICENSE.md).
