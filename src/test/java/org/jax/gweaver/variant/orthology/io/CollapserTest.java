package org.jax.gweaver.variant.orthology.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CollapserTest {

	private Path testDir;

	@Before
	public void createTestDir() throws Exception {
		this.testDir = Files.createTempDirectory(getClass().getSimpleName());
		
		File test1 = new File(testDir.toFile(), "Test1.txt");
		test1.createNewFile();
		write("Hello World".getBytes("UTF-8"), test1);
		
		File test2 = new File(testDir.toFile(), "Test2.txt");
		test2.createNewFile();
		write(getReality().getBytes("UTF-8"), test2);

		File test3 = new File(testDir.toFile(), "Test3.txt");
		test3.createNewFile();
		write("Whad'ya know, Joe?".getBytes("UTF-8"), test3);

	}
	
	private void write(byte[] data, File file) throws FileNotFoundException, IOException {
		try (FileOutputStream out = new FileOutputStream(file)) {
			IOUtils.write(data, out);
		}
	}

	private String getReality() {
		StringBuilder buf = new StringBuilder();
		buf.append("Reality is a shapeless unity.\n");
		buf.append("The mind which distinguishes between aspects of this unit,\n");
		buf.append("sees only disunity.\n");
		buf.append("Remain unconcerned.");
		return buf.toString();
	}

	@After
	public void delete() throws Exception {
		FileUtils.deleteDirectory(testDir.toFile());
	}

	@Test
	public void dir() throws Exception {
		Path tmp = Files.createTempFile(getClass().getSimpleName()+"_dir", ".zip");
		try {
			Collapser.collapse(testDir, tmp);
			
			assertTrue(Files.size(tmp)>10);
			try (ZipFile zip = new ZipFile(tmp.toFile())) {
				assertEquals(3, zip.size());
			}
			
		} finally {
			tmp.toFile().delete();
		}
	}
	
	@Test
	public void file() throws Exception {
		Path tmp = Files.createTempFile(getClass().getSimpleName()+"_file", ".zip");
		try {
			Collapser.collapse(testDir.resolve("Test2.txt"), tmp);
			
			assertTrue(Files.size(tmp)>10);
			try (ZipFile zip = new ZipFile(tmp.toFile())) {
				assertEquals(1, zip.size());
			}
			
		} finally {
			tmp.toFile().delete();
		}
	}
	
	@Test
	public void emptyFile() throws Exception {
		File tmp = File.createTempFile(getClass().getSimpleName()+"_file", ".zip");
		tmp.createNewFile();
		File data = File.createTempFile(getClass().getSimpleName()+"_someDataFile", ".txt");
		data.createNewFile();
		try {
			Collapser.collapse(data.toPath(), tmp.toPath());
			
			assertTrue(tmp.length()>0);
			try (ZipFile zip = new ZipFile(tmp)) {
				assertEquals(1, zip.size());
			}
			
		} finally {
			tmp.delete();
			data.delete();
		}
	}

	@Test(expected=FileNotFoundException.class)
	public void fileNotWritable() throws Exception {
		File tmp = File.createTempFile(getClass().getSimpleName()+"_file", ".zip");
		tmp.createNewFile();
		tmp.setReadOnly();
		try {
			Collapser.collapse(testDir.resolve("Test2.txt"), tmp.toPath());
			
		} finally {
			tmp.delete();
		}
	}


	@Test(expected=FileNotFoundException.class)
	public void notexisting() throws Exception {
		Path tmp = Files.createTempFile(getClass().getSimpleName()+"_file", ".zip");
		try {
			Collapser.collapse(Paths.get("/some/not/existing/path"), tmp);			
		} finally {
			tmp.toFile().delete();
		}
	}

	@Test
	public void empty() throws Exception {
		Path tmp = Files.createTempFile(getClass().getSimpleName()+"_file", ".zip");
		Path dir = Files.createTempDirectory(getClass().getSimpleName());
		try {
			Collapser.collapse(dir, tmp);			
			assertTrue(Files.size(tmp)>1);
			try (ZipFile zip = new ZipFile(tmp.toFile())) {
				assertEquals(0, zip.size());
			}
		} finally {
			tmp.toFile().delete();
			dir.toFile().delete();
		}
	}

}