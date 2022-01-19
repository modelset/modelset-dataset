package modelset.process;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import mar.validation.IFileInfo;
import mar.validation.IFileProvider;
import modelset.metadata.AnnotationsValidator;
import modelset.metadata.AnnotationsValidator.ParsedMetadata;
import modelset.metadata.AnnotationsValidator.SyntaxError;

public class ModelSetFileProvider implements IFileProvider {

	private List<ModelSetFileInfo> files;

	public ModelSetFileProvider(File db, File repoRoot) throws SQLException {
		Connection dataset = DriverManager.getConnection(getConnectionString(db));
		PreparedStatement stm = dataset.prepareStatement("select mo.id, mo.filename, mm.metadata from models mo join metadata mm on mo.id = mm.id");
		stm.execute();
		this.files = new ArrayList<ModelSetFileInfo>();
		ResultSet rs = stm.getResultSet();
		while (rs.next()) {
			String id = rs.getString(1);
			String filename = rs.getString(2);
			String metadata = rs.getString(3);
			try {
				ParsedMetadata m = AnnotationsValidator.INSTANCE.toMetadata(metadata);
				files.add(new ModelSetFileInfo(id, filename, repoRoot, m));
			} catch (SyntaxError e) {
				System.err.println("Error for " + id + ": [" + metadata + "]");
				e.printStackTrace();
			}
		}			
	}

	@Override
	public @Nonnull List<? extends ModelSetFileInfo> getLocalFiles() {
		return files;
	}
	
	private static String getConnectionString(File file) {
		return "jdbc:sqlite:" + file.getAbsolutePath();
	}	
	
}