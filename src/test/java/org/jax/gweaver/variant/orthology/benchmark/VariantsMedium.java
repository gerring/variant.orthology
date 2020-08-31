package org.jax.gweaver.variant.orthology.benchmark;

import java.io.File;

import org.jax.gweaver.variant.orthology.domain.GeneticEntity;
import org.jax.gweaver.variant.orthology.io.AbstractReader;
import org.jax.gweaver.variant.orthology.io.VariantReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("This test is not really a unit test, it takes ten minutes to run.")
public class VariantsMedium extends BenchmarkTest {
	/**
	 * 874k nodes in ~514  (macbook)
	 * @throws Exception
	 */
	@Test
	public void variantZipRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Homo sapiens", new File("src/test/resources/data/zip/hs_gvf/homo_sapiens_incl_consequences_1.gvf.zip"));
		reader.setWindForwardAmount(2048);
		test(reader, 872732, 872993, true, 1);
	}

}
