package org.jax.gweaver.variant.orthology.benchmark;

import java.io.File;

import org.jax.gweaver.variant.orthology.domain.GeneticEntity;
import org.jax.gweaver.variant.orthology.io.AbstractReader;
import org.jax.gweaver.variant.orthology.io.GeneReader;
import org.jax.gweaver.variant.orthology.io.VariantReader;
import org.junit.jupiter.api.Test;


/**
 * Put in its own test class so that the docker image
 * gets cleaned on the next test. This test writes a lot of
 * stuff which is not easy to delete again without causing
 * out of memory errors.
 * 
 * @author gerrim
 *
 */
public class GenesMedium1 extends BenchmarkTest {

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
		test(reader, 95996, 899084, true, 1);
	}


}
