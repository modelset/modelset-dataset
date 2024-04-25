package modelset.database;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import mar.modelling.loader.ILoader;
import mar.validation.IFileInfo;
import mar.validation.IFileProvider;

public class MinSizeAdapterProvider implements IFileProvider {

	private IFileProvider provider;
	private ILoader loader;
	private int minSize;

	public MinSizeAdapterProvider(IFileProvider provider, int minSize, ILoader loader) {
		this.provider = provider;
		this.loader = loader;
		this.minSize = minSize;
	}
	
	@Override
	public @Nonnull List<? extends IFileInfo> getLocalFiles() {
		List<? extends IFileInfo> files = provider.getLocalFiles();
		return files.stream().filter(f -> {
			try {
				Resource r = loader.toEMF(f.getFullFile());
				boolean b = hasEnoughElements(r);
				r.unload();
				return b;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}).collect(Collectors.toList());
	}

	private boolean hasEnoughElements(Resource r) {
		int count = 0;
		TreeIterator<EObject> it = r.getAllContents();
		while (it.hasNext()) {
			count++;
			if (count > minSize)
				return true;
		}
		return false;
	}

}
