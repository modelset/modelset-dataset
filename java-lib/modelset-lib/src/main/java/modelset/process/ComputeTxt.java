package modelset.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.emf.ecore.resource.Resource;

import mar.indexer.common.configuration.ModelLoader;
import mar.model2text.Model2TextUtils;
import mar.validation.AnalyserRegistry;
import mar.validation.IFileInfo;
import mar.validation.IFileProvider;
import mar.validation.ResourceAnalyser.Factory;
import modelset.database.ModelSetFileProvider;

public class ComputeTxt {

	public static void main(String[] args) throws Exception {
		File repoFolder = new File("../../raw-data/repo-ecore-all");
		File db = new File("../../datasets/dataset.ecore/data/ecore.db");
		File outputFolder = new File("../../txt/repo-ecore-all");

		ModelLoader loader = ModelLoader.DEFAULT;	
		ModelSetFileProvider provider = new ModelSetFileProvider(db, repoFolder);
		generateTxt(provider, outputFolder, "ecore", loader);	
		
		
		repoFolder = new File("../../raw-data/repo-genmymodel-uml");
		db = new File("../../datasets/dataset.genmymodel/data/genmymodel.db");
		outputFolder = new File("../../txt/repo-genmymodel-uml");
		loader = ModelLoader.UML;
		provider = new ModelSetFileProvider(db, repoFolder);
		generateTxt(provider, outputFolder, "uml", loader);
	}

	public static void generateTxt(IFileProvider provider, File outputFolder, String modelType, ModelLoader loader)
			throws SQLException, IOException, FileNotFoundException {
		Factory factory = AnalyserRegistry.INSTANCE.getFactory(modelType);
		factory.configureEnvironment();		
		for (IFileInfo f : provider.getLocalFiles()) {
			System.out.println(f.getRelativePath());
			Resource r = loader.load(f.getFullFile());
			String content = generateSingleText(r);
			r.unload();
			
			java.nio.file.Path txtFolder = Paths.get(outputFolder.getAbsolutePath(), f.getRelativePath());
			txtFolder.toFile().mkdirs();
			
			String fname = txtFolder.getName(txtFolder.getNameCount() - 1).toString();
			java.nio.file.Path outputFile = txtFolder.resolve(FilenameUtils.removeExtension(fname) + ".txt");
			
			IOUtils.write(content.getBytes(), new FileOutputStream(outputFile.toFile()));
		}
	}

	public static String generateSingleText(Resource r) {
		String content = Model2TextUtils.model2document(r, new SimpleFilter(), new SimplePathFactory());
		return content;
	}
}
