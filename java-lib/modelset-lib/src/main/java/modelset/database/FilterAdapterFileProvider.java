package modelset.database;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import mar.validation.IFileInfo;
import mar.validation.IFileProvider;

public class FilterAdapterFileProvider implements IFileProvider {

	private IFileProvider provider;
	private Predicate<IFileInfo> predicate;

	public FilterAdapterFileProvider(IFileProvider provider, Predicate<IFileInfo> predicate) {
		this.provider = provider;
		this.predicate = predicate;
	}
	
	@Override
	public @Nonnull List<? extends IFileInfo> getLocalFiles() {
		List<? extends IFileInfo> files = provider.getLocalFiles();
		return files.stream().filter(this.predicate).collect(Collectors.toList());
	}
}
