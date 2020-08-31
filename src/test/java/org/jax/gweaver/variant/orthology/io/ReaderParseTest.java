package org.jax.gweaver.variant.orthology.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.glassfish.hk2.utilities.reflection.ParameterizedTypeImpl;
import org.jax.gweaver.variant.orthology.domain.GeneticEntity;
import org.jax.gweaver.variant.orthology.io.AbstractReader;
import org.jax.gweaver.variant.orthology.io.GeneReader;
import org.jax.gweaver.variant.orthology.io.RepeatedLineReader;
import org.jax.gweaver.variant.orthology.io.VariantReader;
import org.junit.jupiter.api.Test;

/**
 * Simple test to check file parsing without neo4j involved.
 * Test just parses files into objects without mixing in neo4j.
 * This means it is fast to run and try different parsing options.
 * 
 * @author Matthew Gerring
 *
 */
public class ReaderParseTest {
	
	/**
	 * TODO
	 * 1. Test more than one file in a directory
	 * 2. Test directory of zip and unzipped files.
	 * 3. Test more than one zip in a directory.
	 * 4. Test more than one file in a zip
	 * 5. Test directories in a zip (may fail)
	 */
	
	
	@Test
	public void simpleGeneRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Homo sapiens", new File("src/test/resources/data/1000/hs_gtf/hg38_2.gtf"));
		List<GeneticEntity> found = reader.stream().collect(Collectors.toList());
		
		assertEquals(224, found.size());
		assertEquals(1000, reader.linesProcessed());
	}

	@Test
	public void simpleGeneRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Mus musculus", new File("src/test/resources/data/1000/mm_gtf/mm10_2.gtf"));
		List<GeneticEntity> found = reader.stream().collect(Collectors.toList());
		
		assertEquals(157, found.size());
		assertEquals(1000, reader.linesProcessed());
	}

	@Test
	public void simpleVariantRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Homo sapiens", new File("src/test/resources/data/1000/hs_gvf/homo_sapiens_incl_consequences_2.gvf"));
		List<GeneticEntity> found = reader.stream().collect(Collectors.toList());
		
		assertEquals(1000, found.size());
		assertEquals(1000, reader.linesProcessed());
	}

	@Test
	public void simpleVariantRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Homo sapiens", new File("src/test/resources/data/1000/mm_gvf/mus_musculus_incl_consequences_2.gvf"));
		List<GeneticEntity> found = reader.stream().collect(Collectors.toList());
		
		assertEquals(1000, found.size());
		assertEquals(1000, reader.linesProcessed());
	}

	@Test
	public void parallelGeneRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Homo sapiens", new File("src/test/resources/data/1000/hs_gtf/hg38_2.gtf"));
		List<GeneticEntity> found = reader.stream().parallel().collect(Collectors.toList());
		
		assertEquals(224, found.size());
		assertEquals(1000, reader.linesProcessed());
	}

	@Test
	public void parallelGeneRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Mus musculus", new File("src/test/resources/data/1000/mm_gtf/mm10_2.gtf"));
		List<GeneticEntity> found = reader.stream().parallel().collect(Collectors.toList());
		
		assertEquals(157, found.size());
		assertEquals(1000, reader.linesProcessed());
	}

	@Test
	public void parallelVariantRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Homo sapiens", new File("src/test/resources/data/1000/hs_gvf/homo_sapiens_incl_consequences_2.gvf"));
		List<GeneticEntity> found = reader.stream().parallel().collect(Collectors.toList());
		
		assertEquals(1000, found.size());
		assertEquals(1000, reader.linesProcessed());
	}

	@Test
	public void parallelVariantRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Homo sapiens", new File("src/test/resources/data/1000/mm_gvf/mus_musculus_incl_consequences_2.gvf"));
		List<GeneticEntity> found = reader.stream().parallel().collect(Collectors.toList());
		
		assertEquals(1000, found.size());
		assertEquals(1000, reader.linesProcessed());
	}
	
	@Test
	public void simpleRepeatTest1() throws Exception {
		
		int rsize = 100000;
		AbstractReader<GeneticEntity> reader = new RepeatedLineReader<>("Mus musculus", rsize, GeneReader.class);
		long size = reader.stream().count();
		assertEquals(rsize, size);
		assertEquals(rsize, reader.linesProcessed());
	}

	@Test
	public void simpleRepeatTest2() throws Exception {
		
		int rsize = 100000;
		AbstractReader<GeneticEntity> reader = new RepeatedLineReader<>("Mus musculus", rsize, VariantReader.class);
		long size = reader.stream().count();
		assertEquals(rsize, size);
		assertEquals(rsize, reader.linesProcessed());
	}

	@Test
	public void variantZipRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Homo sapiens", new File("src/test/resources/data/zip/hs_gvf/homo_sapiens_incl_consequences_1.gvf.zip"));
		long count = reader.stream().count();
		assertEquals(872732, count);
		assertEquals(872993, reader.linesProcessed());
	}

	@Test
	public void variantZipRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Mus musculus", new File("src/test/resources/data/zip/mm_gvf/mus_musculus_incl_consequences_1.gvf.zip"));
		long count = reader.stream().count();
		assertEquals(1726211, count);
		assertEquals(1726211, reader.linesProcessed());
	}
	
	@Test
	public void geneZipRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Homo sapiens", new File("src/test/resources/data/zip/hs_gtf/hg38_1.gtf.zip"));
		long count = reader.stream().count();
		assertEquals(115709, count);
		assertEquals(1173235, reader.linesProcessed());
	}

	@Test
	public void geneZipRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Mus musculus", new File("src/test/resources/data/zip/mm_gtf/mm10_1.gtf.zip"));
		long count = reader.stream().count();
		assertEquals(95996, count);
		assertEquals(899084, reader.linesProcessed());
	}
	@Test
	public void parallelVariantZipRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Homo sapiens", new File("src/test/resources/data/zip/hs_gvf/homo_sapiens_incl_consequences_1.gvf.zip"));
		long count = reader.stream().parallel().count();
		assertEquals(872732, count);
		assertEquals(872993, reader.linesProcessed());
	}

	@Test
	public void parallelVariantZipRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new VariantReader<>("Mus musculus", new File("src/test/resources/data/zip/mm_gvf/mus_musculus_incl_consequences_1.gvf.zip"));
		long count = reader.stream().parallel().count();
		assertEquals(1726211, count);
		assertEquals(1726211, reader.linesProcessed());
	}
	
	@Test
	public void parallelGeneZipRead1() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Homo sapiens", new File("src/test/resources/data/zip/hs_gtf/hg38_1.gtf.zip"));
		long count = reader.stream().parallel().count();
		assertEquals(115709, count);
		assertEquals(1173235, reader.linesProcessed());
	}

	@Test
	public void parallelGeneZipRead2() throws Exception {
		
		AbstractReader<GeneticEntity> reader = new GeneReader<>("Mus musculus", new File("src/test/resources/data/zip/mm_gtf/mm10_1.gtf.zip"));
		long count = reader.stream().parallel().count();
		assertEquals(95996, count);
		assertEquals(899084, reader.linesProcessed());
	}

}
