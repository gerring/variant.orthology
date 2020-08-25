package org.jax.gweaver.variant.orthology.node;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

/**
 * 
 * Label creator for graph database. Creates required labels in DB for gene orthology matching type and ID.
 * This class is very simple and replaces some Cypher as simple as:
CREATE INDEX ON :Gene(gene_id);
CREATE INDEX ON :Transcrpit(transcript_id);
CREATE INDEX ON :Variant(rs_id)`;
 * 
 * @author Matthew Gerring
 *
 */
public class IndexCreator {

	private static final List<String> DEFAULT_NODES = Arrays.asList("Gene(gene_id)", "Transcrpit(transcript_id)", "Variant(rs_id)");
	
	private final Session session;
	private List<String> nodes;

	public IndexCreator(SessionFactory factory) {
		this(factory.openSession(), DEFAULT_NODES);
	}
	
	public IndexCreator(Session session) {
		this(session, DEFAULT_NODES);
	}

	public IndexCreator(Session session, List<String> nodeTypes) {
		this.session = session;
		this.nodes = nodeTypes;
	}
	
	/**
	 * Creates the required indices and returns a list of indices created
	 * @return last result
	 */
	public Result create() {
		
		Result res = null;
		Map<String,Object> empty =  Collections.emptyMap();
		for (String node : nodes) {
			String crt = "CREATE INDEX ON :"+node+";";
			res = session.query(crt, empty);
		}
		return res;
	}

	/**
	 * @param labels the labels to set
	 */
	public void setNodes(List<String> ns) {
		this.nodes = ns;
	}

	/**
	 * @return the labels
	 */
	public List<String> getNodes() {
		return nodes;
	}
}
