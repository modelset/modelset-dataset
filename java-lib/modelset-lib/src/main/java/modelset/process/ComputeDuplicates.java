package modelset.process;

import java.io.File;

import org.eclipse.emf.ecore.resource.Resource;

import mar.analysis.duplicates.EcoreDuplicateFinder;
import mar.indexer.common.configuration.ModelLoader;
import mar.validation.AnalyserRegistry;
import mar.validation.IFileInfo;
import mar.validation.ResourceAnalyser.Factory;

public class ComputeDuplicates extends AbstractDuplicateComputation<Resource> {

	public static void main(String[] args) throws Exception {
		File repoFolder = new File("../../raw-data/repo-ecore-all");
		File db = new File("../../datasets/dataset.ecore/data/ecore.db");

		Factory factory = AnalyserRegistry.INSTANCE.getFactory("ecore");
		factory.configureEnvironment();

		EcoreDuplicateFinder<IFileInfo> finder = new EcoreDuplicateFinder<>();
		ModelSetFileProvider provider = new ModelSetFileProvider(db, repoFolder);

		ModelLoader loader = ModelLoader.DEFAULT;	
				
		new File("../../dups/").mkdirs();
		
		File outputFile = new File("../../dups/ecore-dups.db");
		if (outputFile.exists())
			outputFile.delete();

		ComputeDuplicates dup = new ComputeDuplicates();
		dup.dumpToDatabase("ecore", provider, finder, loader::load, (r) -> r.unload(), outputFile);
				
		System.out.println("Finished");
	}

}
