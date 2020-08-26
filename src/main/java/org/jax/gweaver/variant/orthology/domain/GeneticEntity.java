package org.jax.gweaver.variant.orthology.domain;

import java.util.Objects;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

public abstract class GeneticEntity {
	
	@Id
	@GeneratedValue
    private Long uid;

    private Long build = 0L;
    private Boolean active = true;
    
    private String chr;
    private String type;
    private String phase;
    private String strand;
    private String source;
    private String species;
    private Integer start = 0;
    private Integer end = 0;
	private String seq_id;  // sequence ID stores chromosome
	private String score;   // TODO Should this be number?

	// This is just used for making the 
	// writes parallel. It is not really a property
	// required for the science.
	private boolean lastInStream = false;

	/**
	 * @return the uid
	 */
	public Long getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(Long uid) {
		this.uid = uid;
	}

	/**
	 * @return the build
	 */
	public Long getBuild() {
		return build;
	}

	/**
	 * @param build the build to set
	 */
	public void setBuild(Long build) {
		this.build = build;
	}

	/**
	 * @return the active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(Boolean active) {
		this.active = active;
	}

	/**
	 * @return the chr
	 */
	public String getChr() {
		return chr;
	}

	/**
	 * @param chr the chr to set
	 */
	public void setChr(String chr) {
		this.chr = chr;
	}

	@Override
	public int hashCode() {
		return Objects.hash(active, build, chr, end, phase, score, seq_id, source, species, start, strand, type, uid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof GeneticEntity))
			return false;
		GeneticEntity other = (GeneticEntity) obj;
		return Objects.equals(active, other.active) && Objects.equals(build, other.build)
				&& Objects.equals(chr, other.chr) && Objects.equals(end, other.end)
				&& Objects.equals(phase, other.phase) && Objects.equals(score, other.score)
				&& Objects.equals(seq_id, other.seq_id) && Objects.equals(source, other.source)
				&& Objects.equals(species, other.species) && Objects.equals(start, other.start)
				&& Objects.equals(strand, other.strand) && Objects.equals(type, other.type)
				&& Objects.equals(uid, other.uid);
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the phase
	 */
	public String getPhase() {
		return phase;
	}

	/**
	 * @param phase the phase to set
	 */
	public void setPhase(String phase) {
		this.phase = phase;
	}

	/**
	 * @return the strand
	 */
	public String getStrand() {
		return strand;
	}

	/**
	 * @param strand the strand to set
	 */
	public void setStrand(String strand) {
		this.strand = strand;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the species
	 */
	public String getSpecies() {
		return species;
	}

	/**
	 * @param species the species to set
	 */
	public void setSpecies(String species) {
		this.species = species;
	}

	/**
	 * @return the start
	 */
	public Integer getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(Integer start) {
		this.start = start;
	}

	/**
	 * @return the end
	 */
	public Integer getEnd() {
		return end;
	}

	/**
	 * @param end the end to set
	 */
	public void setEnd(Integer end) {
		this.end = end;
	}

	/**
	 * @return the seq_id
	 */
	public String getSeq_id() {
		return seq_id;
	}

	/**
	 * @param seq_id the seq_id to set
	 */
	public void setSeq_id(String seq_id) {
		this.seq_id = seq_id;
	}

	/**
	 * @return the score
	 */
	public String getScore() {
		return score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(String score) {
		this.score = score;
	}

	/**
	 * @return the lastInStream
	 */
	public boolean isLastInStream() {
		return lastInStream;
	}

	/**
	 * @param lastInStream the lastInStream to set
	 */
	public void setLastInStream(boolean lastInStream) {
		this.lastInStream = lastInStream;
	}

}
