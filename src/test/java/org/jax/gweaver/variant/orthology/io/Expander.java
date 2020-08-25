package org.jax.gweaver.variant.orthology.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * 
 * Expands files in a zip, usually to a temporary folder.
 * 
 * @author Matthew Gerring
 *
 */
class Expander implements Closeable {

	private final Path dir;
	private final boolean deleteOnExit;
	
	/**
	 * Same as calling new Expander(Files.createTempDirectory("Expand"), true)
	 * 
	 * @throws IOException - If the temporary directory cannot be created.
	 */
	public Expander() throws IOException {
		this (Files.createTempDirectory("Expand"), true);
	}
	
	/**
	 * Same as calling new Expander(dir, true);
	 * @param dir - the directory to expand into. If it does not exist, it will be created.
	 */
	public Expander(Path dir) {
		this (dir, true);
	}
	
	/**
	 * 
	 * @param dir - The directory to expand into. If it does not exist, it will be created.
	 * @param deleteOnExit - True to mark files created as requiring the delete on exit flag to be set.
	 */
	public Expander(Path dir, boolean deleteOnExit) {
		this.dir = dir;
		this.deleteOnExit = deleteOnExit;
	}

	/**
	 * Call to expand a zip file to a directory. Optionally
	 * the files created may be set as delete on exit.
	 * 
	 * @param zip - The zip file to expand.
	 * @return list of files expanded
	 * @throws ZipException When zip is illegal
	 * @throws IOException If the file-writing does astray
	 */
	public List<Path> expand(Path zip) throws ZipException, IOException {

		List<Path> pathsExpanded = new ArrayList<>();
		File outputDir = dir.toFile();
		outputDir.mkdirs();
		
		try (ZipFile zipFile = new ZipFile(zip.toFile())) {

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				
				File des = new File(outputDir,  entry.getName());
				if (deleteOnExit) des.deleteOnExit();
				pathsExpanded.add(des.toPath());
				
				if (entry.isDirectory()) {
					des.mkdirs();
				} else {
					des.getParentFile().mkdirs();
					des.createNewFile();
					try (InputStream in = zipFile.getInputStream(entry);
						 OutputStream out = new FileOutputStream(des)) {
						IOUtils.copy(in, out);
					}
				}
			}
		}
		
		return pathsExpanded;
	}

	/**
	 * Call to delete the expand directory
	 * @throws RuntimeException - If it cannot be deleted.
	 * This method is not allowed to throw a checked exception
	 * so that is wrapped in one we can throw.
	 */
	@Override
	public void close()  {
		try {
			FileUtils.deleteDirectory(dir.toFile());
		} catch (IOException e) {
			if (Files.exists(dir) && dir.toFile().list().length<1) {
				return;
			}
			throw new RuntimeException(e);
		}
	}

	public Path getDir() {
		return dir;
	}

	public boolean isDeleteOnExit() {
		return deleteOnExit;
	}
}
