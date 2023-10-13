package modelset.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import org.eclipse.emf.ecore.resource.Resource;

import mar.analysis.duplicates.DuplicateFinder.DuplicationGroup;
import mar.analysis.duplicates.EcoreDuplicateFinder;
import mar.indexer.common.configuration.ModelLoader;
import mar.validation.AnalyserRegistry;
import mar.validation.IFileInfo;
import mar.validation.ResourceAnalyser.Factory;

public class ComputeDuplicates {

	private static final double T0 = 0.8;
	private static final double T1 = 0.7;

	public static void main(String[] args) throws Exception {
		File repoFolder = new File("../../raw-data/repo-ecore-all");
		File db = new File("../../datasets/dataset.ecore/data/ecore.db");
		
		ModelLoader loader = ModelLoader.DEFAULT;	
		Collection<DuplicationGroup<IFileInfo>> dups = generateDuplicates(repoFolder, db, "ecore", loader);
		
		new File("../../dups/").mkdirs();
		
		File outputFile = new File("../../dups/ecore-dups.db");
		if (outputFile.exists())
			outputFile.delete();
		
		DuplicationDatabase ddb = new DuplicationDatabase(outputFile);
		String groupId = "ecore_" + T0 + "_" + T1;
		ddb.addDuplicationRun(groupId, T0, T1);
		for (DuplicationGroup<IFileInfo> duplicationGroup : dups) {
			ddb.addGroup(groupId, duplicationGroup);
		}		
		
		ddb.close();
		
		System.out.println("Finished");
	}

	private static Collection<DuplicationGroup<IFileInfo>> generateDuplicates(File repoFolder, File db, String modelType, ModelLoader loader)
			throws SQLException, IOException, FileNotFoundException {
		
		EcoreDuplicateFinder<IFileInfo> finder = new EcoreDuplicateFinder<>();
				
		Factory factory = AnalyserRegistry.INSTANCE.getFactory(modelType);
		factory.configureEnvironment();
		
		System.out.println("Loading files...");
		ModelSetFileProvider provider = new ModelSetFileProvider(db, repoFolder);
		for (IFileInfo f : provider.getLocalFiles()) {
			Resource r = loader.load(f.getFullFile());			
			finder.addResource(f, r);
			r.unload();
		}
		
		System.out.println("Computing duplicates...");
		return finder.getDuplicates(T0, T1);		
	}
}
