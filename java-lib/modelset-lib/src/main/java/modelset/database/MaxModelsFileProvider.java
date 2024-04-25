package modelset.database;

import java.util.List;

import javax.annotation.Nonnull;

import mar.validation.IFileInfo;
import mar.validation.IFileProvider;

public class MaxModelsFileProvider implements IFileProvider {

	private IFileProvider provider;
	private int size;

	public MaxModelsFileProvider(IFileProvider provider, int size) {
		this.provider = provider;
		this.size = size;
	}
	
	@Override
	public @Nonnull List<? extends IFileInfo> getLocalFiles() {
		return provider.getLocalFiles().subList(0, size);
	}

}
