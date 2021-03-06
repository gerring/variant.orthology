package org.jax.gweaver.variant.orthology.domain;

import java.util.Objects;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label="Variant")
public class Variant extends GeneticEntity {

	@Index(unique=true)
	private String rsId;
	
	@Index(unique=true)
	private String id;
    
	private String refAllele;
	private String altAllele;
	private Float altAlleleFreq = 0F;
    private String variantEffect;
    private String biotype;


	@Relationship(type = "HAS_A", direction = Relationship.INCOMING)
	private Transcript transcript;

	// Autogenerated

	/**
	 * @return the rs_id
	 */
	public String getRsId() {
		return rsId;
	}


	/**
	 * @param rs_id the rs_id to set
	 */
	public void setRsId(String rs_id) {
		this.rsId = rs_id;
	}


	/**
	 * @return the ref_allele
	 */
	public String getRefAllele() {
		return refAllele;
	}


	/**
	 * @param ref_allele the ref_allele to set
	 */
	public void setRefAllele(String ref_allele) {
		this.refAllele = ref_allele;
	}


	/**
	 * @return the alt_allele
	 */
	public String getAltAllele() {
		return altAllele;
	}


	/**
	 * @param alt_allele the alt_allele to set
	 */
	public void setAltAllele(String alt_allele) {
		this.altAllele = alt_allele;
	}


	/**
	 * @return the alt_allele_freq
	 */
	public Float getAltAlleleFreq() {
		return altAlleleFreq;
	}


	/**
	 * @param alt_allele_freq the alt_allele_freq to set
	 */
	public void setAltAlleleFreq(Float alt_allele_freq) {
		this.altAlleleFreq = alt_allele_freq;
	}

	/**
	 * @return the variant_effect
	 */
	public String getVariantEffect() {
		return variantEffect;
	}


	/**
	 * @param variant_effect the variant_effect to set
	 */
	public void setVariantEffect(String variant_effect) {
		this.variantEffect = variant_effect;
	}


	/**
	 * @return the biotype
	 */
	public String getBiotype() {
		return biotype;
	}


	/**
	 * @param biotype the biotype to set
	 */
	public void setBiotype(String biotype) {
		this.biotype = biotype;
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
		result = prime * result
				+ Objects.hash(altAllele, altAlleleFreq, biotype, id, refAllele, rsId, transcript, variantEffect);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Variant))
			return false;
		Variant other = (Variant) obj;
		return Objects.equals(altAllele, other.altAllele) && Objects.equals(altAlleleFreq, other.altAlleleFreq)
				&& Objects.equals(biotype, other.biotype) && Objects.equals(id, other.id)
				&& Objects.equals(refAllele, other.refAllele) && Objects.equals(rsId, other.rsId)
				&& Objects.equals(transcript, other.transcript) && Objects.equals(variantEffect, other.variantEffect);
	}


	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}


	// Auto-generated stuff: 
}