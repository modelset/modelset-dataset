package modelset.process;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EStringToStringMapEntryImpl;

import mar.model2graph.IMetaFilter;

class SimpleFilter implements IMetaFilter {

	private static Set<String> names = new HashSet<>();
	static {
		names.add("nsuri");
		names.add("nsprefix");
	}

	@Override
	public boolean passFilerStructural(EObject f) {
		if (f instanceof EAttribute) {
			String attrName = ((EAttribute) f).getName();
			if (names.contains(attrName.toLowerCase()))
				return false;
		}
		return true;
	}

	@Override
	public boolean passFilterObject(EObject o) {
		if (o instanceof EAnnotation || o instanceof BasicEMap.Entry)
			return false;
		return true;
	}
	
}