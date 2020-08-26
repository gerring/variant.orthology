package org.jax.gweaver.variant.orthology.io;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Spliterator;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.beanutils.BeanMap;
import org.jax.gweaver.variant.orthology.domain.GeneticEntity;

/**
 * Class for readers of lines to types.
 * 
 * This class is a bit hard to understand because it is a Stream. The slight complexity
 * here is worth it to enable a Stream to be used with any file. Parallel streams might
 * be faster for processing large gene files.
 * 
 * @author Matthew Gerring
 * @param <T> The type of thing this reader will read.
 */
public abstract class AbstractReader<T> implements Spliterator<T> {

	private static final long build = generateBuildNumber(); // Once per vm execution

	/**
	 * The scanner for all the file(s) which we will parse. 
	 */
	protected final Iterator<String> scanner;
	protected final String species;
	
	/**
	 * The file, if any, which we will use for estimation.
	 */
	private File file;
	
	/**
	 * Amount to wind forward when using multi-threading.
	 * This is the maximum amount which one thread will tackle
	 * in a single job.
	 */
	private int windForwardAmount = 1000;
	
	private volatile int count;

	public AbstractReader(String species, File file) throws IOException {
		// Iterate the file with a Scanner which does not load the file to memory
		// and gives each line at a time.
		this(species, new ScannerInterator<String>(file));
		this.file = file; // Used for estimation
	}
	
	protected AbstractReader(String species, Iterator<String> iterator) {
		this.species = species;
		this.scanner = iterator;
		this.count = 0;
	}
	
	/**
	 * Just used for RepeatedLineReader.
	 * @param species
	 */
	protected AbstractReader(String species) {
		this(species, (Iterator<String>)null);
	}

	
	public Stream<T> stream() {
		return StreamSupport.stream(this, false);
	}

