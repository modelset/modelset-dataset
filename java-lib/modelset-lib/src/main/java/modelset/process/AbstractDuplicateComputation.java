package modelset.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.function.Consumer;

import javax.annotation.CheckForNull;

import mar.analysis.duplicates.DuplicateFinder;
import mar.analysis.duplicates.DuplicateFinder.DuplicationGroup;
import mar.validation.IFileInfo;
import mar.validation.IFileProvider;

public class AbstractDuplicateComputation<T> {

	private static final double T0 = 0.8;
	private static final double T1 = 0.7;

	protected Collection<DuplicationGroup<IFileInfo>> generateDuplicates(IFileProvider provider, 
			DuplicateFinder<IFileInfo, T> finder, 
			LoaderFunction<T> loader, 
			@CheckForNull Consumer<T> unloader)
			throws SQLException, IOException, FileNotFoundException {
		
		
		System.out.println("Loading files...");
		int totalLoadedFiles = 0;
		for (IFileInfo f : provider.getLocalFiles()) {
			try {
				System.out.println("Loading " + f.getAbsolutePath());
				T r = loader.load(f.getFullFile());			
				finder.addResource(f, r);
				if (unloader != null)
					unloader.accept(r);
				totalLoadedFiles++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Total loaded files: " + totalLoadedFiles);
		System.out.println("Computing duplicates...");
		return finder.getDuplicates(T0, T1);		
	}
	

	protected void dumpToDatabase(String modelType, 
			IFileProvider provider, 
			DuplicateFinder<IFileInfo, T> finder, 
			LoaderFunction<T> loader, @CheckForNull Consumer<T> unloader, 
			File outputFile) throws SQLException, IOException {
		
		Collection<DuplicationGroup<IFileInfo>> dups = generateDuplicates(provider, finder, loader, unloader);
		
		System.out.println("Total duplication groups: " + dups.size());
		
		DuplicationDatabase ddb = new DuplicationDatabase(outputFile);
		String groupId = modelType + "_" + T0 + "_" + T1;
		ddb.addDuplicationRun(groupId, T0, T1);
		for (DuplicationGroup<IFileInfo> duplicationGroup : dups) {
			ddb.addGroup(groupId, duplicationGroup);
		}		
		
		ddb.close();

	}

	
	@FunctionalInterface
	public static interface LoaderFunction<T> {
		public T load(File f) throws IOException;
	}
}
