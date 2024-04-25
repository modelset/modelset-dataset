package modelset.process.textual;

import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcorePackage;

import modelset.process.textual.AbstractSyntaxPrinter.InvalidModelException;
import modelset.process.textual.CodeXGlueOutput.Mode;

public class TextUtils {

	public static String toEmfaticType(EDataType dt) {
		if (dt instanceof EEnum) {
			EEnum e = (EEnum) dt;
			// FIXME: Todo check, packages
			return AbstractSyntaxPrinter.nonNull(e.getName());
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
		String name = dt.getName();
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
		} else if ("String".equals(name)) {
			return "String";
		} else if ("Integer".equals(name)) {
			return "Integer";			
		} else if ("Double".equals(name)) {
			return "Double";			
		} else if ("Boolean".equals(name)) {
			return "Boolean";			
		} else {
			if (dt.eIsProxy())
				throw new InvalidModelException("Proxy");
			
			throw new UnsupportedOperationException(dt.toString());
		}
		
	}

	public static String toEmfaticCardinality(ETypedElement t, Mode mode) {
		if (t.getLowerBound() == 0 && t.getUpperBound() == 1)
			return applyMode("[ ? ]", mode); // Could be empty string
		else if (t.getLowerBound() == 0 && t.getUpperBound() == -1)
			return applyMode("[ * ]", mode);
		else if (t.getLowerBound() == 1 && t.getUpperBound() == -1)
			return applyMode("[ + ]", mode);
		else if (t.getLowerBound() == 1 && t.getUpperBound() == 1)
			return applyMode("[ 1 ]", mode);
		else if (t.getLowerBound() >= 0 && t.getUpperBound() == -1)
			return applyMode("[ " + t.getLowerBound() + " .. * ]", mode);
		else if (t.getLowerBound() >= 0 && t.getUpperBound() == -2)
			return applyMode("[ " + t.getLowerBound() + " .. ? ]", mode);
		else if (t.getLowerBound() >= 0 && t.getLowerBound() == t.getUpperBound())
			return applyMode("[ " + t.getLowerBound() + " ]", mode);
		else if (t.getLowerBound() >= 0 && t.getUpperBound() > 0)
			return applyMode("[ " + t.getLowerBound() + " .. " + t.getUpperBound() + " ]", mode);
		throw new UnsupportedOperationException(t.toString());
	}

	protected static String applyMode(String cardinalityString, Mode mode) {
		return mode == Mode.FULL ? cardinalityString.replace(" ", "") : cardinalityString;
	}
}
