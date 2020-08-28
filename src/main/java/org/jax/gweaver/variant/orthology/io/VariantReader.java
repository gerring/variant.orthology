package org.jax.gweaver.variant.orthology.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.beanutils.BeanMap;
import org.jax.gweaver.variant.orthology.domain.GeneticEntity;
import org.jax.gweaver.variant.orthology.domain.Variant;

/**
 * Class which reads a file using Scanner such that even
 * large files may be parsed without all being in memory.
 * 
 * @author Matthew Gerring
 * @param <N>  A node entity, either a Gene or a Transcript related to a Gene.
 *
 */
public class VariantReader<N extends GeneticEntity> extends AbstractReader<N>{
	
	// TODO Are all invariant types a Variant or are some ignored?
	private static final Collection<String> VARIANTS = Arrays.asList("snv", "deletion", "insertion", "indel", "substitution");


	// Used in RepeatedLineReader, do not delete.
	protected VariantReader(String species) {
		super(species);
	}

	public VariantReader(String species, File file) throws IOException {
		super(species, file, 4096); // Variant every line in this file
	}

	@SuppressWarnings("unchecked")
	@Override
	protected N create(String line) throws ReaderException {
        
		String[] rec = line.split("\t");
        Object bean = new Object();
        String type = rec[2].trim();
        if (VARIANTS.contains(type.toLowerCase())) {
        	bean = new Variant();
        } else {
        	// TODO Should we throw exceptions or ignore these cases. Examples cds, start_codon
        	return null;
        }
        
// TODO Not finished. The connection is not made (:Variant) <-[:HAS_A]- (:Transcript)
//        # in the variant file (gvf) we have data about both variants
//        # and transcripts. In the GVF file a variant is followed by
//        # some number of transcripts that it connects to. We could make
//        # the relationship connection here, but first we need to index
//        # the variants on their rs_id, and the transcripts on
//        # transcript_id. Then, we'll scan through this file again with
//        # another script to make the relation:
//        # (:Variant) <-[:HAS_A]- (:Transcript)
		
        try {
			BeanMap d = new BeanMap(bean);
			populate(d, rec);
			
	        Map<String,Object> attributes = parseAttributes(rec[8]);
	        d.put("id", attributes.get("ID"));
	        d.put("rs_id", attributes.get("Dbxref").toString().split(":")[0]);
	        d.put("alt_allele", attributes.get("Variant_seq"));
	        d.put("ref_allele", attributes.get("Reference_seq"));
	        
        } catch (IllegalArgumentException ne) {
        	throw new ReaderException("The line "+line+" of bean type "+bean.getClass().getSimpleName()+" cannot be parsed ", ne);
        }
        
        return (N)bean;
	}

	protected String getAssignmentChar() {
		return "=";
	}

}