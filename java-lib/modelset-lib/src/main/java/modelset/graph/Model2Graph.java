package modelset.graph;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.IntegerIdProvider;
import org.jgrapht.nio.dot.DOTExporter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import mar.model2graph.IMetaFilter;
import mar.model2graph.MetaFilterNames;


public class Model2Graph {
	
	public String serializeGraphAsJson(Resource resource, IMetaFilter filter) {
		IntegerIdProvider<Node> p1 = new IntegerIdProvider<Node>();
		Function<Node, String> p2 = (arg0) -> arg0.object.eClass().getName();
		Function<Edge, String> p3 = (arg0) -> arg0.label;
		
		DOTExporter<Node, Edge> exporter = new DOTExporter<>(); //new DOTExporter<>(p1,p2,p3);
		
		Graph<Node, Edge> g = getGraph(resource, filter);
		
		// Export in a JSON format that can be easily added to a Networkx graph
		// using:
		// from networkx.readwrite import json_graph
		// G = json_graph.node_link_graph(data)
		/**
		 {'directed': False,
 			'multigraph': False,
 			'graph': {},
 			'nodes': [{'color': 'red', 'id': 4},
  						{'color': 'green', 'id': 5},
  						{'id': 1},
  						{'id': 2},
  						{'id': 3}],
 			'links': [{'source': 1, 'target': 2}, {'source': 1, 'target': 3}]}
		 */
		Map<String, Object> jsonGraph = new HashMap<String, Object>();
		jsonGraph.put("directed", true);
		jsonGraph.put("multigraph", true);
		
		List<Map<String, Object>> nodes = new ArrayList<>();
		Map<Node, Integer> nodeToId = new HashMap<Model2Graph.Node, Integer>();
		int id = 0;
		for(Node node : g.vertexSet()) {
			Map<String, Object> attrs = new HashMap<String, Object>();
			for(EAttribute a : node.getElement().eClass().getEAllAttributes()) {
				Object o = node.getElement().eGet(a);
				if (o instanceof EEnumLiteral) {
					// FIXME: Possibly convert to string??
					continue;
				}
				attrs.put(a.getName(), o);				
			}
			attrs.put("eClass", node.getElement().eClass().getName());
			attrs.put("id", id);
			nodeToId.put(node, id);
			nodes.add(attrs);
			id++;
		}
		
		List<Map<String, Integer>> links = new ArrayList<>();
		for(Edge e : g.edgeSet()) {
			Node src = g.getEdgeSource(e);
			Node tgt = g.getEdgeTarget(e);
			Map<String, Integer> link = new HashMap<String, Integer>();
			link.put("source", Preconditions.checkNotNull(nodeToId.get(src)));
			link.put("target", Preconditions.checkNotNull(nodeToId.get(tgt)));
			links.add(link);
		}

		jsonGraph.put("nodes", nodes);
		jsonGraph.put("links", links);

		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(jsonGraph);
		} catch (JsonProcessingException e1) {
			throw new RuntimeException(e1);
		}
	}
	
	public static Resource registerMetamodel(String metamodelpath, ResourceSet rs) throws FileNotFoundException, IOException{
		File fileMetamodel = new File(metamodelpath);
		URI uri = URI.createFileURI(fileMetamodel.getAbsolutePath());
		Resource r = rs.getResource(uri, true);
		r.getAllContents().forEachRemaining(o -> {
			if (o instanceof EPackage) {
				EPackage pkg = (EPackage) o;
				EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
			}
		});
		return r;
	}
	
	private Graph<Node, Edge> getGraph(Resource r, IMetaFilter mf){
		
		Graph<Node, Edge> g = GraphTypeBuilder.<Node, Edge>directed().allowingMultipleEdges(true)
				.allowingSelfLoops(true).edgeClass(Edge.class).buildGraph();
		
		TreeIterator<EObject> it = r.getAllContents();
		ArrayList<EObject> allObjects = new ArrayList<EObject>();
		while (it.hasNext()) {
			EObject obj = it.next();
			if (!obj.eIsProxy()) {
				allObjects.add(obj);
			}
		}
		
		for (EObject obj : allObjects) {
			if (obj != null && mf.passFilterObject(obj)) {
				Node n1 = new Node(obj);
				g.addVertex(n1);
				
				for (EStructuralFeature f : obj.eClass().getEAllStructuralFeatures()) {
					if (f.isDerived())
						continue;
					// ignore the structural feature (attribute or reference)
					if (!mf.passFilerStructural(f))
						continue;
					
					if (f instanceof EReference && f.isMany()) {
						@SuppressWarnings("unchecked")
						Collection<EObject> elements = (Collection<EObject>) obj.eGet(f);
						for (EObject e : elements) {
							// ignore the class
							if (e != null && !mf.passFilterObject(e))
								continue;
							if (e != null && e.eIsProxy())
								continue;
							if (!allObjects.contains(e))
								continue;

							if (e != null) {
								Node n2 = new Node(e);
								g.addVertex(n2);
								g.addEdge(n1, n2, new Edge(f.getName()));
							}
						}
						continue;
					}
					
					// take the reference
					if (f instanceof EReference && !f.isMany()) {
						EObject element = (EObject) obj.eGet(f);
						// ignore the class
						if (element != null &&!mf.passFilterObject(element))
							continue;
						if (element != null && element.eIsProxy())
							continue;
						if (!allObjects.contains(element))
							continue;
						if (element != null) {
							Node n2 = new Node(element);
							g.addVertex(n2);
							g.addEdge(n1, n2, new Edge(f.getName()));
							continue;
						}
					}
				}
				
			}
		}
		
		return g;
	}
	
	private static class Node {
		
		EObject object;

		public Node(EObject object) {
			super();
			this.object = object;
		}

		public EObject getElement() {
			return object;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((object == null) ? 0 : object.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return object.eClass().getName();
		}
		
		
	}
	
	private static class Edge extends DefaultEdge {
		
		String label;

		public Edge(String label) {
			super();
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}		
	}

}

