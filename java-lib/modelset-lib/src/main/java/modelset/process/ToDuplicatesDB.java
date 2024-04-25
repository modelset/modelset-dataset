package modelset.process;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.eclipse.emf.ecore.resource.Resource;

import mar.analysis.duplicates.EcoreDuplicateFinder;
import mar.analysis.duplicates.UMLDuplicateFinder;
import mar.analysis.duplicates.XtextDuplicateFinder;
import mar.indexer.common.configuration.ModelLoader;
import mar.modelling.loader.ILoader;
import mar.validation.AnalyserRegistry;
import mar.validation.IFileInfo;
import mar.validation.IFileProvider;
import mar.validation.ResourceAnalyser.Factory;
import modelset.process.util.Utils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "compute-duplicates", mixinStandardHelpOptions = true, description = "Compute duplicates of a crawled dataset")
public class ToDuplicatesDB implements Callable<Integer> {

    @Option(required = true, names = { "-r", "--repo-data" }, description = "Data repository folder")
    private File repoFolder;

    @Option(required = true, names = { "-d", "--db" }, description = "Crawler database folder")
    private File crawlerDb;

    @Option(required = true, names = { "-o", "--output" }, description = "Output file for the dataset")
    private File outputFile;

    @Option(required = true, names = { "-t", "--type" }, description = "Artefact type")
    private String type;

	public static void main(String[] args) throws Exception {
        int exitCode = new CommandLine(new ToDuplicatesDB()).execute(args);
        System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		if (outputFile.exists())
			outputFile.delete();
		if (! outputFile.getParentFile().exists()) 
			outputFile.getParentFile().mkdirs();
		
		if (! crawlerDb.exists()) {
			System.out.println("No database file: " + crawlerDb);
			return -1;
		}
			
		
		IFileProvider provider = Utils.loadDatabase(crawlerDb, repoFolder, type);
		
		switch (type) {
		case "xtext":
			XtextDuplicateFinder<IFileInfo> xtextFinder = new XtextDuplicateFinder<IFileInfo>();
			FileBasedDuplicateComputation<File> xdup = new FileBasedDuplicateComputation<>();
			xdup.dumpToDatabase(type, provider, xtextFinder, f -> f , null, outputFile);
			break;
		case "ecore":
			Factory factory = AnalyserRegistry.INSTANCE.getFactory("ecore");
			factory.configureEnvironment();

			EcoreDuplicateFinder<IFileInfo> finder = new EcoreDuplicateFinder<IFileInfo>();
			FileBasedDuplicateComputation<Resource> edup = new FileBasedDuplicateComputation<>();
			edup.dumpToDatabase(type, provider, finder, f -> ModelLoader.DEFAULT.load(f) , r -> r.unload(), outputFile);
			break;
		case "uml":
			Factory factoryUML = AnalyserRegistry.INSTANCE.getFactory("uml");
			factoryUML.configureEnvironment();
			ILoader umlLoader = factoryUML.newLoader();
			
			
			IFileProvider umlProvider = Utils.loadDatabase(crawlerDb, repoFolder, type);
			FileBasedDuplicateComputation<Resource> umlDup = new FileBasedDuplicateComputation<>();
			UMLDuplicateFinder<IFileInfo> umlFinder = new UMLDuplicateFinder<IFileInfo>();
			umlDup.dumpToDatabase(type, umlProvider, umlFinder, f -> umlLoader.toEMF(f) , r -> r.unload(), outputFile);
			break;			
		default:
			break;
		}
		System.out.println("Finished");
	
		return 0;
	}
	
	protected static class FileBasedDuplicateComputation<T> extends AbstractDuplicateComputation<T> {
		@Override
		public void dumpToDatabase(String modelType, mar.validation.IFileProvider provider, mar.analysis.duplicates.DuplicateFinder<IFileInfo, T> finder, AbstractDuplicateComputation.LoaderFunction<T> loader, java.util.function.Consumer<T> unloader, File outputFile) throws SQLException, IOException {
			super.dumpToDatabase(modelType, provider, finder, loader, unloader, outputFile);
		}
	}
	
}
