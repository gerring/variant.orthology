package org.jax.gweaver.variant.orthology.node;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jax.gweaver.variant.orthology.AbstractNeo4jTest;
import org.jax.gweaver.variant.orthology.domain.Gene;
import org.jax.gweaver.variant.orthology.node.IndexCreator;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 
 * @see https://medium.com/neo4j/testing-your-neo4j-based-java-application-34bef487cc3c
 * @see https://dzone.com/articles/improving-neo4j-ogm-performance
 * 
 * @author Matthew Gerring
 *
 */
@Testcontainers
public class IndexCreatorTest extends AbstractNeo4jTest {
	
	@Test
	public void simpleIndexCreation() throws Exception {
		
		String crt = "CREATE INDEX ON :Fred(fred_id);";
		Map<String,Object> params =  Collections.emptyMap();
		Result res = sessionFactory.openSession().query(crt, params);
		assertFalse(res.iterator().hasNext());
	}
	
	@Test
	public void indexCreatorOneGene() throws Exception {
		
		Session session = sessionFactory.openSession();
		IndexCreator creator = new IndexCreator(session);		
		Result res = creator.create();
		assertNotNull(res);
		assertFalse(res.iterator().hasNext());
	}

}
