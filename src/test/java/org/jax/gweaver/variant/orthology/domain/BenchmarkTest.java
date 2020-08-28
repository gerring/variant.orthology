package org.jax.gweaver.variant.orthology.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.jax.gweaver.variant.orthology.AbstractNeo4jTest;
import org.jax.gweaver.variant.orthology.io.AbstractReader;
import org.jax.gweaver.variant.orthology.io.GeneReader;
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
import org.testcontainers.junit.jupiter.Testcontainers;

@Disabled
@Testcontainers
public class BenchmarkTest extends AbstractNeo4jTest {

	// These tests add a lot of nodes. Cleaning up after that
	// Can cause Neo4j to die
	@BeforeEach
	@AfterEach
	public void clearSession() {
		if (session!=null) {
			// TODO This does not work for 1.7 million nodes 
			// in one of the tests below.
			session.deleteAll(Gene.class);
			session.deleteAll(Variant.class);
			session.deleteAll(Transcript.class);
			session.purgeDatabase();
		}
	}

	/**
	 * 874k nodes in ~514  (macbook)
	 * @throws Exception
	 */
	@Test
	public void variantZipRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Homo sapiens", new File("src/test/resources/data/zip/hs_gvf/homo_sapiens_incl_consequences_1.gvf.zip"));
		test(reader, 872732, 872993, true);
	}

	/**
	 * 1.7 mill nodes in ~990s  (macbook)  roughly 0.6 ms/node
	 * @throws Exception
	 */
	@Test
	public void variantZipRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Mus musculus", new File("src/test/resources/data/zip/mm_gvf/mus_musculus_incl_consequences_1.gvf.zip"));
		test(reader, 1726211, 1726211, true);
	}
	
	
	/**
	 * Best time on macbook: 180s
	 * @throws Exception
	 */
	@Test
	public void geneZipRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Homo sapiens", new File("src/test/resources/data/zip/hs_gtf/hg38_1.gtf.zip"));
		test(reader, 115709, 1173235, false);
	}

	/**
	 * 
	 * Running mm10_1.gtf times every 10000 nodes approx. (NOTE 10000 nodes added not lines as not all are nodes)
	 * 
	 * Item									Time(ms) 	Notes
	 * Python (1 thread py2neo):  			88559 
	 * Java (1 thread):						48788
	 * Java (1 thread, commit/1000):		20772		Might get out of memory if commits not often enough
	 * Java (parallel, commit/1000):		6000		Might get out of memory if commits not often enough
	 * Java (parallel, commit/5000):		6120		Might get out of memory if commits not often enough
	 * Target:								10000		Anything under 10000  10x speed increase over python
	 * 
	 * Speed note: It would also be possible to run python scripts in the cloud and use cloud for the 
	 * multi-thrreading. This solution would keep everything in Python and probably be a little more reliable
	 * than concurrency in Java. However concurrency is easily testible on one desktop and can be deployed to 
	 * the cloud using less nodes so it *might* be cheaper top develop and deploy.
	 * 
	 * 95996 node in 75s on macbook
	 * 
	 * @author Matthew Gerring
	 *
	 */
	@Test
	public void geneZipRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Mus musculus", new File("src/test/resources/data/zip/mm_gtf/mm10_1.gtf.zip"));
		test(reader, 95996, 899084, true);
	}


	private volatile int inodes;
	private volatile int curT5;
	private volatile long start;
	
	private void test(AbstractReader<GeneticEntity> reader, int nodeCount, int lines, boolean threads) throws Exception {
		
		this.inodes = 0;
		this.start = System.currentTimeMillis();
		
		if (threads) {
			try (ThreadTransactionManager<GeneticEntity> man = new ThreadTransactionManager<>(session)) { 
							
				// Save add nodes committing every so often for speed reasons
				// Use  bunch of threads to do this. NOTE parallel streams were tried
				// but it is important to ensure that every chunk of things added is commited
				// by the same thread. This is why this approach is chosen.
				man.run(reader,  
						node->man.save(node),
						info->time(info));
			}
		} else {
			try (AbstractTransactionManager<Object> man = new SimpleTransactionManager<>(session, 1000)) {
				// Save nodes, every so often 
				reader.stream()
					.mapToInt(man::save)
					.count();
				
			}
		}
		
		NodeManager nman = new NodeManager(session);
		assertEquals(nodeCount, nman.countAllNodes());
		
		assertEquals(lines, reader.linesProcessed());
		
	}
	
	private void time(TimeInfo info) {

		inodes += info.getCount();
		
		// % operator will not work here because of miltiple threads
		// means a nice ordered procedure through the lines is not happening.
		int t5 = inodes/10000;
		if (t5>curT5) {
			long time = System.currentTimeMillis()-start;
			String msg = String.format("Total %d in %d. Completed last %d nodes in %d ms", inodes, time, info.getCount(), info.getTime());
			System.out.println(msg);
			curT5 = t5;
		}
	}

	
}
