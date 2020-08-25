package org.jax.gweaver.variant.orthology.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ScannerInterator<T> implements Iterator<String> {
	
	private static final Logger logger = LoggerFactory.getLogger(ScannerInterator.class);

	private Iterator<Scanner> scanners;
	private Scanner current;
	private Path expand;

	public ScannerInterator(File zipOrDirOrFile) throws IOException {
		this.scanners = createIterator(zipOrDirOrFile);
	}

	@Override
	public boolean hasNext() {
		if (current==null) {
			if (!scanners.hasNext()) {
				clean();
				return false;
			}
			current = scanners.next();
		}
		boolean scannerNext = current.hasNextLine();
		if (scannerNext) return true;
		current = null;
		return hasNext();
	}

	@Override
	public String next() {
		return current.nextLine();
	}


	private Iterator<Scanner> createIterator(File zdof) throws IOException {


		if (zdof.isFile()) {
			if (!zdof.getName().toLowerCase().endsWith(".zip")) {
				List<Scanner> scans = Arrays.asList(new Scanner(zdof));
				return scans.iterator();
			} else {
				expand = Files.createTempDirectory("Scanner");
				Expander expander = new Expander(expand);
				List<Path> expanded = expander.expand(zdof.toPath());
				
				final List<Scanner> scans = expanded.stream().map(p->{
					try {
						return new Scanner(p.toFile());
					} catch (FileNotFoundException e) {
						throw new RuntimeException(e);
					}
				}).collect(Collectors.toList());
				return scans.iterator();
			}
		}

		throw new IllegalArgumentException("No Scanner creator for "+zdof.getName());
	}
		
	private void clean() {
		try {
			if (expand!=null) {
				FileUtils.deleteDirectory(expand.toFile());
			}
		} catch (IOException e) {
			logger.error("Cannot delete temp zip dir!", e);
		}
	}

}
