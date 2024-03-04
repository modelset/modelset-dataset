package modelset.process;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

import mar.indexer.common.configuration.ModelLoader;
import mar.validation.AnalyserRegistry;
import mar.validation.IFileInfo;
import mar.validation.ResourceAnalyser.Factory;

public class ComputeEmfatic {

	private static enum Mode {
		TOKEN,
		LINE,
		FULL
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("./ComputeEmfatic mode dups/no-dups output-file");
			return;
		}
		
		Mode mode;
		if (args[0].contains("line")) {
			mode = Mode.LINE;
		} else if (args[0].contains("token")) {
			mode = Mode.TOKEN;
		} else {
			mode = Mode.FULL;
		}
		
		boolean filterDuplicates = false;
		if (args[1].contains("no-dups")) {
			filterDuplicates = true;
		}
		
		
		File outputFile = new File(args[2]);
		
		File repoFolder = new File("../../raw-data/repo-ecore-all");
		File db = new File("../../datasets/dataset.ecore/data/ecore.db");
		DuplicationDatabase dupDb = null;
		if (filterDuplicates) {
			dupDb = new DuplicationDatabase(new File("../../dups/ecore-dups.db"));
		}
		
		ModelLoader loader = ModelLoader.DEFAULT;
		generateTokenization(repoFolder, db, outputFile, "ecore", mode, dupDb, loader);
		
