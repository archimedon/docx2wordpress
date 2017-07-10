package com.rasajournal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {

    @JsonProperty(value = "term_id")
    public Long termId;

    @JsonProperty
    public Long parent;

    @JsonProperty
    public String name;

    public Category() {
	super();
    }

    /**
     * @param termId
     * @param parent
     * @param name
     */
    public Category(Long termId, Long parent, String name) {
	super();
	this.termId = termId;
	this.parent = parent;
	this.name = name;
    }

    /**
     * @return the termId
     */
    public Long getTermId() {
        return termId;
    }

    /**
     * @param termId the termId to set
     */
    public void setTermId(Long termId) {
        this.termId = termId;
    }

    /**
     * @return the parent
     */
    public Long getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(Long parent) {
        this.parent = parent;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
	return name;
    }

}
