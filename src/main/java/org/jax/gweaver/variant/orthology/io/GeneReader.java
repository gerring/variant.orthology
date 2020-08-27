package org.jax.gweaver.variant.orthology.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanMap;
import org.jax.gweaver.variant.orthology.domain.Gene;
import org.jax.gweaver.variant.orthology.domain.GeneticEntity;
import org.jax.gweaver.variant.orthology.domain.Transcript;

/**
 * Class which reads a file using Scanner such that even
 * large files may be parsed without all being in memory.
 * 
 * @author Matthew Gerring
 * @param <N>  A node entity, either a Gene or a Transcript related to a Gene.
 *
 */
public class GeneReader<N extends GeneticEntity> extends AbstractReader<N>{


	protected GeneReader(String species) {
		super(species);
	}
	
	public GeneReader(String species, File file) throws IOException {
		super(species, file, 4096); // Genes are not that dense maybe one gene / 10 lines
	}

	@SuppressWarnings("unchecked")
	@Override
	protected N create(String line) throws ReaderException {
        
		String[] rec = line.split("\t");
        Object bean = new Object();
        String type = rec[2].trim();
        if ("gene".equals(type.toLowerCase())) {
        	bean = new Gene();
        } else if ("transcript".equals(type.toLowerCase())) {
        	bean = new Transcript();
        } else if ("exon".equals(type.toLowerCase())) {
        	return null; // TODO
        } else {
        	// TODO Should we throw exceptions or ignore these cases. Examples cds, start_codon
        	return null;
        }
		
        try {
			BeanMap d = new BeanMap(bean);
			populate(d, rec);
	        
	        d.put("phase", rec[7]);
	        
	        Map<String,Object> attributes = parseAttributes(rec[8]);
	        d.put("gene_id", attributes.get("gene_id").toString().split(":")[0]);
	        d.put("gene_name", attributes.get("gene_name"));
	        d.put("gene_biotype", attributes.get("gene_biotype"));
 	        transfer("transcript_id", attributes, d);
	        transfer("transcript_biotype", attributes, d);
	        transfer("transcript_name", attributes, d);
	        
        } catch (IllegalArgumentException ne) {
        	throw new ReaderException("The line "+line+" of bean type "+bean.getClass().getSimpleName()+" cannot be parsed ", ne);
        }
        
        return (N)bean;
	}
	

	protected String getAssignmentChar() {
		return " ";
	}

}