		dupDb.close();
	}

	private static void generateTokenization(File repoFolder, File db, File outputFile, String modelType, Mode mode, DuplicationDatabase dupDb, ModelLoader loader) throws SQLException, IOException {
		Factory factory = AnalyserRegistry.INSTANCE.getFactory(modelType);
		factory.configureEnvironment();
		
		Map<String, Object> result = new HashMap<>();
		
		CodeXGlueOutput all = new CodeXGlueOutput(mode);				
		ModelSetFileProvider provider = new ModelSetFileProvider(db, repoFolder);
		for (IFileInfo f : provider.getLocalFiles()) {
			if (dupDb != null) {
				if (! dupDb.isGroupRepresentive(f.getModelId()))
					continue;
			}
			
			System.out.println(f.getRelativePath());
			Resource r = loader.load(f.getFullFile());

			try {
				CodeXGlueOutput output = new CodeXGlueOutput(mode);
				convertToEmfaticTokens(r, output);
				
				if (mode == Mode.FULL) {
					Map<String, Object> model = new HashMap<>();
					model.put("raw", output.builder.toString());
					result.put(f.getModelId(), model);
				} else {
					all.merge(output);
				}
			} catch (InvalidModelException e) {
				System.out.println("Invalid model: " + f);
			}
		}	
				
		
		if (mode == Mode.FULL) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.writer().writeValue(outputFile, result);
		} else {
			Files.write(all.builder.toString().getBytes(), outputFile);
		}
	}	
	
	private static void convertToEmfaticTokens(Resource r, CodeXGlueOutput output) {
		try(PieceOfCode c = output.start()) {
			for (EObject obj : r.getContents()) {
				if (obj instanceof EPackage) {
					convertRootPackage((EPackage) obj, output);
				}
			}
		}
	}

	private static void convertRootPackage(EPackage obj, CodeXGlueOutput output) {
		//@namespace(uri="AnURI", prefix="uri-name")
		//package ecore;
		convertNamespace(obj, output);
		output.token("package").w().token(obj.getName()).token(";").newLine();
		convertPackageContents(obj, output);
	}

	private static void convertPackageContents(EPackage obj, CodeXGlueOutput output) {
		for (EPackage pkg : obj.getESubpackages()) {
			convertPackage(pkg, output);
		}
		
		for (EClassifier classifier : obj.getEClassifiers()) {
			if (classifier instanceof EClass) {
				convertClass((EClass) classifier, output);
			}
		}
	}

	private static void convertNamespace(EPackage obj, CodeXGlueOutput output) {
		output.token("@").token("namespace").token("(").
			token("uri").token("=").stringToken(obj.getNsURI()).token(",").w().
			token("prefix").token("=").stringToken(obj.getNsPrefix()).token(")").
			newLine();
	}

	private static void convertPackage(EPackage pkg, CodeXGlueOutput output) {
		convertNamespace(pkg, output);
		output.token("package").w().token(pkg.getName()).w().token("{").newLine().indent();
			convertPackageContents(pkg, output);
		output.unindent().token("}").newLine();
	}

	// (abstract?) class A { }
	private static void convertClass(EClass c, CodeXGlueOutput output) {
		if (c.isAbstract())
			output.token("abstract").w();
		output.token("class").w().token(nonNull(c.getName())).w();
		
		if (c.getESuperTypes().size() > 0) {
			output.token("extends").w();
			for (int i = 0, len = c.getESuperTypes().size(); i < len; i++) {
				EClass sup = c.getESuperTypes().get(i);
				output.token(nonNull(sup.getName()));
				if (i + 1 != len)
					output.token(",").w();
			}
		}
		
		output.token("{").newLine().indent();
		convertClassContents(c, output);
		output.unindent().token("}").newLine();
	}

	private static void convertClassContents(EClass c, CodeXGlueOutput output) {
		for (EStructuralFeature feature : c.getEStructuralFeatures()) {
			if (feature instanceof EAttribute) {
				convertAttribute((EAttribute) feature, output);
			} else {
				convertReference((EReference) feature, output);				
			}
		}
	}

	private static void convertAttribute(EAttribute attr, CodeXGlueOutput output) {
		EDataType dt = attr.getEAttributeType();
		String type = toEmfaticType(dt);
		String card = applyMode(output.mode, toEmfaticCardinality(attr));

		output.token("attr").w();
		output.token(type);
		output.token(card).w();
		output.token(attr.getName());
		output.token(";");
		output.newLine();
	}

	private static void convertReference(EReference ref, CodeXGlueOutput output) {
		EClass referenced = ref.getEReferenceType();
		// TODO: This needs to check whether this is an imported package or a subpackage...
		
		String referencedName = nonNull(referenced.getName());
		String refType = ref.isContainment() ? "val" : "ref";
		String card = applyMode(output.mode, toEmfaticCardinality(ref));
		
		output.token(refType).w();
		output.token(referencedName);
		output.token(card).w();
		output.token(ref.getName());
		output.token(";");
		output.newLine();		
	}

	private static String toEmfaticType(EDataType dt) {
		if (dt instanceof EEnum) {
			EEnum e = (EEnum) dt;
			// FIXME: Todo check, packages
			return e.getName();
		}
		
		if (dt == EcorePackage.Literals.EBOOLEAN)
			return "boolean";
		else if (dt == EcorePackage.Literals.EBOOLEAN_OBJECT) {
			return "Boolean";
		} else if (dt == EcorePackage.Literals.EBYTE) {
			return "byte";
		} else if (dt == EcorePackage.Literals.EBYTE_OBJECT) {
			return "Byte";
		} else if (dt == EcorePackage.Literals.ECHAR) {
			return "char";
		} else if (dt == EcorePackage.Literals.ECHARACTER_OBJECT) {
			return "Character";
		} else if (dt == EcorePackage.Literals.EDOUBLE) {
			return "double";
		} else if (dt == EcorePackage.Literals.EDOUBLE_OBJECT) {
			return "Double";
		} else if (dt == EcorePackage.Literals.EINT) {
			return "int";
		} else if (dt == EcorePackage.Literals.EINTEGER_OBJECT) {
			return "Integer";
		} else if (dt == EcorePackage.Literals.ELONG) {
			return "long";
		} else if (dt == EcorePackage.Literals.ELONG_OBJECT) {
			return "Long";
		} else if (dt == EcorePackage.Literals.ESHORT) {
			return "short";
		} else if (dt == EcorePackage.Literals.ESHORT_OBJECT) {
			return "Short";
		} else if (dt == EcorePackage.Literals.EDATE) {
			return "Date";
		} else if (dt == EcorePackage.Literals.ESTRING) {
			return "String";
		} else if (dt == EcorePackage.Literals.EJAVA_OBJECT) {
			return "Object";
		} else if (dt == EcorePackage.Literals.EJAVA_CLASS) {
			return "Class";
		} else if (dt == EcorePackage.Literals.EOBJECT) {
			// This doesn't look correct, because EObject is an EClass
			return "EObject";
		} else if (dt == EcorePackage.Literals.ECLASS) {
			// This doesn't look correct, because EClass is an EClass
			return "EClass";
		}
		
		String typeName = dt.getInstanceTypeName();
		if (typeName != null) {
			if ("org.eclipse.emf.ecore.EObject".equals(typeName))
				return "EObject";
			if ("org.eclipse.emf.ecore.EClass".equals(typeName))
				return "EClass";
			
			if (typeName.startsWith("org.eclipse.emf.ecore")) {
				String[] parts = typeName.split("\\.");			
				return "ecore." + parts[parts.length - 1];
			}
			// FIXME: Not sure about this
			return typeName;
		} else {
			if (dt.eIsProxy())
				throw new InvalidModelException();
			
			throw new UnsupportedOperationException(dt.toString());
		}
		
	}

	private static String toEmfaticCardinality(ETypedElement t) {
		if (t.getLowerBound() == 0 && t.getUpperBound() == 1)
			return "[ ? ]"; // Could be empty string
		else if (t.getLowerBound() == 0 && t.getUpperBound() == -1)
			return "[ * ]";
		else if (t.getLowerBound() == 1 && t.getUpperBound() == -1)
			return "[ + ]";
		else if (t.getLowerBound() == 1 && t.getUpperBound() == 1)
			return "[ 1 ]";
		else if (t.getLowerBound() >= 0 && t.getUpperBound() == -1)
			return "[ " + t.getLowerBound() + " .. * ]";
		else if (t.getLowerBound() >= 0 && t.getUpperBound() == -2)
			return "[ " + t.getLowerBound() + " .. ? ]";
		else if (t.getLowerBound() >= 0 && t.getLowerBound() == t.getUpperBound())
			return "[ " + t.getLowerBound() + " ]";
		else if (t.getLowerBound() >= 0 && t.getUpperBound() > 0)
			return "[ " + t.getLowerBound() + " .. " + t.getUpperBound() + " ]";
		throw new UnsupportedOperationException(t.toString());
	}

	private static String applyMode(Mode mode, String cardinalityString) {
		return mode == Mode.FULL ? cardinalityString.replace(" ", "") : cardinalityString;
	}
	
	private static <T> T nonNull(T obj) {
		if (obj == null)
			throw new InvalidModelException();
		return obj;
	}


	// https://github.com/microsoft/CodeXGLUE/tree/main/Code-Code/CodeCompletion-token
	private static class CodeXGlueOutput {

		private final StringBuilder builder = new StringBuilder();
		private final Mode mode;
		int indent = 0;
		
		public CodeXGlueOutput(Mode mode) {
			this.mode = mode;
		}

		public CodeXGlueOutput w() {
			if (mode == Mode.FULL)
				builder.append(" ");
			return this;
		}

		public void merge(CodeXGlueOutput output) {
			builder.append(output.builder);
		}

		public CodeXGlueOutput newLine() {
			if (mode == Mode.LINE) {
				builder.append(" <EOL>");
			} else if (mode == Mode.FULL) {
				builder.append("\n");
			}
			return this;
		}
		
		public CodeXGlueOutput indent() {
			indent++;
			return this;
		}

		public CodeXGlueOutput unindent() {
			indent--;
			return this;
		}
		public PieceOfCode start() {
			if (mode != Mode.FULL)
				builder.append(" <s>");
			return new PieceOfCode(this);
		}
		
		public CodeXGlueOutput token(String string) {
			if (string.isEmpty())
				return this;
			
			doIndentIfNeeded();
			
			if (mode != Mode.FULL)
				builder.append(" ");
			builder.append(string);
			return this;
		}

		public CodeXGlueOutput stringToken(String str) {
			doIndentIfNeeded();

			if (mode != Mode.FULL)
				builder.append(" ");
			builder.append("\"");
			builder.append(str);
			builder.append("\"");
			return this;
		}

		private void doIndentIfNeeded() {
			int size = builder.length();
			if (mode == Mode.FULL && size > 0) {
				char last = builder.charAt(size - 1);
				if (last == '\n') {
					for(int i = 0; i < indent; i++)
						builder.append("\t");					
				}
			}
		}		
	}
	
	private static class PieceOfCode implements AutoCloseable {

		private CodeXGlueOutput output;

		public PieceOfCode(CodeXGlueOutput output) {
			this.output = output;
		}

		@Override
		public void close() {
			if (output.mode != Mode.FULL) {
	 			output.builder.append(" ");
				output.builder.append("</s>\n");
			}
		}
	}
	
	private static class InvalidModelException extends RuntimeException {
		private static final long serialVersionUID = 5490556461546321329L;
		
	}
	
	public static class JsonDatasetModel {
		
	}
}
