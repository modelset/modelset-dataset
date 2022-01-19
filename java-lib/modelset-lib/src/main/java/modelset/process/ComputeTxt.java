package modelset.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import mar.indexer.common.configuration.ModelLoader;
import mar.model2graph.IMetaFilter;
import mar.model2text.Model2TextUtils;
import mar.paths.ListofPaths;
import mar.paths.Path;
import mar.paths.PathFactory;
import mar.paths.stemming.IStemmer;
import mar.paths.stemming.IStopWords;
import mar.paths.stemming.ITokenizer;
import mar.paths.stemming.WhitespaceTokenizer;
import mar.validation.AnalyserRegistry;
import mar.validation.IFileInfo;
import mar.validation.ResourceAnalyser.Factory;

public class ComputeTxt {

	public static void main(String[] args) throws Exception {
		File repoFolder = new File("../../raw-data/repo-ecore-all");
		File db = new File("../../datasets/dataset.ecore/data/ecore.db");
		File outputFolder = new File("../../txt/repo-ecore-all");

		ModelLoader loader = ModelLoader.DEFAULT;	
		generateTxt(repoFolder, db, outputFolder, "ecore", loader);	
		
		
		repoFolder = new File("../../raw-data/repo-genmymodel-uml");
		db = new File("../../datasets/dataset.genmymodel/data/genmymodel.db");
		outputFolder = new File("../../txt/repo-genmymodel-uml");
		loader = ModelLoader.UML;
		generateTxt(repoFolder, db, outputFolder, "uml", loader);
	}

	private static void generateTxt(File repoFolder, File db, File outputFolder, String modelType, ModelLoader loader)
			throws SQLException, IOException, FileNotFoundException {
		Factory factory = AnalyserRegistry.INSTANCE.getFactory(modelType);
		factory.configureEnvironment();		
		ModelSetFileProvider provider = new ModelSetFileProvider(db, repoFolder);
		for (IFileInfo f : provider.getLocalFiles()) {
			System.out.println(f.getRelativePath());
			Resource r = loader.load(f.getFullFile());
			String content = Model2TextUtils.model2document(r, new SimpleFilter(), new SimplePathFactory());
			
			java.nio.file.Path txtFolder = Paths.get(outputFolder.getAbsolutePath(), f.getRelativePath());
			txtFolder.toFile().mkdirs();
			
			String fname = txtFolder.getName(txtFolder.getNameCount() - 1).toString();
			java.nio.file.Path outputFile = txtFolder.resolve(FilenameUtils.removeExtension(fname) + ".txt");
			
			IOUtils.write(content.getBytes(), new FileOutputStream(outputFile.toFile()));
		}
	}
	
	private static class SimpleFilter implements IMetaFilter {
	
		private static Set<String> names = new HashSet<>();
		static {
			names.add("nsuri");
			names.add("nsprefix");
		}

		@Override
		public boolean passFilerStructural(EObject f) {
			if (f instanceof EAttribute) {
				String attrName = ((EAttribute) f).getName();
				if (names.contains(attrName.toLowerCase()))
					return false;
			}
			return true;
		}

		@Override
		public boolean passFilterObject(EObject o) {
			return true;
		}
		
	}
	
	private static class SimplePathFactory implements PathFactory {

		@Override
		public IStemmer getStemmer() {
			return IStemmer.IDENTITY;
		}

		@Override
		public IStopWords getStopWords() {
			return new IStopWords() {
				@Override
				public boolean isStopWord(@Nonnull String s) {
					return false;
				}				
			};
		}

		@Override
		public ITokenizer getTokenizer() {
			return ITokenizer.IDENTITY;
		}

		@Override
		public ListofPaths newPathSet(List<? extends Path> arg0) {
			throw new UnsupportedOperationException();
		}
		
	}
}