	private static long generateBuildNumber() {
		LocalDateTime now = LocalDateTime.now();
		String format = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH));
		return Long.parseLong(format);
	}
       
	/**
	 * Parse the line to type T
	 * @param line
	 * @return
	 * @throws ReaderException
	 */
	protected abstract T create(String line) throws ReaderException;
		 
	/**
	 * The variants are encoded with delimiter space(' ') or '#'
	 * @return
	 */
	protected abstract String getAssignmentChar();

	/**
	 * 
	 * @param name
	 * @param from
	 * @param to
	 * @return previous value in to or null if name is not in from or was not previously set in to.
	 */
	protected Object transfer(String name, Map<String,Object> from, Map<Object,Object> to) {
		if (from.containsKey(name)) {
			return to.put(name, from.get(name));
		}
		return null;
	}

	/**
	 * @return the build
	 */
	public long getBuild() {
		return build;
	}

	@Override
	public boolean tryAdvance(Consumer<? super T> action) {
		
		String line = nextLine();
		if (line == null) return false;
		T made = null;
		try {
			while ((made = create(line)) == null) {
				line = nextLine();
				if (line == null) return false;
			}
		} catch (NullPointerException | ReaderException e) {
			throw new IllegalArgumentException(e);
		}
		accept(made, action, scanner.hasNext());
		return true;
	}
	
	// TODO Test how many threads neo4j client can take
	//private Semaphore semaphore = new Semaphore(10);
	private void accept(T made, Consumer<? super T> action, boolean isMore) {
		if (made instanceof GeneticEntity) {
			((GeneticEntity)made).setLastInStream(!isMore);
		}	
		action.accept(made);
	}
	
	private synchronized String nextLine() {
		
		String line = null;
		try {
			if (!scanner.hasNext()) {
				line = null;
				return line;
			}

			line = scanner.next();
			if (line==null) return line;
			line = line.trim();
			if (!line.startsWith("#")) ++count;
			while((line.isEmpty() || line.startsWith("#")) && scanner.hasNext()) {
				line = scanner.next();
				if (line==null) return line;
				line = line.trim();
				if (! line.startsWith("#")) ++count;
			}
			if (line.isEmpty())  line = null;
			return line;
			
		} catch (IndexOutOfBoundsException | IllegalStateException | IllegalArgumentException i) {
			return null;
		
		} finally {
	 		if (line == null) {
	 			if (scanner instanceof Scanner) {
	 				((Scanner)scanner).close();
	 			}
	 		}
		}

	}

	@Override
	public Spliterator<T> trySplit() {
		try {
			if (!scanner.hasNext()) {
				return null;
			}
		} catch (IndexOutOfBoundsException | IllegalStateException | IllegalArgumentException i) {
			return null;
		}
		return chunk();
	}

	@Override
	public long estimateSize() {
		if (file==null) return 100000;
		String typical = "1	havana	transcript	11869	14409	.	+	.	gene_id \"ENSG00000223972\"; gene_version \"5\"; transcript_id \"ENST00000456328\"; transcript_version "
				+ "\"2\"; gene_name \"DDX11L1\"; gene_source \"havana\"; gene_biotype \"transcribed_unprocessed_pseudogene\"; transcript_name \"DDX11L1-202\"; transcript_source \""
				+ "havana\"; transcript_biotype \"processed_transcript\"; tag \"basic\"; transcript_support_level \"1\"; cannot be parsed ";
		int bytesPerLine = typical.getBytes().length;
		return file.length()/bytesPerLine;
	}

	@Override
	public int characteristics() {
		return Spliterator.IMMUTABLE | Spliterator.ORDERED;
	}

	private Spliterator<T> chunk() {
		
		return new Spliterator<T>() {
 		
			private Iterator<String> lines;
			private int size;
			private void readForward() {
				if (lines!=null) return;
				List<String> ls = Collections.synchronizedList(new LinkedList<>());
				for (int i = 0; i < windForwardAmount; i++) {
					String line = nextLine();
					if (line==null) break; // First null line is always the end
					ls.add(line);
				}
				lines = ls.iterator();
				size = ls.size();
			}
	
			@Override
			public boolean tryAdvance(Consumer<? super T> action) {
				if (lines==null) readForward();
				if (!lines.hasNext()) return false;
				
				String line = nextLocalLine();
				if (line == null || line.isEmpty()) return false;
				T made;
				try {
					while ((made = create(line)) == null) {
						line = nextLocalLine();
						if (line == null || line.isEmpty()) return false;
					}
				} catch (ReaderException e) {
					throw new IllegalArgumentException(e);
				}
				if (made!=null) {
					accept(made, action, lines.hasNext());
				}
				return made!=null;
			}
	
			private synchronized String nextLocalLine() {
				if (!lines.hasNext()) return null;
				return lines.next();
 			}

			@Override
			public Spliterator<T> trySplit() {
				return null;
			}
	
			@Override
			public long estimateSize() {
				if (lines==null) readForward();
				return size;
			}
	
			@Override
			public long getExactSizeIfKnown() {
				if (lines==null) readForward();
				return size;
		    }
			
			@Override
			public int characteristics() {
				return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.IMMUTABLE;
			}
			
		};
	}

	/**
	 * @return the windForwardAmount
	 */
	public int getWindForwardAmount() {
		return windForwardAmount;
	}

	/**
	 * @param windForwardAmount the windForwardAmount to set
	 */
	public void setWindForwardAmount(int windForwardAmount) {
		this.windForwardAmount = windForwardAmount;
	}
	
	/**
	 * All lines processed including those ignored.
	 * @return
	 */
	public int linesProcessed() {
		return count;
	}

	protected void populate(BeanMap d, String[] rec) {
        d.put("seq_id", rec[0]);
        d.put("source", rec[1]);
        d.put("type", rec[2]);
        d.put("start", rec[3]);
        d.put("end", rec[4]);
        d.put("score", rec[5]);
        d.put("strand", rec[6]);
        if (rec[6].length() > 8) {
        	d.put("strand", rec[6].substring(0, 8));
        }
        
        // Do not repeat information, there will be millions of nodes.
        // d.put("attributes", rec[8]);
        d.put("active", Boolean.TRUE);
        d.put("build", getBuild());
        d.put("species", species);
	}

	protected Map<String, Object> parseAttributes(String rec8) {
		// Split attributes in rec[8]
        // str: gene_id "ENSMUSG00000102693"; gene_version "1"; gene_name "4933401J01Rik"; gene_source "havana"; gene_biotype "TEC"; havana_gene "OTTMUSG00000049935"; havana_gene_version "1";
        String [] attr = rec8.split(";");
        Map<String,Object> attributes = new HashMap<>();
        for (int i = 0; i < attr.length; i++) {
        	String line = attr[i].trim().replace("\"", "");
			String[] kv = line.split(getAssignmentChar());
			if (kv.length==2) attributes.put(kv[0], kv[1].trim());
		}
        return attributes;
    }

}
