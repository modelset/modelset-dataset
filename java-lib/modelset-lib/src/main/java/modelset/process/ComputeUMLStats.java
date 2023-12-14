package modelset.process;

import java.io.File;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.ImmutableList;

import mar.analysis.uml.UMLAnalyser;
import mar.analysis.uml.UMLAnalyser.ExtendedStatComputation;
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
		UMLAnalyser umlAnalyser = (UMLAnalyser) singleAnalyser;
		umlAnalyser.withExtendedStats(new UMLAdditionalStats());
		
		try (ResourceAnalyser analyser = new ResourceAnalyser(singleAnalyser, new ModelSetFileProvider(db, repoFolder), outputAnalysisDB)) {					
			analyser.check();
		}

	}	

	private static ImmutableList<Class<?>> extendedTypes = ImmutableList.<Class<?>>builder().
			add(org.eclipse.uml2.uml.State.class).
			add(org.eclipse.uml2.uml.Transition.class).
			add(org.eclipse.uml2.uml.Activity.class).
			add(org.eclipse.uml2.uml.Component.class).
			add(org.eclipse.uml2.uml.Package.class).
			add(org.eclipse.uml2.uml.Class.class).
			add(org.eclipse.uml2.uml.Enumeration.class).
			add(org.eclipse.uml2.uml.Interaction.class).
			add(org.eclipse.uml2.uml.DataType.class).
			add(org.eclipse.uml2.uml.Property.class).
			add(org.eclipse.uml2.uml.Relationship.class).
			add(org.eclipse.uml2.uml.Operation.class).
			add(org.eclipse.uml2.uml.Generalization.class).
			add(org.eclipse.uml2.uml.Actor.class).
			add(org.eclipse.uml2.uml.UseCase.class).
			add(org.eclipse.uml2.uml.Association.class).build();

	public static class UMLAdditionalStats implements ExtendedStatComputation {
		
		
		@Override
		public void init(Map<String, Integer> types) {
			for (Class<?> c : extendedTypes) {
				types.put("type_" + c.getSimpleName(), 0);
			}		
		}

		@Override
		public void process(EObject obj, Map<String, Integer> types) {

			for (Class<?> c : extendedTypes) {
				if (c.isInstance(obj)) {
					String name = c.getSimpleName();
					types.compute("type_" + name, (k, v) -> v + 1);
					break;
				}
			}
		}
		
	}
	
	

}
