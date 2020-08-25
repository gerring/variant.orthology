package org.jax.gweaver.variant.orthology.node;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;

/**
 * Gets the labels in the system.
 * 
 * @author Matthew Gerring
 *
 */
public class NodeManager {
	
	private Session session;

	public NodeManager(Session session) {
		this.session = session;
	}

	/**
	 * 
	 * @return string list of all labels.
	 */
	public List<String> getDistinctLabels() {
		
		return call("MATCH (n) RETURN distinct labels(n);", "labels(n)");
	}

	
	public <T> List<T> getAllNodes() {
		
		return call("MATCH (s) RETURN s;", "s");
	}
	
	@SuppressWarnings("unchecked")
	private <T> List<T> call(String cypher, String var) {
		
		Map<String,Object> empty =  Collections.emptyMap();
		Result res = session.query(cypher, empty);
		Iterator<Map<String,Object>> it = res.iterator();
		if (!it.hasNext()) return Collections.emptyList();
		
		List<T> ret = new LinkedList<>();
		while(it.hasNext()) {
			Map<String,Object> next = it.next();
			if (!next.containsKey(var))  continue;
			Object valueOrArray = next.get(var);
			T val = null;
			if (valueOrArray.getClass().isArray()) {
				val = (T)Array.get(valueOrArray, 0);
			} else {
				val = (T)valueOrArray;
			}
			ret.add(val);
		}
		return ret;
	}
}
