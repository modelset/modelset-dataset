package modelset.process;

import java.io.File;

import mar.validation.AnalyserRegistry;
import mar.validation.ISingleFileAnalyser;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.Factory;

public class ComputeEcoreStats {

	public static void main(String[] args) throws Exception {
		File repoFolder = new File("../../raw-data/repo-ecore-all");
		File db = new File("../../datasets/dataset.ecore/data/ecore.db");
		File outputAnalysisDB = new File("../../datasets/dataset.ecore/data/analysis.db");
		
		Factory factory = AnalyserRegistry.INSTANCE.getFactory("ecore");
		factory.configureEnvironment();
		ISingleFileAnalyser singleAnalyser = factory.newAnalyser();
		try (ResourceAnalyser analyser = new ResourceAnalyser(singleAnalyser, new ModelSetFileProvider(db, repoFolder), outputAnalysisDB)) {					
			analyser.check();
		}

	}
			
}
