package modelset.database;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import mar.validation.IFileInfo;
import mar.validation.IFileProvider;
import modelset.process.DuplicationDatabase;

public class DuplicationAdapterProvider implements IFileProvider {

	private IFileProvider provider;
	private DuplicationDatabase dupDb;

	public DuplicationAdapterProvider(IFileProvider provider, DuplicationDatabase dupDb) {
		this.provider = provider;
		this.dupDb = dupDb;
	}
	
	@Override
	public @Nonnull List<? extends IFileInfo> getLocalFiles() {
		List<? extends IFileInfo> files = provider.getLocalFiles();
		return files.stream().filter(f -> {
			try {
				return dupDb.isGroupRepresentive(f.getModelId());
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}).collect(Collectors.toList());
	}

}
