package modelset.process;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Nonnull;

import mar.analysis.duplicates.DuplicateFinder.DuplicationGroup;
import mar.validation.IFileInfo;

/**
 * A wrapper to handle the duplication database.
 * 
 * @author jesus
 *
 */
public class DuplicationDatabase implements AutoCloseable {
	private Connection connection;

	@Nonnull	
	public DuplicationDatabase(File file) {					
		String url = getConnectionString(file);
		 
        try {
        	Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
            	if (! file.exists()) {
	                DatabaseMetaData meta = conn.getMetaData();
	                System.out.println("The driver name is " + meta.getDriverName());
	                System.out.println("A new database has been created.");
            	}
            	
                String duplication_run = "CREATE TABLE IF NOT EXISTS duplication_run (\n"
                        + "    id varchar(255) NOT NULL,\n" 
                        + "    t0 float NOT NULL,\n"      
                        + "    t1 float NOT NULL\n"                          
                        + ");";

                String duplicates = "CREATE TABLE IF NOT EXISTS duplicates (\n"
                        + "    model_id    varchar(255) NOT NULL,\n"  
                        + "    group_id    varchar (255) NOT NULL,\n"
                        + "    duplication_run varchar (255) NOT NULL\n"
                        + ");";
                                
                Statement stmt = conn.createStatement();
                stmt.execute(duplication_run);
                
                stmt = conn.createStatement();
                stmt.execute(duplicates);             
            }
                        
            this.connection = conn;
            this.connection.setAutoCommit(false);            
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
	}

	@Override
	public void close() throws SQLException {
		this.connection.commit();
		this.connection.close();
	}

	@Nonnull
	public static String getConnectionString(File file) {
		return "jdbc:sqlite:" + file.getAbsolutePath();
	}


	public void addDuplicationRun(String id, double t0, double t1) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO duplication_run(id, t0, t1) VALUES (?, ?, ?)");
		preparedStatement.setString(1, id);
		preparedStatement.setDouble(2, t0);
		preparedStatement.setDouble(3, t1);
		preparedStatement.execute();
		preparedStatement.close();			
	}

	public void addGroup(String runId, DuplicationGroup<IFileInfo> duplicationGroup) throws SQLException {
		try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO duplicates(model_id, group_id, duplication_run) VALUES (?, ?, ?)")) {
			String groupId = duplicationGroup.getRepresentative().getModelId();
			for (IFileInfo f : duplicationGroup) {
				String id = f.getModelId();
				preparedStatement.setString(1, id);
				preparedStatement.setString(2, groupId);
				preparedStatement.setString(3, runId);
				preparedStatement.execute();
			}
		}		
	}

	public boolean isGroupRepresentive(String modelId) throws SQLException {
		try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT model_id FROM duplicates WHERE model_id = ?")) {
			preparedStatement.setString(1, modelId);
			ResultSet rs = preparedStatement.executeQuery();
			return rs.next();
		}		
	}

}
