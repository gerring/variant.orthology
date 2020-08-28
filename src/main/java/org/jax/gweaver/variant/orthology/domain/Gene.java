package org.jax.gweaver.variant.orthology.domain;

import java.util.Objects;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @see https://dzone.com/articles/introduction-to-neo4j-ogm
 * 
 * @author Matthew Gerring
 *
 */
@NodeEntity(label="Gene")
public class Gene extends GeneticEntity {
	
	
	@Index(unique=true)
    private String geneId;
	
	@Index(unique=true)
    private String geneName;
	private String geneBiotype;
	   	

    @Relationship(type = "PRODUCES", direction = Relationship.OUTGOING)
    private Transcript transcript;

    // Autogenerated
	/**
	 * @return the gene_id
	 */
	public String getGeneId() {
		return geneId;
	}


	/**
	 * @param gene_id the gene_id to set
	 */
	public void setGeneId(String gene_id) {
		this.geneId = gene_id;
	}


	/**
	 * @return the gene_name
	 */
	public String getGeneName() {
		return geneName;
	}


	/**
	 * @param gene_name the gene_name to set
	 */
	public void setGeneName(String gene_name) {
		this.geneName = gene_name;
	}

	/**
	 * @return the gene_biotype
	 */
	public String getGeneBiotype() {
		return geneBiotype;
	}


	/**
	 * @param gene_biotype the gene_biotype to set
	 */
	public void setGeneBiotype(String gene_biotype) {
		this.geneBiotype = gene_biotype;
	}


	/**
	 * @return the transcript
	 */
	public Transcript getTranscript() {
		return transcript;
	}


	/**
	 * @param transcript the transcript to set
	 */
	public void setTranscript(Transcript transcript) {
		this.transcript = transcript;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(geneBiotype, geneId, geneName, transcript);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Gene))
			return false;
		Gene other = (Gene) obj;
		return Objects.equals(geneBiotype, other.geneBiotype) && Objects.equals(geneId, other.geneId)
				&& Objects.equals(geneName, other.geneName) && Objects.equals(transcript, other.transcript);
	}

    // Auto-generated stuff
    
}