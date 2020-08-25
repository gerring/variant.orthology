package org.jax.gweaver.variant.orthology.domain;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jax.gweaver.variant.orthology.AbstractNeo4jTest;
import org.jax.gweaver.variant.orthology.domain.Gene;
import org.jax.gweaver.variant.orthology.io.AbstractReader;
import org.jax.gweaver.variant.orthology.io.GeneReader;
import org.jax.gweaver.variant.orthology.io.RepeatedLineReader;
import org.jax.gweaver.variant.orthology.io.VariantReader;
import org.jax.gweaver.variant.orthology.node.NodeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
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
public class OGMTest extends AbstractNeo4jTest {
	
	private int  nodeCount;
	private long start;
	
	@BeforeEach
	public void before() {
		nodeCount = -1;
		start = -1;
	}
	
	@Test
	public void checkConnectionQuery1() {
		
		Session session = sessionFactory.openSession();	
		Map<String,Object> empty =  Collections.emptyMap();
		session.query("CREATE (n:Person { name: 'A', title: 'Developer' });", empty);
		
		Result res = session.query("MATCH (n) RETURN distinct labels(n);", empty);		
		String[] labels = (String[])res.iterator().next().get("labels(n)");
		assertEquals(1, labels.length);
		assertEquals("Person", labels[0]);
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
		assertEquals(1, labels.size());
		assertEquals("Person", labels.get(0));
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
		gene.setGene_name("TEST");
		gene.setGene_id("fred");
		gene.setGene_biotype("bio");
		
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
	public void simpleRepeatedVariantead() throws Exception {
		
		int rsize = 1000; // Increase for better scale test.
		AbstractReader<GeneticEntity> reader = new RepeatedLineReader<>("Homo sapiens", rsize, VariantReader.class);
		Session session = sessionFactory.openSession();	
		long saved = reader.stream().map(node->{session.save(node);return node;}).count();
		
		assertEquals(rsize, saved);
		assertEquals(rsize, reader.linesProcessed());
		
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
		long saved = reader.stream().parallel().map(node->{session.save(node);return node;}).count();
		
		assertEquals(rsize, saved);
		assertEquals(rsize, reader.linesProcessed());
	}

	@Test
	public void variantZipRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Homo sapiens", new File("src/test/resources/data/zip/hs_gvf/homo_sapiens_incl_consequences_1.gvf.zip"));
		start = System.currentTimeMillis();
		reader.setConsumer(node->time());
		
		long count = reader.stream().map(node->{session.save(node);return node;}).count();
		assertEquals(872732, count);
		assertEquals(872993, reader.linesProcessed());
	}

	@Test
	public void variantZipRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Mus musculus", new File("src/test/resources/data/zip/mm_gvf/mus_musculus_incl_consequences_1.gvf.zip"));
		start = System.currentTimeMillis();
		reader.setConsumer(node->time());
		
		long count = reader.stream().map(node->{session.save(node);return node;}).count();
		assertEquals(1726211, count);
		assertEquals(1726211, reader.linesProcessed());
	}
	
	@Test
	public void geneZipRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Homo sapiens", new File("src/test/resources/data/zip/hs_gtf/hg38_1.gtf.zip"));
		start = System.currentTimeMillis();
		reader.setConsumer(node->time());
		
		long count = reader.stream().map(node->{session.save(node);return node;}).count();		
		assertEquals(115709, count);
		assertEquals(1173235, reader.linesProcessed());
	}

	/**
	 * 
	 * Running mm10_1.gtf times every 10000 nodes approx. (NOTE 10000 nodes added not lines as not all are nodes)
	 * 
	 * (ms)
	 * Java (1 thread):				48788
	 * Python (1 thread py2neo):  	88559 
	 * Target:						1000
	 * 
	 * TODO Try: parallel()  transaction
	 * 
	 * @author Matthew Gerring
	 *
	 */
	@Test
	public void geneZipRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Mus musculus", new File("src/test/resources/data/zip/mm_gtf/mm10_1.gtf.zip"));
		start = System.currentTimeMillis();
		reader.setConsumer(node->time());
		
		long count = reader.stream().map(node->{session.save(node);return node;}).count();
		assertEquals(95996, count);
		assertEquals(899084, reader.linesProcessed());
	}


	private void time() {
		
		int increment = 10000;
		if (nodeCount%increment == 0) {
			long end = System.currentTimeMillis();
			long dif = end-start;
			start = end; 
			String msg = String.format("Completed %d nodes. Time last %d in %d ms", this.nodeCount, increment, dif);
			System.out.println(msg);
		}
		
		++nodeCount;
	}

}
