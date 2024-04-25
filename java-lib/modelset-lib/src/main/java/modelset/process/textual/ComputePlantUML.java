package modelset.process.textual;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.FinalState;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.Vertex;

import mar.indexer.common.configuration.ModelLoader;
import mar.validation.AnalyserRegistry;
import mar.validation.ResourceAnalyser.Factory;
import modelset.process.textual.CodeXGlueOutput.PieceOfCode;

/**
 * Docs: https://plantuml.com/en/state-diagram
 * 
 */
public class ComputePlantUML extends AbstractSyntaxPrinter<StateMachine> {
	
	public static void main(String[] args) throws Exception {
		Factory factory = AnalyserRegistry.INSTANCE.getFactory("uml");
		factory.configureEnvironment();
		

		new ComputePlantUML().runMain("ComputePlantUML", Dataset.UML, args);
	}
	
	@Override
	protected List<StateMachine> getElements(File f) throws IOException {
		Resource r = ModelLoader.UML.load(f);
		
		List<StateMachine> elements = new ArrayList<>();
		TreeIterator<EObject> it = r.getAllContents();
		while (it.hasNext()) {
			EObject obj = it.next();
			if (obj instanceof StateMachine) {
				if (! isEmpty((StateMachine) obj))
					elements.add((StateMachine) obj);
			}
		}
		return elements;
	}
	
	private boolean isEmpty(StateMachine obj) {
		return obj.getRegions().isEmpty();
	}

	@Override
	protected void convertToTokens(StateMachine sm, CodeXGlueOutput output) {
		try(PieceOfCode c = output.start()) {
			generate(sm, output);
			System.out.println("---");
			System.out.println(output.builder.toString());
		}
	}
	
	public void generate(StateMachine sm, CodeXGlueOutput output) {
		
		List<Region> regions = sm.getRegions();
		
		output.token("@startuml").newLine();
		
		String sep = "";
		for (Region region : regions) {
			if (! sep.isEmpty())
				output.token(sep).newLine();
				
			mapRegion(region, output);
			sep = "---";
		}
		
		output.token("@enduml").newLine();
	}

	// repo-uml-pruned/sm/lituss/LobbyServer/LobbyServer/master/sim.uml
	
	public void mapRegion(Region r, CodeXGlueOutput output) {
		for (Transition transition : r.getTransitions()) {
			if (transition.getSource() == null || transition.getTarget() == null) {
				System.out.println("Malformed model, transition without source or target: " + transition);
				continue;
			}
			
			String src = toStateId(transition.getSource());
			String tgt = toStateId(transition.getTarget());
			
			 // : EvNewValue
			
			String name = transition.getName();
			String label = name == null ? "" : name + " ";
			List<Constraint> rules = transition.getOwnedRules();
			if (rules.size() > 0) {
				// Pick one just to show something
				// Maybe the rest could go in a comment?
				Constraint c = rules.get(0);
				ValueSpecification spec = c.getSpecification();
				if (spec instanceof OpaqueExpression) {
					OpaqueExpression e = (OpaqueExpression) spec;
					String value = String.join("&&", e.getBodies());
					label += value;
				}
			}
			
			output.token(src).w().token("-->").w().token(tgt);
			
			if (label != null && !label.trim().isEmpty()) {
				output.w().token(":").w().token("\"" + label + "\"");
			}
			
			output.newLine();
		}
		
		for (Vertex v : r.getSubvertices()) {
			if (v instanceof State && !(v instanceof FinalState)) {
				List<Region> subRegions = ((State) v).getRegions();
				String sep = "";
				String stateId = toStateId(v);

				
				output.token("state").w().token(stateId).w().token("{").newLine().indent();
				for (Region region : subRegions) {
					if (! sep.isEmpty())
						output.token(sep).newLine();
					
					mapRegion(region, output);
					sep = "---";
				}
				output.unindent().token("}").newLine();
			} else if (v instanceof Pseudostate) {
				// 				state join_state <<join>>
				switch(((Pseudostate) v).getKind()) {
				case FORK_LITERAL:
					output.token("state").w().token(toStateId(v)).w().token("<<fork>>").newLine();;
				case JOIN_LITERAL:
					output.token("state").w().token(toStateId(v)).w().token("<<join>>").newLine();;
					//text.line("state " + toStateId(v) + " <<join>>");
				case JUNCTION_LITERAL:
					//text.line("state " + toStateId(v)); // TOOD: Change color
					output.token("state").w().token(toStateId(v)).newLine();				
				}
				// Ignore rest?
			}
		}
	}
	
	private Map<Vertex, String> names = new HashMap<>();
	
	public String toStateId(Vertex vertex) {
		if (names.containsKey(vertex))
			return names.get(vertex);
		
		String name = toStateId_(vertex, names.size());
		names.put(vertex, name);
		return name;
	}

	public String toStateId_(Vertex vertex, int idx) {		
		if (vertex instanceof FinalState) {
			return "[*]";
		} else if (vertex instanceof State) {
			State state = (State) vertex;
			return normalizeName(state.getName());
		} else if (vertex instanceof Pseudostate) {
			Pseudostate state = (Pseudostate) vertex;

			switch(state.getKind()) {
			case INITIAL_LITERAL:
				return "[*]";
			case CHOICE_LITERAL:
			case DEEP_HISTORY_LITERAL:
			case SHALLOW_HISTORY_LITERAL:
				return toSynthesizedName(state.getName(), "H", idx);
			case ENTRY_POINT_LITERAL:
			case EXIT_POINT_LITERAL:
			case FORK_LITERAL:
				if (vertex.getName() == null)
					return "fork_" + idx;
				return vertex.getName() + "_" + idx;
			case JOIN_LITERAL:
				if (vertex.getName() == null)
					return "join_" + idx;
				return vertex.getName() + "_" + idx;
			case JUNCTION_LITERAL:
				return "junction_" + idx;
			case TERMINATE_LITERAL:
				// TODO: Mark this specially, with a comment?
				return "[*]";
			default:
				break;
			
			}
		}
		throw new UnsupportedOperationException("Vertex not supported: " + vertex);
	}

	private String normalizeName(String name) {
		//if (!name.matches("^[A-Za-z0-9_]+$")) {
		//	return "\"" + name + "\"";			
		//}
		return name.replaceAll("[^A-Za-z0-9_]", "_");
	}
	
	private String toSynthesizedName(String name, String hint, int idx) {
		name = normalizeName(name);
		return name == null ? (hint + "_" + idx) : (name + "_" + hint);
	}
	
	
}
