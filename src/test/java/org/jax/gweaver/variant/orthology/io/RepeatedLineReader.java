package org.jax.gweaver.variant.orthology.io;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.jax.gweaver.variant.orthology.domain.GeneticEntity;

/**
 * This class repeats the same line a given number of times. 
 * It allows tests to be created, including in production, which
 * check the scale of the solution. The real gvf and gtf files
 * may be huge when really processed.
 * 
 * @author Matthew Gerring
 *
 * @param <T>
 */
@SuppressWarnings("all")
public class RepeatedLineReader<T extends GeneticEntity> extends AbstractReader<T> {

	private AbstractReader<T> reader;
	
	/**
	 * Create a reader that just repeats a similar line 'size' number of times.
	 * Used for testing mostly.
	 * 
	 * @param species
	 * @param size
	 * @param type
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public RepeatedLineReader(String species, int size, Class<? extends AbstractReader> type) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		super(species, createIterator(size, type));
		setWindForwardAmount(1000);
		this.reader = type.getDeclaredConstructor(String.class).newInstance(species);
		
		// We just start the counters somewhere representative.
		geneCount = 223180;
		varCount = 656;
	}


	@Override
	protected T create(String line) throws ReaderException {
		return reader.create(line);
	}

	@Override
	protected String getAssignmentChar() {
		return reader.getAssignmentChar();
	}

	private static <T> Iterator<String> createIterator(final int size, Class<? extends AbstractReader> type) {
		return new Iterator<String>() {
			int counted = 0;

			@Override
			public boolean hasNext() {
				return counted<size;
			}

			@Override
			public String next() {
				String line = nextLine(type);
				counted++;
				return line;
			}
			
		};
	}

	private static int geneCount;
	private static int varCount;
	
	private static <T> String nextLine(Class<? extends AbstractReader> type) throws IllegalArgumentException {
		if (type == GeneReader.class) {
			return "1	ensembl	gene	758233	758336	.	-	.	gene_id \"ENSG00000"+(++geneCount)+"\"; gene_version \"1\"; gene_name \"RNU6-1199P\"; gene_source \"ensembl\"; gene_biotype \"snRNA\";";
		} else if (type == VariantReader.class) {
			return "19	dbSNP	SNV	92959	92959	.	+	.	ID="+(++varCount)+";Variant_seq=G;ancestral_allele=A;Variant_effect=upstream_gene_variant 0 transcript ENST00000633500;evidence_values=Frequency;Dbxref=dbSNP_150:rs1025620664;Reference_seq=A";
		} else {
			throw new IllegalArgumentException("Cannot get example line for "+type);
		}
	}

}
