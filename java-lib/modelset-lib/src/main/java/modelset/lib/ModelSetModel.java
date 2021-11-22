package modelset.lib;

import java.io.File;
import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;

import mar.indexer.common.configuration.ModelLoader;
import modelset.metadata.AnnotationsValidator.ParsedMetadata;
import modelset.process.ModelSetFileInfo;

public class ModelSetModel {

	private final ModelSetFileInfo file;
	private final ModelType modelType;

	public ModelSetModel(ModelSetFileInfo f, ModelType modelType) {
		this.file = f;
		this.modelType = modelType;
	}

	public String getId() {
		return file.getModelId();
	}

	public File getFullFile() {
		return file.getFullFile();
	}
	
	public File getRelativeFile() {
		return file.getRelativeFile();
	}
	
	public ParsedMetadata getMetadata() {
		return file.getMetadata();
	}
	
	public Resource toResource() throws IOException {
		switch (modelType) {
		case ECORE:
			return ModelLoader.DEFAULT.load(file.getFullFile());
		case UML:
			return ModelLoader.UML.load(file.getFullFile());
		}
		throw new IllegalStateException();
	}

}
