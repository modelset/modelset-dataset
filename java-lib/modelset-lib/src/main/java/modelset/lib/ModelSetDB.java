package modelset.lib;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import mar.validation.IFileInfo;
import modelset.process.ModelSetFileProvider;

public class ModelSetDB {

	private final List<ModelSetModel> files;
	private final ModelType modelType;
	
	public ModelSetDB(File db, File repoRoot, ModelType modelType) throws SQLException {
		ModelSetFileProvider provider = new ModelSetFileProvider(db, repoRoot);
		this.modelType = modelType;
		this.files = provider.getLocalFiles().stream().map(f -> toModel(f)).collect(Collectors.toList());
	}
	
	public ModelSetDB(String db, String repoRoot, ModelType modelType) throws SQLException {
		this(new File(db), new File(repoRoot), modelType);
	}
	
	public List<? extends ModelSetModel> getFiles() {
		return files;
	}
	
	public ModelType getModelType() {
		return modelType;
	}
	
	private ModelSetModel toModel(IFileInfo f) {
		return new ModelSetModel(f, modelType);
	}
	
}
