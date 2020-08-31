package org.jax.gweaver.variant.orthology.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the expander class.
 * 
 * @author Matthew Gerring
 *
 */
public class ExpanderTest {

	private Expander expander;
	private static List<Path> allDirectories;
	
	@BeforeClass
	public static void innit() {
		allDirectories = new ArrayList<>();
	}
	
	@AfterClass
	public static void checkGone() throws Exception {
		allDirectories.stream().forEach(dir->assertFalse(Files.exists(dir)));
	}

	@Before
	public void create() throws IOException {
		Path dir = Files.createTempDirectory("ExpanderDir");
		this.expander = new Expander(dir, true);
	}
	
	@After
	public void clean() throws IOException {
		allDirectories.add(expander.getDir());
		this.expander.close();
	}

	@Test
	public void unzip1() throws Exception {
	    Path path = Paths.get("src/test/resources/data/zip/hs_gtf/hg38_1.gtf.zip");
	    expander.expand(path);
	    
	    assertEquals(1, Files.list(expander.getDir()).count());
	}

}