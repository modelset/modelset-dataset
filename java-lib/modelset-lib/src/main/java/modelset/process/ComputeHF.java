package modelset.process;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;

import org.eclipse.emf.ecore.resource.Resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mar.modelling.loader.ILoader;
import mar.validation.IFileInfo;
import mar.validation.IFileProvider;
import modelset.database.ModelSetFileInfo;

public class ComputeHF {

	public void generateJsonFormat(IFileProvider provider, @CheckForNull DuplicationDatabase dupDb, ILoader loader, String type, File output) throws IOException {
		List<HFLine> lines = new ArrayList<>();
		for (IFileInfo f : provider.getLocalFiles()) {
			System.out.println(f.getRelativePath());
			
			try {
				HFLine line = generateLine(f, dupDb, loader, type);
				lines.add(line);
			} catch (IOException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				if (e.getClass().getName().contains("DiagnosticWrappedException")) {
					System.out.println("Invalid file " + f.getAbsolutePath());
					continue;
				}
				// This is because some graphs are not properly serialized, for the moment we just skip them
				if (e.getCause() instanceof JsonProcessingException) {
					e.printStackTrace();
					System.out.println("Can't generate graph " + f.getAbsolutePath());
					continue;
				}
				throw new RuntimeException(e);
			}
		}
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.writer().writeValue(output, lines);
		
	}
	
	private HFLine generateLine(IFileInfo f, @CheckForNull DuplicationDatabase dupDb, ILoader loader, String type) throws IOException, SQLException {
		Resource r = loader.toEMF(f.getFullFile());
		String id = f.getModelId();
		Path path = f.getFullFile().toPath();
		String label = null;
		if (f instanceof ModelSetFileInfo) {
			label = ((ModelSetFileInfo) f).getCategory();
		}
		
		
		String txt = ComputeTxt.generateSingleText(r);
		String graph = ComputeGraph.generateSingleGraph(r);
		String xmi = Files.readString(path);
		boolean isDuplicated = dupDb == null ? false : ! dupDb.isGroupRepresentive(id);	
		
		r.unload();
		
		return new HFLine(label, id, txt, xmi, isDuplicated, graph, type);
	}

	private static class HFLine {
		@JsonProperty
		private String labels;
		
		@JsonProperty
		private String ids;		
		
		@JsonProperty 
		private String txt;
		
		@JsonProperty 
		private String xmi;
		
		@JsonProperty
		private boolean is_duplicated;
		
		@JsonProperty 
		private String graph;
		
		@JsonProperty
		private String model_type;

		public HFLine(String labels, String ids, String txt, String xmi, boolean is_duplicated, String graph,
				String model_type) {
			super();
			this.labels = labels;
			this.ids = ids;
			this.txt = txt;
			this.xmi = xmi;
			this.is_duplicated = is_duplicated;
			this.graph = graph;
			this.model_type = model_type;
		}

	}
	
}
