package modelset.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;
import mar.validation.IFileInfo;
import mar.validation.IFileProvider;

public class MarAnalysisFileProvider implements IFileProvider {

	private List<PlainFileInfo> files;

	public MarAnalysisFileProvider(File db, File repoRoot, String type) throws SQLException, IOException {
		if (! db.exists())
			throw new IllegalArgumentException("File " + db + " does not exist");
			
		try (AnalysisDB analysis = new AnalysisDB(db)) {		
			this.files = new ArrayList<PlainFileInfo>();
			for (Model m : analysis.getValidModels(f -> f)) {
				files.add(new PlainFileInfo(m.getId(), m.getRelativePath().toString(), repoRoot));			
			}
		}
	}

	@Override
	public @Nonnull List<? extends IFileInfo> getLocalFiles() {
		return files;
	}

	public static boolean isMarDb(File f) throws SQLException {
		try(Connection connection = DriverManager.getConnection(AnalysisDB.getConnectionString(f))) {
			DatabaseMetaData dmd = connection.getMetaData(); 
			ResultSet rs = dmd.getTables(null, null, "models", new String[] { "TABLE" });
			ResultSet rs2 = dmd.getTables(null, null, "stats", new String[] { "TABLE" });		
			return rs.next() && rs2.next();
		}
	}
	
}
