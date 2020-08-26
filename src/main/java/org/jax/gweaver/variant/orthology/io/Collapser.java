package org.jax.gweaver.variant.orthology.io;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * 
 * A class used to collapse a path into a zip file.
 * 
 * @author Matthew Gerring
 *
 */
class Collapser implements Closeable {
	
	private Path output;

	/**
	 * Create a collapser which will collapse serveral locations to one file.
	 * You can also use the static collapse(...) method for a one time use.
	 * @param output - Where we will collapse to
	 */
	public Collapser(Path output) {
		this.output = output;
	}
	
	/**
	 * Collapse the source into the output path.
	 * Adds to the zip if something is already in the output path. Overwrites
	 * if the ZipEntry is the same as an existing one.
	 * 
	 * @param source file or dir to add
	 * @throws IOException - If paths not readable or writable
	 * @throws FileNotFoundException - if file cannot be created for zip
	 */
	public void collapse(Path source) throws IOException, FileNotFoundException {
		collapse(source, output);
	}
	
	/**
	 * Create zip file at output, containing source
	 * @param source - file or dir we want to zip
	 * @param output - location to which we would like to zip
	 * @throws IOException - If paths not readable or writable
	 * @throws FileNotFoundException - if file cannot be created for zip
	 */
	public static void collapse(Path source, Path output) throws IOException, FileNotFoundException {
		
		try (ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(output.toFile()))) {
			compress(source.getParent().toString(), source.getFileName().toString(), zipFile, path->true, path->path.toString());
		}
	}

	/**
	 * Create zip file at output, containing source
	 * @param source - file or dir we want to zip
	 * @param output - location to which we would like to zip
	 * @param checkIfWanted - check a file for being wanted in the zip
	 * @throws IOException - If paths not readable or writable
	 * @throws FileNotFoundException - if file cannot be created for zip
	 */
	public static void collapse(Path source, Path output, Predicate<Path> checkIfWanted) throws IOException, FileNotFoundException {
		
		collapse(source, output, checkIfWanted, path->path.toString());
	}
	

	/**
	 * Create zip file at output, containing source
	 * @param source - file or dir we want to zip
	 * @param output - location to which we would like to zip
	 * @param checkIfWanted - check a file for being wanted in the zip
	 * @param nameMapper - map names of zip entries
	 * @throws IOException - If paths not readable or writable
	 * @throws FileNotFoundException - if file cannot be created for zip
	 */
	public static void collapse(Path source, Path output, Predicate<Path> checkIfWanted, Function<Path,String> nameMapper) throws IOException, FileNotFoundException {
		
		try (ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(output.toFile()))) {
			compress(source.getParent().toString(), source.getFileName().toString(), zipFile, checkIfWanted, nameMapper);
		}
	}


	private static void compress(String rootDir, 
								 String dirName, 
								 ZipOutputStream out,
								 Predicate<Path> wanted,
								 Function<Path,String> nameMapper) throws IOException, FileNotFoundException {
		
		Path dir = Paths.get(rootDir, dirName);
		
		if (Files.isDirectory(dir)) {
			
			List<Path> files = Files.list(dir).collect(Collectors.toList());
			
			for (Path file : files) {
				
				if (!wanted.test(file)) continue;
				String fileName = file.getFileName().toString();
				if (Files.isDirectory(file)) {
					compress(rootDir, Paths.get(dirName,fileName).toString(), out, wanted, nameMapper);
				} else {
					ZipEntry entry = new ZipEntry(nameMapper.apply(Paths.get(dirName,fileName)));
					out.putNextEntry(entry);
	
					String path = Paths.get(rootDir, dirName, fileName).toString();
					try (FileInputStream in = new FileInputStream(path)) {
						IOUtils.copy(in, out);
					}
				}
			}
		} else {
			ZipEntry entry = new ZipEntry(nameMapper.apply(dir.getFileName()));
			out.putNextEntry(entry);
			try (FileInputStream in = new FileInputStream(dir.toFile())) {
				IOUtils.copy(in, out);
			}
		}
	}
	
	/**
	 * Call to delete the collapsed file, the zip file.
	 * @throws RuntimeException - If it cannot be deleted.
	 * This method is not allowed to throw a checked exception
	 * so that is wrapped in one we can throw.
	 */
	@Override
	public void close()  {
		FileUtils.deleteQuietly(output.toFile());
	}

}
