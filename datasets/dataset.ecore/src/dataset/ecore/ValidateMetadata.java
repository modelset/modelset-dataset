package dataset.ecore;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.Nullable;

import modelset.common.annotations.AnnotationsValidator.ParsedMetadata;
import modelset.common.annotations.AnnotationsValidator.SyntaxError;
import modelset.datasetcreator.transformations.MetadataTransformation;

public class ValidateMetadata {

	public static void main(String[] args) {

		File dbFile  = new File("data/ecore.db");
		File dscFile = new File("data/ecore.dsc");

		AtomicInteger counter = new AtomicInteger();
		MetadataTransformation trafo = new MetadataTransformation(dbFile, dscFile);
		trafo.forEachRawModel(m -> {
			try {
				if (m.isTagged()) {
					ParsedMetadata md = m.toMetadata();
					@Nullable
					List<? extends String> cat = md.getValues("category");
					if (cat != null && cat.size() > 1) {
						System.out.println("More than one category: " + m.getId());
					}
					if (cat != null && cat.size() == 0) {
						System.out.println("No category: " + m.getId());
					}
				}
			} catch (SyntaxError e) {
				System.out.println(m.getId());
				counter.incrementAndGet();
			}			
		});

		System.out.println("Finished. Invalid models: " + counter.get());

	}
	
}
