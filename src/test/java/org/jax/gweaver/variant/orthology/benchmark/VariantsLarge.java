package org.jax.gweaver.variant.orthology.benchmark;

import java.io.File;

import org.jax.gweaver.variant.orthology.domain.GeneticEntity;
import org.jax.gweaver.variant.orthology.io.AbstractReader;
import org.jax.gweaver.variant.orthology.io.VariantReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("This test is not really a unit test, it takes twenty minutes to run.")
public class VariantsLarge extends BenchmarkTest {

	/**
	 * 1.7 mill nodes in ~990s  (macbook)  roughly 0.6 ms/node
	 * @throws Exception
	 */
	@Test
	public void variantZipRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Mus musculus", new File("src/test/resources/data/zip/mm_gvf/mus_musculus_incl_consequences_1.gvf.zip"));
		reader.setWindForwardAmount(10000);
		test(reader, 1726211, 1726211, true, 1);
	}
	

}
