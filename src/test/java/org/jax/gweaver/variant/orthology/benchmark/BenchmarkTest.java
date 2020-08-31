package org.jax.gweaver.variant.orthology.benchmark;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jax.gweaver.variant.orthology.AbstractNeo4jTest;
import org.jax.gweaver.variant.orthology.domain.GeneticEntity;
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

@Testcontainers
public class BenchmarkTest extends AbstractNeo4jTest {


	private volatile long inodes;
	private volatile long curT5;
	private volatile long start;
	
	protected void test(AbstractReader<GeneticEntity> reader, int nodeCount, int lines, boolean threads, double rtpn) throws Exception {
		
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
		long nNode = nman.countAllNodes();
		assertEquals(nodeCount, nNode);
		assertEquals(lines, reader.linesProcessed());
		
		long time = System.currentTimeMillis()-start;
		double tpn = ((double)time)/nNode;
		assertTrue(rtpn>=tpn);
	}
	
	private void time(TimeInfo info) {

		inodes += info.getCount();
		
		// % operator will not work here because of multiple threads
		// means a nice ordered procedure through the lines is not happening.
		// Instead we just count the 
		long t5 = inodes/10000L;
		if (t5>curT5) {
			if (info.getCount()<=0) return;
			long time = System.currentTimeMillis()-start;
			double tpn = ((double)time)/inodes;
			
			String msg = String.format("Total %d in %d ms. Time per node %.2f ms", inodes, time, tpn);
			System.out.println(msg);
			curT5 = t5;
		}
	}

	
}
