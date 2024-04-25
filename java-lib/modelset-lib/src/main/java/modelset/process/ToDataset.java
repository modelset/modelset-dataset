package modelset.process;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import mar.indexer.common.configuration.ModelLoader;
import mar.modelling.loader.ILoader;
import mar.validation.AnalyserRegistry;
import mar.validation.IFileProvider;
import mar.validation.ResourceAnalyser.Factory;
import modelset.database.DuplicationAdapterProvider;
import modelset.database.MarAnalysisFileProvider;
import modelset.database.MarCrawlerFileProvider;
import modelset.database.MinSizeAdapterProvider;
import modelset.database.ModelSetFileProvider;
import modelset.process.util.Utils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "to-dataset", mixinStandardHelpOptions = true, description = "Generates a dataset in different formats")
public class ToDataset implements Callable<Integer> {
	
    @Option(required = true, names = { "-r", "--repo-data" }, description = "Data repository folder")
    private File repoRoot;

    @Option(required = true, names = { "--type" }, description = "The type of the models")
    private String type;

    @Option(required = true, names = { "--db" }, description = "Database: either in ModelSet or MAR analysis or crawler style")
    private File dbFile;

    @Option(required = true, names = { "-o", "--output" }, description = "Output file or folder for the dataset")
    private File output;

    @Option(required = false, names = { "--dup" }, description = "Duplication database")
    private File duplicationDbFile = null;

    @Option(required = true, names = { "--target" }, description = "Target dataset: txt, graph, hf")
    private String target;

    @Option(required = false, names = { "--min_size" }, description = "Minimun size of the model in number of elements")
    private int minSize = -1;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ToDataset()).execute(args);
        System.exit(exitCode);
    }

	@Override
	public Integer call() throws Exception {
		Target tgt = Target.valueOf(target.toUpperCase());
		if (tgt == null)
			throw new IllegalArgumentException("Not allowed target");
		
		IFileProvider provider = Utils.loadDatabase(dbFile, repoRoot, type);
		IFileProvider originalProvider = provider;
		
		DuplicationDatabase dupDb = null;
		if (duplicationDbFile != null) {
			if (! duplicationDbFile.exists()) {
				System.out.println("Duplication db doesn't exist: " + duplicationDbFile);
				return -1;
			}
			dupDb = new DuplicationDatabase(duplicationDbFile);
			provider = new DuplicationAdapterProvider(provider, dupDb);
		}
		

		Factory factory = AnalyserRegistry.INSTANCE.getFactory(type);
		factory.configureEnvironment();
		ILoader loader = factory.newLoader();
		
		if (minSize > 0) {
			System.out.println("Num files: " + provider.getLocalFiles().size());
			provider = new MinSizeAdapterProvider(provider, minSize, loader);
			originalProvider = new MinSizeAdapterProvider(provider, minSize, loader);
		}

		switch (tgt) {
		case TXT:
			ComputeTxt.generateTxt(provider, output, type, Utils.getLoader(type));
			break;
		case GRAPH:
			ComputeGraph.generateGraph(provider, output, type, Utils.getLoader(type));
			break;
		case HF:
			ComputeHF hf = new ComputeHF();
			hf.generateJsonFormat(originalProvider, dupDb, loader, type, output);
			break;
		}
	
		return 0;
	}
	

	public static enum Target {
		TXT,
		GRAPH,
		HF
	}
}