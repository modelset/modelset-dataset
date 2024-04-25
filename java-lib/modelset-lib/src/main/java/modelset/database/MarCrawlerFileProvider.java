package modelset.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import mar.ingestion.CrawlerDB;
import mar.ingestion.IngestedModel;
import mar.validation.IFileProvider;

public class MarCrawlerFileProvider implements IFileProvider {

	private List<PlainFileInfo> files;

	public MarCrawlerFileProvider(File db, File repoRoot, String type) throws SQLException {
		CrawlerDB crawlerDb = new CrawlerDB(type, "github", repoRoot.getAbsolutePath(), db); 
		this.files = new ArrayList<PlainFileInfo>();
		for (IngestedModel m : crawlerDb.getModels()) {
			files.add(new PlainFileInfo(m.getModelId(), m.getRelativePath(), repoRoot));			
		}
	}

	@Override
	public @Nonnull List<? extends PlainFileInfo> getLocalFiles() {
		return files;
	}

	public static boolean isCrawlerDb(File f) throws SQLException {
		try(Connection connection = DriverManager.getConnection(CrawlerDB.getConnectionString(f))) {
			DatabaseMetaData dmd = connection.getMetaData(); 
			ResultSet rs = dmd.getTables(null, null, "data", new String[] { "TABLE" });
			ResultSet rs2 = dmd.getTables(null, null, "repo_info", new String[] { "TABLE" });		
			return rs.next() && rs2.next();
		}
	}
}
