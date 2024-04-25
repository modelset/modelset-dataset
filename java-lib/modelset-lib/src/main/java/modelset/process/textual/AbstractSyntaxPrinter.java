package modelset.process.textual;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

import mar.validation.IFileInfo;
import mar.validation.IFileProvider;
import modelset.database.ModelSetFileProvider;
import modelset.process.DuplicationDatabase;
import modelset.process.textual.CodeXGlueOutput.Mode;

public abstract class AbstractSyntaxPrinter<T> {
	
	protected static enum Dataset {
		ECORE,
		UML
	}
	
	protected void runMain(String name, Dataset dataset, String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("./" + name + " mode dups/no-dups output-file");
			return;
		}
		
		Mode mode = getMode(args[0]);
		boolean filterDuplicates = getFilterDuplicates(args[1]);
		
		File outputFile = new File(args[2]);
		
		
		File repoFolder;
		File db;
		File dupDbFile;

		if (dataset == Dataset.ECORE) {
			repoFolder = new File("../../raw-data/repo-ecore-all");
			db = new File("../../datasets/dataset.ecore/data/ecore.db");
			dupDbFile = new File("../../dups/ecore-dups.db");
		} else if (dataset == Dataset.UML) {
			repoFolder = new File("../../raw-data/repo-genmymodel-uml");
			db = new File("../../datasets/dataset.genmymodel/data/genmymodel.db");
			dupDbFile = new File("../../dups/uml-dups.db");
		} else {
			throw new IllegalArgumentException("Invalid dataset");
		}
		
		DuplicationDatabase dupDb = null;
		if (filterDuplicates) {
			dupDb = new DuplicationDatabase(dupDbFile);
		}
		
		ModelSetFileProvider provider = new ModelSetFileProvider(db, repoFolder);
		generateTokenization(provider, outputFile, mode, dupDb);
		
		if (dupDb != null)
			dupDb.close();
	
	}

	protected boolean getFilterDuplicates(String dupsStr) {
		boolean filterDuplicates = false;
		if (dupsStr.contains("no-dups")) {
			filterDuplicates = true;
		}
		return filterDuplicates;
	}

	protected Mode getMode(String modeStr) {
		Mode mode;
		if (modeStr.contains("line")) {
			mode = Mode.LINE;
		} else if (modeStr.contains("token")) {
			mode = Mode.TOKEN;
		} else {
			mode = Mode.FULL;
		}
		return mode;
	}

	protected void generateTokenization(IFileProvider provider, File outputFile, Mode mode, DuplicationDatabase dupDb) throws SQLException, IOException {
		Map<String, Object> result = new HashMap<>();
		
		CodeXGlueOutput all = new CodeXGlueOutput(mode);
		int processed = 0;
		for (IFileInfo f : provider.getLocalFiles()) {
			if (dupDb != null) {
				if (! dupDb.isGroupRepresentive(f.getModelId()))
					continue;
			}
			
			System.out.println(f.getRelativePath());

			try {
				List<T> printableElements = getElements(f.getFullFile());
				
				int i = 0;
				for (T t : printableElements) {
					CodeXGlueOutput output = new CodeXGlueOutput(mode);
					convertToTokens(t, output);
					
					if (mode == Mode.FULL) {
						Map<String, Object> model = new HashMap<>();
						model.put("raw", output.builder.toString());
						
						String id = f.getModelId() + "#" + i;
						result.put(id, model);
					} else {
						all.merge(output);
					}
					
					unload(t);
				}
				
				processed++;
				if (processed % 10 == 0) {
					System.out.println("Writing intermediate results with " + processed + " files");
					writeResult(outputFile, mode, result, all);
				}
			} catch (InvalidModelException e) {
				System.out.println("Invalid model: " + f.getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
				
		
		writeResult(outputFile, mode, result, all);
	}

	private void writeResult(File outputFile, Mode mode, Map<String, Object> result, CodeXGlueOutput all)
			throws IOException, StreamWriteException, DatabindException {
		if (mode == Mode.FULL) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.writer().writeValue(outputFile, result);
		} else {
			System.out.println("Writing to: " + outputFile);
			Files.write(all.builder.toString().getBytes(), outputFile);
		}
	}	

	protected void unload(T t) {
		// To be implemented by subclasses
	}

	protected abstract List<T> getElements(File f) throws IOException;

	protected abstract void convertToTokens(T r, CodeXGlueOutput output);

	@SuppressWarnings("unused")
	protected
	static <T> T nonNull(T obj) {
		if (obj == null)
			throw new InvalidModelException("Null value");
		return obj;
	}

	public static class InvalidModelException extends RuntimeException {
		public InvalidModelException(String string) {
			super(string);
		}

		private static final long serialVersionUID = 5490556461546321329L;
		
	}
	

}
