package org.jax.gweaver.variant.orthology.benchmark;

import java.io.File;

import org.jax.gweaver.variant.orthology.domain.GeneticEntity;
import org.jax.gweaver.variant.orthology.io.AbstractReader;
import org.jax.gweaver.variant.orthology.io.GeneReader;
import org.jax.gweaver.variant.orthology.io.VariantReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("This test is not really a unit test, it takes four minutes to run.")
public class GenesMedium2 extends BenchmarkTest {

	
	/**
	 * Best time on macbook: 180s
	 * @throws Exception
	 */
	@Test
	public void geneZipRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Homo sapiens", new File("src/test/resources/data/zip/hs_gtf/hg38_1.gtf.zip"));
		test(reader, 115709, 1173235, false, 1);
	}


}
