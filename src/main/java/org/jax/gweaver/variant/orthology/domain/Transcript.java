package org.jax.gweaver.variant.orthology.domain;

import java.util.Objects;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label="Transcript")
public class Transcript extends GeneticEntity {

	@Index(unique=true)
	private String transcript_id;
	
    private String transcript_name;
    
    private String gene_name;
	private String gene_biotype;
    private String gene_id;
    private String transcript_biotype;
    
	@Relationship(type = "PRODUCES", direction = Relationship.INCOMING)
    private Gene gene;
	
	
	@Relationship(type = "HAS_A", direction = Relationship.OUTGOING)
	private Variant variant;

	// Autogenerated
	/**
	 * @return the transcript_id
	 */
	public String getTranscript_id() {
		return transcript_id;
	}


	/**
	 * @param transcript_id the transcript_id to set
	 */
	public void setTranscript_id(String transcript_id) {
		this.transcript_id = transcript_id;
	}


	/**
	 * @return the transcript_name
	 */
	public String getTranscript_name() {
		return transcript_name;
	}


	/**
	 * @param transcript_name the transcript_name to set
	 */
	public void setTranscript_name(String transcript_name) {
		this.transcript_name = transcript_name;
	}


	/**
	 * @return the gene_id
	 */
	public String getGene_id() {
		return gene_id;
	}


	/**
	 * @param gene_id the gene_id to set
	 */
	public void setGene_id(String gene_id) {
		this.gene_id = gene_id;
	}

	/**
	 * @return the transcript_biotype
	 */
	public String getTranscript_biotype() {
		return transcript_biotype;
	}


	/**
	 * @param transcript_biotype the transcript_biotype to set
	 */
	public void setTranscript_biotype(String transcript_biotype) {
		this.transcript_biotype = transcript_biotype;
	}


	/**
	 * @return the gene
	 */
	public Gene getGene() {
		return gene;
	}


	/**
	 * @param gene the gene to set
	 */
	public void setGene(Gene gene) {
		this.gene = gene;
	}


	/**
	 * @return the variant
	 */
	public Variant getVariant() {
		return variant;
	}


	/**
	 * @param variant the variant to set
	 */
	public void setVariant(Variant variant) {
		this.variant = variant;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(gene, gene_biotype, gene_id, gene_name, transcript_biotype,
				transcript_id, transcript_name, variant);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Transcript))
			return false;
		Transcript other = (Transcript) obj;
		return Objects.equals(gene, other.gene) && Objects.equals(gene_biotype, other.gene_biotype)
				&& Objects.equals(gene_id, other.gene_id) && Objects.equals(gene_name, other.gene_name)
				&& Objects.equals(transcript_biotype, other.transcript_biotype)
				&& Objects.equals(transcript_id, other.transcript_id)
				&& Objects.equals(transcript_name, other.transcript_name) && Objects.equals(variant, other.variant);
	}


	/**
	 * @return the gene_name
	 */
	public String getGene_name() {
		return gene_name;
	}


	/**
	 * @param gene_name the gene_name to set
	 */
	public void setGene_name(String gene_name) {
		this.gene_name = gene_name;
	}


	/**
	 * @return the gene_biotype
	 */
	public String getGene_biotype() {
		return gene_biotype;
	}


	/**
	 * @param gene_biotype the gene_biotype to set
	 */
	public void setGene_biotype(String gene_biotype) {
		this.gene_biotype = gene_biotype;
	}

	// Auto-generated stuff
}