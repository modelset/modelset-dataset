package modelset.process.util;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import mar.indexer.common.configuration.ModelLoader;
import mar.validation.IFileProvider;
import modelset.database.MarAnalysisFileProvider;
import modelset.database.MarCrawlerFileProvider;
import modelset.database.ModelSetFileProvider;

public class Utils {

	public static ModelLoader getLoader(String type) {
		if (type.equals("ecore"))
			return ModelLoader.DEFAULT;
		else if (type.equals("uml"))
			return ModelLoader.UML;
		throw new IllegalArgumentException("Unknown type");
	}

	public static IFileProvider loadDatabase(File db, File repoRoot, String type) {
		try {
			if (ModelSetFileProvider.isModelSetDb(db)) {
				return new ModelSetFileProvider(db, repoRoot);
			} else if (MarCrawlerFileProvider.isCrawlerDb(db)) {
				return new MarCrawlerFileProvider(db, repoRoot, type);
			} else if (MarAnalysisFileProvider.isMarDb(db)) {
				return new MarAnalysisFileProvider(db, repoRoot, type);
			}
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);	
		}
		throw new IllegalArgumentException("Invalid database");
	}
}
