package modelset.process;

import java.io.File;

import mar.validation.AnalyserRegistry;
import mar.validation.ISingleFileAnalyser;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.Factory;

public class ComputeUMLStats {
			
	public static void main(String[] args) throws Exception {
		File repoFolder = new File("../../raw-data/repo-genmymodel-uml");
		File db = new File("../../datasets/dataset.genmymodel/data/genmymodel.db");
		File outputAnalysisDB = new File("../../datasets/dataset.genmymodel/data/analysis.db");
		
		Factory factory = AnalyserRegistry.INSTANCE.getFactory("uml");
		factory.configureEnvironment();
		ISingleFileAnalyser singleAnalyser = factory.newAnalyser();
		try (ResourceAnalyser analyser = new ResourceAnalyser(singleAnalyser, new ModelSetFileProvider(db, repoFolder), outputAnalysisDB)) {					
			analyser.check();
		}

	}	
}
