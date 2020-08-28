package org.jax.gweaver.variant.orthology.domain;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.util.Arrays;
import org.jax.gweaver.variant.orthology.AbstractNeo4jTest;
import org.jax.gweaver.variant.orthology.io.AbstractReader;
import org.jax.gweaver.variant.orthology.io.GeneReader;
import org.jax.gweaver.variant.orthology.io.RepeatedLineReader;
import org.jax.gweaver.variant.orthology.io.VariantReader;
import org.jax.gweaver.variant.orthology.node.NodeManager;
import org.jax.gweaver.variant.orthology.transaction.AbstractTransactionManager;
import org.jax.gweaver.variant.orthology.transaction.SimpleTransactionManager;
import org.jax.gweaver.variant.orthology.transaction.ThreadTransactionManager;
import org.jax.gweaver.variant.orthology.transaction.TimeInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 
 * @see https://medium.com/neo4j/testing-your-neo4j-based-java-application-34bef487cc3c
 * @see https://dzone.com/articles/improving-neo4j-ogm-performance
 * @see https://dzone.com/articles/introduction-to-neo4j-ogm
 * 
 * @author Matthew Gerring
 *
 */
@Testcontainers
public class OGMTest extends AbstractNeo4jTest {
	
	@BeforeEach
	@AfterEach
	public void clearSession() {
		if (session!=null) {
			session.deleteAll(Gene.class);
			session.deleteAll(Variant.class);
			session.deleteAll(Transcript.class);
			session.purgeDatabase();
		}
	}

	@Test
	public void checkConnectionQuery1() {
		
		Session session = sessionFactory.openSession();	
		Map<String,Object> empty =  Collections.emptyMap();
		session.query("CREATE (n:Person { name: 'A', title: 'Developer' });", empty);
		
		Result res = session.query("MATCH (n) RETURN distinct labels(n);", empty);		
		String[] labels = (String[])res.iterator().next().get("labels(n)");
		assertTrue(Arrays.asList(labels).contains("Person"));
	}

	@Test
	public void checkConnectionQuery2() {
		
		Session session = sessionFactory.openSession();	
		Map<String,Object> empty =  Collections.emptyMap();
		session.query("CREATE (n:Person { name: 'A', title: 'Developer' });", empty);
		session.query("CREATE (n:Person { name: 'B', title: 'Developer' });", empty);
		session.query("MATCH (a:Person),(b:Person)\n"+
				        "WHERE a.name = 'A' AND b.name = 'B'\n"+
				        "CREATE (a)-[r:RELTYPE]->(b)\n"+
				        "RETURN type(r);", empty);
		
		NodeManager man = new NodeManager(session);
		List<String> labels = man.getDistinctLabels();
		assertTrue(labels.contains("Person"));
	}

	@Test
	public void checkGeneSave1() {
		
		Map<String,Object> empty =  Collections.emptyMap();
		session.query("CREATE (g:Gene { id: 9876, gene_name: 'A', start: 1234, end: 4321 });", empty);
		
		NodeManager man = new NodeManager(session);
		List<String> labels = man.getDistinctLabels();
		assertEquals(1, labels.size());
		assertEquals("Gene", labels.get(0));
	}
	
	@Test
	public void checkGeneSave2() {
		
		
		Session session = sessionFactory.openSession();	
		Gene gene = new Gene();
		gene.setGeneName("TEST");
		gene.setGeneId("fred");
		gene.setGeneBiotype("bio");
		
		session.save(gene);
		
		Gene eneg = session.load(Gene.class, gene.getUid());
		assertEquals(gene, eneg);
		
		NodeManager man = new NodeManager(session);
		List<String> labels = man.getDistinctLabels();
		assertEquals(1, labels.size());
		assertEquals("Gene", labels.get(0));
		
		List<Gene> nodes = man.getAllNodes();
		assertEquals(1, nodes.size());
		assertEquals(gene, nodes.get(0));

	}
	
	@Test
	@Timeout(5000)
	public void simpleGeneRead() throws Exception {
		
		GeneReader<GeneticEntity> reader = new GeneReader<>("Homo sapiens", new File("src/test/resources/data/1000/hs_gtf/hg38_2.gtf"));
		Session session = sessionFactory.openSession();	
		reader.stream().forEach(node->session.save(node));
		
		assertEquals(1000, reader.linesProcessed());
		
		NodeManager man = new NodeManager(session);
		List<String> labels = man.getDistinctLabels();
		assertEquals(2, labels.size());
		assertTrue(labels.contains("Gene"));
		assertTrue(labels.contains("Transcript"));
	}

	
	@Test
	@Timeout(5000)
	public void parallelGeneRead() throws Exception {
		
		GeneReader<GeneticEntity> reader = new GeneReader<>("Homo sapiens", new File("src/test/resources/data/1000/hs_gtf/hg38_2.gtf"));
		Session session = sessionFactory.openSession();	
		List<GeneticEntity> nodes = reader.stream().parallel().map(node->{session.save(node);return node;}).collect(Collectors.toList());
		
		assertEquals(224, nodes.size());
		assertEquals(1000, reader.linesProcessed());
		
		NodeManager man = new NodeManager(session);
		List<String> labels = man.getDistinctLabels();
		assertEquals(2, labels.size());
		assertTrue(labels.contains("Gene"));
		assertTrue(labels.contains("Transcript"));
	}
	
	@Test
	@Timeout(5000)
	public void simpleRepeatedGeneRead() throws Exception {
		
		int rsize = 1000; // Increase for better scale test.
		AbstractReader<GeneticEntity> reader = new RepeatedLineReader<>("Homo sapiens", rsize, GeneReader.class);
		Session session = sessionFactory.openSession();	
		long saved = reader.stream().map(node->{session.save(node);return node;}).count();
		
		assertEquals(rsize, saved);
		assertEquals(rsize, reader.linesProcessed());
		
	}
	
	@Test
	@Timeout(5000)
	public void simpleRepeatedVariantRead() throws Exception {
		
		int rsize = 1000; // Increase for better scale test.
		AbstractReader<GeneticEntity> reader = new RepeatedLineReader<>("Homo sapiens", rsize, VariantReader.class);
		Session session = sessionFactory.openSession();	
		long saved = reader.stream().map(node->{session.save(node);return node;}).count();
		
		assertEquals(rsize, saved);
		assertEquals(rsize, reader.linesProcessed());
		
		NodeManager nman = new NodeManager(session);
		assertEquals(rsize, nman.countAllNodes());

	}

	
	@Test
	@Timeout(5000)
	public void parallelRepeatedGeneRead() throws Exception {
		
		int rsize = 1000; // Increase for better scale test.
		AbstractReader<GeneticEntity> reader = new RepeatedLineReader<>("Homo sapiens", rsize, GeneReader.class);
		Session session = sessionFactory.openSession();	
		long saved = reader.stream().parallel().map(node->{session.save(node);return node;}).count();
		
		assertEquals(rsize, saved);
		assertEquals(rsize, reader.linesProcessed());
	}

	
	@Test
	@Timeout(5000)
	public void parallelRepeatedVariantRead() throws Exception {
		
		int rsize = 1000; // Increase for better scale test.
		AbstractReader<GeneticEntity> reader = new RepeatedLineReader<>("Homo sapiens", rsize, VariantReader.class);
		Session session = sessionFactory.openSession();	
		long saved = reader.stream()
							.parallel()
							.map(node->{session.save(node);return node;})
							.count();
		
		assertEquals(rsize, saved);
		assertEquals(rsize, reader.linesProcessed());
	}

}
