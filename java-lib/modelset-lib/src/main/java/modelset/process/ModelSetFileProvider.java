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

public class ModelSetFileProvider implements IFileProvider {

	private List<IFileInfo> files;

	public ModelSetFileProvider(File db, File repoRoot) throws SQLException {
		Connection dataset = DriverManager.getConnection(getConnectionString(db));
		PreparedStatement stm = dataset.prepareStatement("select mo.id, mo.filename, mm.metadata from models mo join metadata mm on mo.id = mm.id");
		stm.execute();
		this.files = new ArrayList<IFileInfo>();
		ResultSet rs = stm.getResultSet();
		while (rs.next()) {
			String id = rs.getString(1);
			String filename = rs.getString(2);
			files.add(new ModelSetFileInfo(id, filename, repoRoot));
		}			
	}

	@Override
	public @Nonnull List<? extends IFileInfo> getLocalFiles() {
		return files;
	}
	
	private static String getConnectionString(File file) {
		return "jdbc:sqlite:" + file.getAbsolutePath();
	}	
	
}