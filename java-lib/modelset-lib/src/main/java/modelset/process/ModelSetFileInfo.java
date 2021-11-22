package modelset.process;

import java.io.File;

import javax.annotation.Nonnull;

import mar.validation.IFileInfo;
import modelset.metadata.AnnotationsValidator.ParsedMetadata;

public class ModelSetFileInfo implements IFileInfo {

	private String id;
	private String filename;
	private File repoRoot;
	private ParsedMetadata metadata;

	public ModelSetFileInfo(String id, String filename, File repoRoot, ParsedMetadata m) {
		this.id = id;
		this.filename = filename;
		this.repoRoot = repoRoot;
		this.metadata = m;
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
	
	@Nonnull
	public ParsedMetadata getMetadata() {
		return metadata;
	}
}