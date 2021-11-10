package dataset.genmymodel;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import modelset.common.annotations.AnnotationsValidator.SyntaxError;
import modelset.datasetcreator.transformations.MetadataTransformation;

public class ValidateMetadata {

	public static void main(String[] args) {

		File dbFile  = new File("data/genmymodel.db");
		File dscFile = new File("data/genmymodel.dsc");

		AtomicInteger counter = new AtomicInteger();
		MetadataTransformation trafo = new MetadataTransformation(dbFile, dscFile);
		trafo.forEachRawModel(m -> {
			try {
				if (m.isTagged())
					m.toMetadata();
			} catch (SyntaxError e) {
				System.out.println(m.getId());
				System.out.println(m.getMetadata());
				counter.incrementAndGet();
			}			
		});

		System.out.println("Finished. Invalid models: " + counter.get());
	}
	
}
