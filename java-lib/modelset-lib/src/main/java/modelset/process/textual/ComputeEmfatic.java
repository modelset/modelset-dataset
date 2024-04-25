package modelset.process.textual;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;

import mar.indexer.common.configuration.ModelLoader;
import mar.validation.AnalyserRegistry;
import mar.validation.ResourceAnalyser.Factory;
import modelset.process.textual.CodeXGlueOutput.Mode;
import modelset.process.textual.CodeXGlueOutput.PieceOfCode;

public class ComputeEmfatic extends AbstractSyntaxPrinter<Resource> {
	
	public static void main(String[] args) throws Exception {
		Factory factory = AnalyserRegistry.INSTANCE.getFactory("ecore");
		factory.configureEnvironment();
		
		new ComputeEmfatic().runMain("ComputeEmfatic", Dataset.ECORE, args);
	}


	@Override
	protected List<Resource> getElements(File f) throws IOException {
		Resource r = ModelLoader.DEFAULT.load(f);
		return Collections.singletonList(r);
	}
	
	@Override
	protected void convertToTokens(Resource r, CodeXGlueOutput output) {
		try(PieceOfCode c = output.start()) {
			for (EObject obj : r.getContents()) {
				if (obj instanceof EPackage) {
					convertRootPackage((EPackage) obj, output);
				}
			}
		}
	}
	
	/**
	 * This is intended to be called by clients which wants to generate Emfatic files outside ModelSet
	 */
	public static void toTokens(Resource r, CodeXGlueOutput output) {
		new ComputeEmfatic().convertToTokens(r, output);
	}
	
	protected void convertRootPackage(EPackage obj, CodeXGlueOutput output) {
		//@namespace(uri="AnURI", prefix="uri-name")
		//package ecore;
		convertNamespace(obj, output);
		output.token("package").w().token(obj.getName()).token(";").newLine();
		convertPackageContents(obj, output);
	}

	protected void convertPackageContents(EPackage obj, CodeXGlueOutput output) {
		for (EPackage pkg : obj.getESubpackages()) {
			convertPackage(pkg, output);
		}
		
		for (EClassifier classifier : obj.getEClassifiers()) {
			if (classifier instanceof EClass) {
				convertClass((EClass) classifier, output);
			} else if (classifier instanceof EEnum) {
				convertEnum((EEnum) classifier, output);
			} else if (classifier instanceof EDataType) {
				convertDataType((EDataType) classifier, output);				
			}
		}
	}

	protected void convertEnum(EEnum c, CodeXGlueOutput output) {
		output.token("enum").w().token(nonNull(c.getName())).w();
		output.token("{").newLine().indent();
		for (EEnumLiteral l : c.getELiterals()) {
			output.token(nonNull(l.getName())).w().token("=").w().token(Integer.toString(l.getValue())).token(";");
		}
		output.unindent().token("}").newLine();
	}


	protected void convertDataType(EDataType c, CodeXGlueOutput output) {
		if (c.getInstanceTypeName() == null)
			return;
		
		output.
			token("datatype").w().token(nonNull(c.getName())).w().
			token(":").w().
			token(c.getInstanceTypeName()).
			token(";").newLine();
	}


	protected void convertNamespace(EPackage obj, CodeXGlueOutput output) {
		output.token("@").token("namespace").token("(").
			token("uri").token("=").stringToken(obj.getNsURI()).token(",").w().
			token("prefix").token("=").stringToken(obj.getNsPrefix()).token(")").
			newLine();
	}

	protected void convertPackage(EPackage pkg, CodeXGlueOutput output) {
		convertNamespace(pkg, output);
		output.token("package").w().token(pkg.getName()).w().token("{").newLine().indent();
			convertPackageContents(pkg, output);
		output.unindent().token("}").newLine();
	}

	// (abstract?) class A { }
	protected void convertClass(EClass c, CodeXGlueOutput output) {
		convertClassHeader(c, output);
		output.token("{").newLine().indent();
		convertClassContents(c, output);
		output.unindent().token("}").newLine();
	}


	protected void convertClassHeader(EClass c, CodeXGlueOutput output) {
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
	}

	protected void convertClassContents(EClass c, CodeXGlueOutput output) {
		for (EStructuralFeature feature : c.getEStructuralFeatures()) {
			if (feature instanceof EAttribute) {
				convertAttribute((EAttribute) feature, output);
			} else {
				convertReference((EReference) feature, output);				
			}
		}
	}

	protected void convertAttribute(EAttribute attr, CodeXGlueOutput output) {
		EDataType dt = attr.getEAttributeType();
		String type = toEmfaticType(dt);
		String card = toEmfaticCardinality(attr, output.mode);

		output.token("attr").w();
		output.token(type);
		output.token(card).w();
		output.token(attr.getName());
		output.token(";");
		output.newLine();
	}

	protected void convertReference(EReference ref, CodeXGlueOutput output) {
		EClass referenced = ref.getEReferenceType();
		// TODO: This needs to check whether this is an imported package or a subpackage...
		
		String referencedName = nonNull(referenced.getName());
		String refType = ref.isContainment() ? "val" : "ref";
		String card = toEmfaticCardinality(ref, output.mode);
		
		output.token(refType).w();
		output.token(referencedName);
		output.token(card).w();
		output.token(ref.getName());
		output.token(";");
		output.newLine();		
	}

	protected String toEmfaticType(EDataType dt) {
		return TextUtils.toEmfaticType(dt);
	}

	protected String toEmfaticCardinality(ETypedElement t, Mode mode) {
		return TextUtils.toEmfaticCardinality(t, mode);
	}

}
