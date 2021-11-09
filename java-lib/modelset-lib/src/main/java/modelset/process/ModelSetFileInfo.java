package modelset.process;

import java.io.File;

import javax.annotation.Nonnull;

import mar.validation.IFileInfo;

public class ModelSetFileInfo implements IFileInfo {

	private String id;
	private String filename;
	private File repoRoot;

	public ModelSetFileInfo(String id, String filename, File repoRoot) {
		this.id = id;
		this.filename = filename;
		this.repoRoot = repoRoot;
	}

	@Override
	public @Nonnull File getFullFile() {
		return new File(repoRoot.getAbsolutePath() + File.separator + filename);
	}

	@Override
	public @Nonnull String getModelId() {
		return id;
	}

	@Override
	public @Nonnull File getRelativeFile() {
		return new File(filename);
	}
	
}