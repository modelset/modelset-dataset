package modelset.process;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.emf.ecore.resource.Resource;

import mar.indexer.common.configuration.ModelLoader;
import mar.validation.AnalyserRegistry;
import mar.validation.IFileInfo;
import mar.validation.ResourceAnalyser.Factory;
import modelset.graph.Model2Graph;

public class ComputeGraph {

	public static void main(String[] args) throws Exception {
		File repoFolder = new File("../../raw-data/repo-ecore-all");
		File db = new File("../../datasets/dataset.ecore/data/ecore.db");
		File outputFolder = new File("../../graph/repo-ecore-all");

		ModelLoader loader = ModelLoader.DEFAULT;	
		generateGraph(repoFolder, db, outputFolder, "ecore", loader);	
	}

	private static void generateGraph(File repoFolder, File db, File outputFolder, String modelType, ModelLoader loader) throws SQLException, IOException {
		Factory factory = AnalyserRegistry.INSTANCE.getFactory(modelType);
		factory.configureEnvironment();		
		ModelSetFileProvider provider = new ModelSetFileProvider(db, repoFolder);
		for (IFileInfo f : provider.getLocalFiles()) {
			System.out.println(f.getRelativePath());
			Resource r = loader.load(f.getFullFile());

			Model2Graph converter = new Model2Graph();
			String content = converter.serializeGraphAsJson(r, new SimpleFilter());

			java.nio.file.Path graphFolder = Paths.get(outputFolder.getAbsolutePath(), f.getRelativePath());
			graphFolder.toFile().mkdirs();			
			String fname = graphFolder.getName(graphFolder.getNameCount() - 1).toString();
			java.nio.file.Path outputFile = graphFolder.resolve(FilenameUtils.removeExtension(fname) + ".json");
			
			IOUtils.write(content.getBytes(), new FileOutputStream(outputFile.toFile()));
		}		
	}	
	
}
